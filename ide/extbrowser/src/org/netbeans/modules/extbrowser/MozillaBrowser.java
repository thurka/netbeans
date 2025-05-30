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

package org.netbeans.modules.extbrowser;

import java.util.logging.Level;
import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Martin Grebac
 */
public class MozillaBrowser extends ExtWebBrowser {

    private static final long serialVersionUID = -3982770681461437966L;

    /** Creates new ExtWebBrowser */
    public MozillaBrowser() {
        super(PrivateBrowserFamilyId.MOZILLA);
    }

    /** Determines whether the browser should be visible or not
     *  @return true when OS is Windows.
     *          false in all other cases.
     */
    public static Boolean isHidden () {
        String detectedPath = null;
        if (Utilities.isWindows()) {
            try {
                detectedPath = NbDdeBrowserImpl.getBrowserPath("MOZILLA");      // NOI18N
            } catch (NbBrowserException e) {
                ExtWebBrowser.getEM().log(Level.FINEST, "Cannot detect Mozilla", e);      // NOI18N
            }
            if ((detectedPath != null) && (detectedPath.trim().length() > 0)) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
        return (Utilities.isUnix() && !Utilities.isMac()) ? Boolean.FALSE : Boolean.TRUE;
    }
    
    /** Getter for browser name
     *  @return name of browser
     */
    @Override
    public String getName () {
        if (name == null) {
            this.name = NbBundle.getMessage(MozillaBrowser.class, "CTL_MozillaBrowserName");
        }
        return name;
    }
    
    /**
     * Returns a new instance of BrowserImpl implementation.
     * @throws UnsupportedOperationException when method is called and OS is not Windows.
     * @return browserImpl implementation of browser.
     */
    @Override
    public HtmlBrowser.Impl createHtmlBrowserImpl() {
        ExtBrowserImpl impl = null;

        if (Utilities.isWindows()) {
            impl = new NbDdeBrowserImpl(this);
        } else if (Utilities.isUnix() && !Utilities.isMac()) {
            impl = new UnixBrowserImpl(this);
        } else {
            throw new UnsupportedOperationException (NbBundle.getMessage (MozillaBrowser.class, "MSG_CannotUseBrowser"));
        }
        
        return impl;
    }
    
    /** Default command for browser execution.
     * Can be overriden to return browser that suits to platform and settings.
     *
     * @return process descriptor that allows to start browser.
     */
    @Override
    protected NbProcessDescriptor defaultBrowserExecutable () {

        String prg;
        String params = "";                                                     // NOI18N
        NbProcessDescriptor retValue;
        
        //Windows
        if (Utilities.isWindows()) {
            params += "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";
            try {
                prg = NbDdeBrowserImpl.getBrowserPath(ExtWebBrowser.MOZILLA);
                return new NbProcessDescriptor(prg, params);
            } catch (NbBrowserException e) {
                ExtWebBrowser.getEM().log(Level.FINE, "Failed to load open command", e);   // NOI18N
                prg = "C:\\Program Files\\Mozilla.org\\Mozilla\\mozilla.exe";     // NOI18N
            } catch (UnsatisfiedLinkError e) {
                prg = "iexplore";                                     // NOI18N
            }
        
            retValue = new NbProcessDescriptor (prg, params);
            return retValue;            
        
        //Unix
        } else { 
            
            prg = "mozilla";                                                      // NOI18N
            if (Utilities.getOperatingSystem() == Utilities.OS_LINUX) {
                java.io.File f = new java.io.File ("/usr/bin/mozilla"); // NOI18N
                if (f.exists()) {
                    prg = f.getAbsolutePath();
                }
                f = new java.io.File ("/usr/local/mozilla/mozilla"); // NOI18N
                if (f.exists()) {
                    prg = f.getAbsolutePath();
                }
            } else if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
                java.io.File f = new java.io.File ("/usr/sfw/lib/mozilla/mozilla"); // NOI18N
                if (f.exists()) {
                    prg = f.getAbsolutePath();
                } else {
                    f = new java.io.File ("/opt/csw/bin/mozilla"); // NOI18N
                    if (f.exists()) {
                        prg = f.getAbsolutePath();
                    }
                }
            }
            retValue = new NbProcessDescriptor(
                prg,
                "-remote \"openURL({" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "})\"", // NOI18N
                NbBundle.getMessage(MozillaBrowser.class, "MSG_BrowserExecutorHint")
            );
        }
        return retValue;    
    }

}
