/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.applib.annotation;

import jakarta.xml.bind.annotation.XmlType;

/**
 * The positioning of a label for a property or action parameter.
 *
 * @since 1.x {@index}
 * @see org.apache.causeway.applib.annotation.PropertyLayout
 * @see org.apache.causeway.applib.annotation.ParameterLayout
 */
@XmlType(
        namespace = "https://causeway.apache.org/applib/layout/component"
        )
public enum LabelPosition {

    DEFAULT,
    LEFT,
    /**
     * Right position of the label for Boolean properties.
     * <strong>Not supported</strong> for now for other types.
     */
    RIGHT,
    TOP,
    NONE,
    /**
     * Ignore the value provided by this annotation (meaning that the framework will keep searching, in meta
     * annotations or superclasses/interfaces).
     */
    NOT_SPECIFIED

}
