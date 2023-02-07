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
package org.apache.causeway.core.metamodel.object;

import java.util.function.Predicate;

import org.apache.causeway.core.metamodel.facets.object.title.TitleRenderRequest;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MmTitleUtil {

    public String abbreviatedTitleOf(final ManagedObject adapter, final int maxLength, final String suffix) {
        return _InternalTitleUtil.abbreviated(titleOf(adapter), maxLength, suffix);
    }

    public String titleOf(final ManagedObject adapter) {
        return adapter!=null
                ? adapter.getTitle()
                : "";
    }

    public String getTitleHonoringTitlePartSkipping(
            final ManagedObject managedObject,
            final Predicate<ManagedObject> skipTitlePart) {
        return ManagedObjects.isPacked(managedObject)
                ? "(multiple objects)"
                : managedObject != null
                    ? _InternalTitleUtil.titleString(
                            TitleRenderRequest.builder()
                            .object(managedObject)
                            .skipTitlePartEvaluator(skipTitlePart)
                            .build())
                    : "(no object)";
    }

    public static String formatAnyCardinalityAsTitle(
            int cardinality, // number of items 
            String singularName) {
        switch (cardinality) {
        case 0:
            return "No " + singularName;
        case 1:
            return "1 " + singularName;
        default:
            return "" + cardinality + " entries of " + singularName;
        }
    }


}
