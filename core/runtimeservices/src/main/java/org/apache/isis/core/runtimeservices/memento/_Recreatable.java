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
package org.apache.isis.core.runtimeservices.memento;

import org.apache.isis.applib.services.bookmark.Oid;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.object.ManagedObject;

import lombok.RequiredArgsConstructor;

interface _Recreatable {

    @RequiredArgsConstructor
    enum RecreateStrategy implements _Recreatable {
        /**
         * The {@link ManagedObject} that this is the memento for, directly has
         * an {@link ValueFacet} (it is almost certainly a value), and so is
         * stored directly.
         */
        VALUE(new _RecreatableValue()),
        /**
         * The {@link ManagedObject} that this is for, is already known by its
         * (persistent) {@link Oid}.
         */
        LOOKUP(new _RecreatableLookup()),
        /**
         * If all other strategies fail, as last resort we use plain java serialization, provided
         * that the type in question is serializable
         */
        SERIALIZABLE(new _RecreatableSerializable());

        private final _Recreatable delegate;

        @Override
        public ManagedObject recreateObject(final _ObjectMementoForScalar memento, final MetaModelContext mmc) {
            return delegate.recreateObject(memento, mmc);
        }

        @Override
        public boolean equals(final _ObjectMementoForScalar memento, final _ObjectMementoForScalar otherMemento) {
            return delegate.equals(memento, otherMemento);
        }

        @Override
        public int hashCode(final _ObjectMementoForScalar memento) {
            return delegate.hashCode();
        }

    }

    ManagedObject recreateObject(_ObjectMementoForScalar memento, MetaModelContext mmc);

    boolean equals(_ObjectMementoForScalar memento, _ObjectMementoForScalar otherMemento);

    int hashCode(_ObjectMementoForScalar memento);

}