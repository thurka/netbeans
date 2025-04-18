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

package org.netbeans.modules.j2ee.dd.api.common;

/**
 * Generated interface for InitParam and ContextParam elements.
 *
 *<p><b><span style="color:red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></span></b>
 *</p>
 */
public interface InitParam extends CommonDDBean, DescriptionInterface {

    public static final String PARAM_NAME = "ParamName";	// NOI18N
    public static final String PARAM_VALUE = "ParamValue";	// NOI18N

    /** Setter for param-name property.
     * @param value property value
     */
    public void setParamName(java.lang.String value);
    /** Getter for param-name  property.
     * @return property value 
     */
    public java.lang.String getParamName();
    /** Setter for param-value property.
     * @param value property value
     */
    public void setParamValue(java.lang.String value);
    /** Getter for param-value  property.
     * @return property value 
     */
    public java.lang.String getParamValue();

}
