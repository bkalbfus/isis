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

import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.object.ManagedObjects;
import org.apache.isis.core.metamodel.object.MmAssertionUtil;
import org.apache.isis.core.metamodel.object.PackedManagedObject;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMementoCollection;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMementoForEmpty;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMementoService;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

/**
 *
 * @since 2.0
 *
 */
@Service
@Named(IsisModuleCoreRuntimeServices.NAMESPACE + ".ObjectMementoServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class ObjectMementoServiceDefault implements ObjectMementoService {

    @Inject @Getter private SpecificationLoader specificationLoader;
    @Inject private MetaModelContext mmc;
    @Inject private ObjectManager objectManager;

    @Override
    public ObjectMemento mementoForBookmark(@NonNull final Bookmark bookmark) {
        return _ObjectMementoForScalar.createPersistent(bookmark, specificationLoader);
    }

    @Override
    public ObjectMemento mementoForSingle(@Nullable final ManagedObject adapter) {
        MmAssertionUtil.assertPojoIsScalar(adapter);

        return _ObjectMementoForScalar.create(adapter)
            .map(ObjectMemento.class::cast)
            .orElseGet(()->
                ManagedObjects.isSpecified(adapter)
                        ? new ObjectMementoForEmpty(adapter.getLogicalType())
                        : null);
    }

    @Override
    public ObjectMemento mementoForMulti(@Nullable final PackedManagedObject packedAdapter) {
        val listOfMementos = packedAdapter.unpack().stream()
                .map(this::mementoForSingle)
                .collect(Collectors.toCollection(ArrayList::new)); // ArrayList is serializable
        return ObjectMementoCollection.of(
                listOfMementos,
                packedAdapter.getLogicalType());
    }

    @Override
    public ObjectMemento mementoForPojo(final Object pojo) {
        val managedObject = objectManager.adapt(pojo);
        return mementoForSingle(managedObject);
    }

    @Override
    public ObjectMemento mementoForPojos(final LogicalType logicalType, final Iterable<Object> iterablePojos) {
        val listOfMementos = _NullSafe.stream(iterablePojos)
                .map(pojo->mementoForPojo(pojo))
                .collect(Collectors.toCollection(ArrayList::new)); // ArrayList is serializable

        return ObjectMementoCollection.of(listOfMementos, logicalType);
    }

    @Override
    public ManagedObject reconstructObject(@Nullable final ObjectMemento memento) {

        if(memento==null) {
            return null;
        }

        if(memento instanceof ObjectMementoForEmpty) {
            val objectMementoForEmpty = (ObjectMementoForEmpty) memento;
            val logicalType = objectMementoForEmpty.getLogicalType();
            val spec = specificationLoader.specForLogicalType(logicalType);
            return spec.isPresent()
                    ? ManagedObject.empty(spec.get())
                    : ManagedObject.unspecified();
        }

        if(memento instanceof ObjectMementoCollection) {
            val objectMementoCollection = (ObjectMementoCollection) memento;

            val elementSpec = specificationLoader.specForLogicalTypeNameElseFail(memento.getLogicalTypeName());

            val objects = objectMementoCollection.unwrapList().stream()
                    .map(this::reconstructObject)
                    .collect(Can.toCan());

            return ManagedObject.packed(elementSpec, objects);
        }

        if(memento instanceof _ObjectMementoForScalar) {
            val objectMementoAdapter = (_ObjectMementoForScalar) memento;
            return objectMementoAdapter.reconstructObject(mmc);
        }

        throw _Exceptions.unrecoverable("unsupported ObjectMemento type %s", memento.getClass());
    }

}
