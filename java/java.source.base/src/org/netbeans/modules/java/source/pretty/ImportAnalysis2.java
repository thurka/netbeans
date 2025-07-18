/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.source.pretty;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import com.sun.tools.javac.comp.Modules;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.util.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.builder.ASTService;
import org.netbeans.modules.java.source.builder.TreeFactory;
import org.netbeans.modules.java.source.save.DiffContext;
import org.netbeans.modules.java.source.save.ElementOverlay;
import org.netbeans.modules.java.source.save.ElementOverlay.FQNComputer;

/**
 *
 * @author Jan Lahoda
 */
public class ImportAnalysis2 {

    private final FQNComputer currentFQN = new FQNComputer();

    private Elements elements;
    private ElementUtilities elementUtils;
    private TreeFactory make;
    private Set<Element> imports;
    private Set<Element> imported;
    private LinkedList<Set<Element>> visibleThroughClasses;
    private Map<String, List<Element>> simpleNames2Elements;
    private PackageElement unnamedPackage;
    private Element pack;
    private ASTService model;
    private final ElementOverlay overlay;
    private CompilationUnitTree cut; //current compilation unit
    private Map<String, Element> usedImplicitlyImportedClassesCache;
    private Set<String> implicitlyImportedClassNames;
    private Element javaLang;
    private CodeStyle cs;
    private ModuleElement modle;

    public ImportAnalysis2(CompilationInfo info) {
        this(JavaSourceAccessor.getINSTANCE().getJavacTask(info).getContext());
        cs = DiffContext.getCodeStyle(info);
        elementUtils = info.getElementUtilities();
    }

    public ImportAnalysis2(Context env) {
        elements = JavacElements.instance(env);
        make = TreeFactory.instance(env);
        model = ASTService.instance(env);
        modle = Modules.instance(env).getDefaultModule();
        overlay = env.get(ElementOverlay.class);
        unnamedPackage = overlay != null ? overlay.unnamedPackage(model, elements, modle) : modle != null ? elements.getPackageElement(modle, "") : elements.getPackageElement("");
    }

    public void setCompilationUnit(CompilationUnitTree cut) {
        this.cut = cut;
    }

    public void setPackage(ExpressionTree packageNameTree) {
        currentFQN.setPackageNameTree(packageNameTree);
        
        if (packageNameTree == null) {
            //if there is no package declaration in the code, unnamedPackage should be used:
            this.pack = unnamedPackage;
            return;
        }

        String packageName = getFQN(packageNameTree);

        this.pack = overlay.resolve(model, elements, packageName);
    }

    /*
     * setPackage must be called before this method!
     */
    public void setImports(List<? extends ImportTree> importsToAdd) {
        imports = new HashSet<Element>();
        imported = new HashSet<Element>();
        simpleNames2Elements = new HashMap<>();
        visibleThroughClasses = new LinkedList<Set<Element>>();
        usedImplicitlyImportedClassesCache = null;

        List<ImportTree> moduleImports = new ArrayList<>();
        List<ImportTree> onDemandImports = new ArrayList<>();
        List<ImportTree> namedImports = new ArrayList<>();

        for (ImportTree imp : importsToAdd) {
            if (imp.isModule()) {
                moduleImports.add(imp);
            } else if (getFQN(imp.getQualifiedIdentifier()).endsWith(".*")) {
                onDemandImports.add(imp);
            } else {
                namedImports.add(imp);
            }
        }

        //process imports in order to ensure shadowing semantics:
        Set<String> defined = new HashSet<>();
        Set<String> clashing = new HashSet<>();
        moduleImports.forEach(imp -> addImport(imp, defined, clashing));
        clashing.clear();
        onDemandImports.forEach(imp -> addImport(imp, defined, clashing));
        clashing.clear();
        namedImports.forEach(imp -> addImport(imp, defined, clashing));

        implicitlyImportedClassNames = new HashSet<String>();
        javaLang = overlay.resolve(model, elements, "java.lang");
        
        if (javaLang != null) {//might be null for broken platforms
            for (Element e : javaLang.getEnclosedElements()) {
                implicitlyImportedClassNames.add(e.getSimpleName().toString());
            }
        }

        if (pack != null) {
            for (Element e : pack.getEnclosedElements()) {
                implicitlyImportedClassNames.add(e.getSimpleName().toString());
            }
        }
    }
    
    public Set<? extends Element> getImports() {
        return imports;
    }

    public void classEntered(ClassTree clazz, boolean isAnonymous) {
        currentFQN.enterClass(clazz, isAnonymous);

        Set<Element> visible = new HashSet<Element>();
        String what = currentFQN.getFQN();
        Element currentClassElement = what != null ? overlay.resolve(model, elements, what) : null;

        if (currentClassElement != null) {
            visible.add(currentClassElement);
        }

        visibleThroughClasses.addFirst(visible);
    }

    public void enterVisibleThroughClasses(ClassTree clazz) {
        Set<Element> visible = visibleThroughClasses.getFirst();
        visible.addAll(overlay.getAllVisibleThrough(model, elements, currentFQN.getFQN(), clazz, modle));
    }

    public void classLeft() {
        visibleThroughClasses.removeFirst();
        currentFQN.leaveClass();
    }

    private String getFQN(ImportTree imp) {
        return getFQN(imp.getQualifiedIdentifier());
    }

    private String getFQN(Tree expression) {
        final StringBuffer result = new StringBuffer();

        new ErrorAwareTreeScanner<Void, Void>() {

            @Override
            public Void visitMemberSelect(MemberSelectTree tree, Void p) {
                super.visitMemberSelect(tree, p);
                result.append('.');
                result.append(tree.getIdentifier().toString());
                return null;
            }

            @Override
            public Void visitIdentifier(IdentifierTree tree, Void p) {
                result.append(tree.getName().toString());
                return null;
            }
        }.scan(expression, null);

        return result.toString();
    }

    private void addImport(ImportTree imp,
                           Set<String> currentTypeImportedNames,
                           Set<String> currentTypeClashingNames) {
        String fqn = getFQN(imp);

        if (imp.isModule()) {
            Element resolved = overlay.resolve(model, elements, fqn);

            if (resolved != null && resolved.getKind() == ElementKind.MODULE) {
                for (PackageElement pack : elementUtils.transitivelyExportedPackages((ModuleElement) resolved)) {
                    for (Element packageContent : pack.getEnclosedElements()) {
                        addImportedElement(overlay.wrap(model, elements, packageContent),
                                           false,
                                           currentTypeImportedNames,
                                           currentTypeClashingNames);
                    }
                }
            }
        } else  if (!imp.isStatic()) {
            Element resolve = overlay.resolve(model, elements, fqn);

            if (resolve != null) {
                addImportedElement(resolve, false, currentTypeImportedNames, currentTypeClashingNames);
            } else {
                //.*?:
                if (fqn.endsWith(".*")) {
                    fqn = fqn.substring(0, fqn.length() - 2);

                    List<TypeElement> classes = Collections.<TypeElement>emptyList();
                    Element clazz = overlay.resolve(model, elements, fqn);

                    if (clazz != null) {
                        classes = ElementFilter.typesIn(clazz.getEnclosedElements());
                    }

                    for (TypeElement te : classes) {
                        addImportedElement(te, false, currentTypeImportedNames, currentTypeClashingNames);
                    }
                } else {
                    //cannot resolve - the imports will probably not work correctly...
                }
            }
        } else {
            int dot = fqn.lastIndexOf('.');

            if (dot != (-1)) {
                String className = fqn.substring(0, dot);
                String memberName = fqn.substring(dot + 1);
                boolean isStarred = "*".equals(memberName);
                Element resolved = overlay.resolve(model, elements, className);

                if (resolved != null) {
                    boolean added = false;
                    for (Element e : resolved.getEnclosedElements()) {
                        if (!e.getModifiers().contains(Modifier.STATIC)) {
                            continue;
                        }
                        if (isStarred || memberName.contains(e.getSimpleName().toString())) {
                            addImportedElement(e, true, currentTypeImportedNames, currentTypeClashingNames);
                        }
                    }
                } else {
                    //cannot resolve - the imports will probably not work correctly...
                }
            } else {
                //no dot?
            }
        }
    }

    private void addImportedElement(Element el,
                                    boolean staticImport,
                                    Set<String> currentTypeImportedNames,
                                    Set<String> currentTypeClashingNames) {
        String simpleName = el.getSimpleName().toString();

        if (currentTypeClashingNames.contains(simpleName)) {
            //there's already a clash for this name, ignore
            return ;
        }

        List<Element> existing = simpleNames2Elements.computeIfAbsent(simpleName, x -> new ArrayList<>());

        if (!existing.isEmpty() && !existing.contains(el)) {
            if (!currentTypeImportedNames.contains(simpleName)) {
                //the element from other import type is shadowed, clear from imported:
                imported.removeAll(existing);
            } else if (!staticImport) {
                //clashing simple names inside the same import type:
                imported.removeAll(existing);
                existing.clear();
                currentTypeClashingNames.add(simpleName);
                return ;
            }
        }

        imported.add(el);
        existing.add(el);
        currentTypeImportedNames.add(simpleName);
    }

    //Note: this method should return either "orig" or a IdentifierTree or MemberSelectTree
    //no other tree type is not allowed - see ImmutableTreeTranslator.translateStable(Tree)
    public ExpressionTree resolveImport(MemberSelectTree orig, final Element element) {
        if (visibleThroughClasses == null || element == null || cs != null && cs.useFQNs()) {
            //may happen for package clause
            return orig;
        }

        if (element.getKind() == ElementKind.PACKAGE) {
            return make.MemberSelect(orig.getExpression(), orig.getIdentifier());
        }
        
        String simpleName = element.getSimpleName().toString();
        boolean clash = false;

        //if type is already accessible, do not import:
        for (Set<Element> els : visibleThroughClasses) {
            if (els.contains(element)) {
                return make.Identifier(element.getSimpleName());
            }
            if (els.stream().anyMatch(el -> el.getSimpleName().contentEquals(simpleName))) {
                clash = true;
                break;
            }
        }

        List<Element> alreadyImported = simpleNames2Elements.get(simpleName);

        clash = clash | (alreadyImported != null && !alreadyImported.isEmpty() && !alreadyImported.contains(element));
        
        //in the same package:
        if (!clash && (element.getKind().isClass() || element.getKind().isInterface())) {
            Element parent = element.getEnclosingElement();

            if (pack != null && pack.equals(parent)) {
                //in the same package:
                return make.Identifier(element.getSimpleName());
            }
        }

        if (!clash && imported.contains(element)) {
            return make.Identifier(element.getSimpleName());
        }

        if (getPackageOf(element) != null && getPackageOf(element).isUnnamed()) {
            if (orig.getExpression().getKind() == Kind.MEMBER_SELECT) {
                return make.MemberSelect(resolveImport((MemberSelectTree) orig.getExpression(), element.getEnclosingElement()),
                                         element.getSimpleName());
            }
            return orig;
        }
        
        if (!clash && implicitlyImportedClassNames.contains(simpleName)) {
            //check clashes between (hidden) java.lang and the newly added element:
            Element used = getUsedImplicitlyImportedClasses().get(simpleName);

            clash = used != null && !element.equals(used);
        }
        
        if (clash) {
            // clashing import, use FQN - no need to continue with QualIdent,
            // make MemberSelectTree
            // (see issue #111024 for details)
            
            //for inner classes, try to resolve import for outter class first:
            if (element.getEnclosingElement().getKind().isClass() || element.getEnclosingElement().getKind().isInterface() && orig.getExpression().getKind() == Kind.MEMBER_SELECT) {
                Element wrappedEnclosingClass = overlay.wrap(model, elements, element.getEnclosingElement());
                return make.MemberSelect(resolveImport((MemberSelectTree) orig.getExpression(), wrappedEnclosingClass), orig.getIdentifier());
            } else {
                return make.MemberSelect(orig.getExpression(), orig.getIdentifier());
            }
        }

        //no creation of static imports yet, import class for fields and methods:
        if (!element.getKind().isClass() && !element.getKind().isInterface()) {
            ExpressionTree clazz = orig.getExpression();

            if (clazz.getKind() == Kind.MEMBER_SELECT) {
                clazz = resolveImport((MemberSelectTree) clazz, overlay.wrap(model, elements, element.getEnclosingElement()));
            }
            return make.MemberSelect(clazz, orig.getIdentifier());
        }

        TypeElement type = (TypeElement) element;

        Element parent = type.getEnclosingElement();
        if (parent != null) {
            if ((parent.getKind().isClass() || parent.getKind().isInterface()) && !cs.importInnerClasses()) {
                ExpressionTree clazz = orig.getExpression();
                if (clazz.getKind() == Kind.MEMBER_SELECT) {
                    clazz = resolveImport((MemberSelectTree) clazz, overlay.wrap(model, elements, parent));
                }
                return make.MemberSelect(clazz, orig.getIdentifier());
            }

            //check for java.lang:
            if (parent.getKind() == ElementKind.PACKAGE) {
                if ("java.lang".equals(((PackageElement) parent).getQualifiedName().toString())) {
                    return make.Identifier(element.getSimpleName());
                }
            }
        }

        Tree imp = make.Identifier(((QualifiedNameable) element).getQualifiedName());
        addImport(make.Import(imp, false), new HashSet<>(), new HashSet<>());
        
        Element original = overlay.getOriginal(element);
        if (original.getEnclosingElement().getKind() == ElementKind.PACKAGE) {
            if (!cs.useSingleClassImport() || checkPackagesForStarImport(((PackageElement)original.getEnclosingElement()).getQualifiedName().toString(), cs))
                original = original.getEnclosingElement();
        }
        imports.add(original);

        return make.Identifier(element.getSimpleName());
    }

    private boolean checkPackagesForStarImport(String pkgName, CodeStyle cs) {
        for (String s : cs.getPackagesForStarImport()) {
            if (s.endsWith(".*")) { //NOI18N
                s = s.substring(0, s.length() - 2);
                if (pkgName.startsWith(s))
                    return true;
            } else if (pkgName.equals(s)) {
                return true;
            }           
        }
        return false;
    }

    private PackageElement getPackageOf(Element el) {
        while ((el != null) && (el.getKind() != ElementKind.PACKAGE)) el = el.getEnclosingElement();

        return (PackageElement) el;
    }

    private Map<String, Element> getUsedImplicitlyImportedClasses() {
        if (usedImplicitlyImportedClassesCache != null) {
            return usedImplicitlyImportedClassesCache;
        }
        
        usedImplicitlyImportedClassesCache = new HashMap<String, Element>();
        
        new ErrorAwareTreeScanner<Void, Void>() {
            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                Element e = overlay.wrap(model, elements, model.getElement(node));

                //javaLang might be null for broken platforms
                if (e != null && ((javaLang != null && javaLang.equals(e.getEnclosingElement())) || (pack != null && pack.equals(e.getEnclosingElement())))) {
                    usedImplicitlyImportedClassesCache.put(e.getSimpleName().toString(), e);
                }

                return null;
            }
        }.scan(cut, null);
        
        return usedImplicitlyImportedClassesCache;
    }
}
