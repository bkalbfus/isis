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
package org.apache.isis.testdomain.commons;

import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.CollectionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.iactn.IsisInteractionFactory;
import org.apache.isis.core.runtime.persistence.changetracking.ChangedObjectsService;
import org.apache.isis.testdomain.auditing.AuditerServiceForTesting;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import lombok.val;

public abstract class InteractionTestAbstract extends IsisIntegrationTestAbstract {
    
    @Inject protected ObjectManager objectManager;
    @Inject protected IsisInteractionFactory interactionFactory;
    @Inject protected WrapperFactory wrapper;
    @Inject protected KVStoreForTesting kvStoreForTesting;
    @Inject private javax.inject.Provider<ChangedObjectsService> changedObjectsServiceProvider;
    
    protected ChangedObjectsService getChangedObjectsService() {
        return changedObjectsServiceProvider.get();
    }
    
    // -- INTERACTION STARTERS
    
    protected ActionInteraction startActionInteractionOn(Class<?> type, String actionId, Where where) {
        val viewModel = factoryService.viewModel(type);
        val managedObject = objectManager.adapt(viewModel);
        return ActionInteraction.start(managedObject, actionId, where);
    }
    
    protected PropertyInteraction startPropertyInteractionOn(Class<?> type, String propertyId, Where where) {
        val viewModel = factoryService.viewModel(type);
        val managedObject = objectManager.adapt(viewModel);
        return PropertyInteraction.start(managedObject, propertyId, where);
    }
    
    protected CollectionInteraction startCollectionInteractionOn(Class<?> type, String collectionId, Where where) {
        val viewModel = factoryService.viewModel(type);
        val managedObject = objectManager.adapt(viewModel);
        return CollectionInteraction.start(managedObject, collectionId, where);
    }
    
    // -- SHORTCUTS
    
    protected Object invokeAction(Class<?> type, String actionId, @Nullable List<Object> pojoArgList) { 
        val managedAction = startActionInteractionOn(type, actionId, Where.OBJECT_FORMS)
                .getManagedAction().get(); // should not throw  

        assertFalse(managedAction.checkVisibility().isPresent()); // is visible
        assertFalse(managedAction.checkUsability().isPresent()); // can invoke
        
        val args = managedAction.getInteractionHead()
                .getPopulatedParameterValues(pojoArgList);
        
        // spawns its own transactional boundary, or reuses an existing one if available 
        val either = managedAction.invoke(args);
        
        assertTrue(either.isLeft()); // assert action did not throw
        
        val actionResultAsPojo = either.leftIfAny().getPojo();
        
        return actionResultAsPojo;
    }
    
    // -- ASSERTIONS

    protected void assertMetamodelValid() {
        val specLoader = objectManager.getMetaModelContext().getSpecificationLoader(); 
        assertEquals(Collections.<String>emptyList(), specLoader.getValidationResult().getMessages());    
    }
    
    protected void assertComponentWiseEquals(Object a, Object b) {
        
        val array1 = _NullSafe.streamAutodetect(a)
            .collect(_Arrays.toArray(Object.class));
        val array2 = _NullSafe.streamAutodetect(b)
            .collect(_Arrays.toArray(Object.class));
        
        assertArrayEquals(array1, array2);
        
    }
    
    protected void assertComponentWiseUnwrappedEquals(Object a, Object b) {
        
        val array1 = _NullSafe.streamAutodetect(a)
            .map(element->(element instanceof ManagedObject) 
                    ? ((ManagedObject)element).getPojo()
                    : element)
            .collect(_Arrays.toArray(Object.class));
        
        val array2 = _NullSafe.streamAutodetect(b)
                .map(element->(element instanceof ManagedObject) 
                        ? ((ManagedObject)element).getPojo()
                        : element)
                .collect(_Arrays.toArray(Object.class));
        
        assertArrayEquals(array1, array2);
        
    }
    
    protected void assertEmpty(Object x) {
        if(x instanceof CharSequence) {
            assertTrue(_Strings.isEmpty((CharSequence)x));
            return;
        }
        assertEquals(0L, _NullSafe.streamAutodetect(x).count());
    }
    
    protected void assertDoesIncrement(Supplier<LongAdder> adder, Runnable runnable) {
        final int eventCount0 = adder.get().intValue();
        runnable.run();
        final int eventCount1 = adder.get().intValue();
        assertEquals(eventCount0 + 1, eventCount1);
    }
    
    protected void assertDoesNotIncrement(Supplier<LongAdder> adder, Runnable runnable) {
        final int eventCount0 = adder.get().intValue();
        runnable.run();
        final int eventCount1 = adder.get().intValue();
        assertEquals(eventCount0, eventCount1);
    }
    
    // -- ASSERTIONS (INTERACTIONAL)
    
    protected void assertInteractional(Runnable runnable) {
        InteractionBoundaryProbe.assertInteractional(kvStoreForTesting, runnable);
    }
    
    protected <T> T assertInteractional(Supplier<T> supplier) {
        return InteractionBoundaryProbe.assertInteractional(kvStoreForTesting, supplier);
    }
    
    // -- ASSERTIONS (TRANSACTIONAL)
    
    protected void assertTransactional(Runnable runnable) {
        InteractionBoundaryProbe.assertTransactional(kvStoreForTesting, runnable);
    }
    
    protected <T> T assertTransactional(Supplier<T> supplier) {
        return InteractionBoundaryProbe.assertTransactional(kvStoreForTesting, supplier);
    }
    
    // -- ASSERTIONS (AUDITING)
    
    protected void assertEmptyAudits() {
        val audits = AuditerServiceForTesting.getAuditEntries(kvStoreForTesting);
        assertTrue(audits.isEmpty());
    }

    protected void assertNoChangedObjectsPending() {
        // previous transaction has committed, so ChangedObjectsService should have cleared its data
        // however, this call has side-effects, it locks current transaction's capacity of further enlisting changes
        // missing a good solution on how to test this yet
        // assertTrue(getChangedObjectsService().getChangedObjectProperties().isEmpty());
    }
    
    protected void assertJdoBookCreateAudits() {

        val expectedAudits = _Sets.ofSorted(
                "Jdo Book/author: '[NEW]' -> 'Sample Author'",
                "Jdo Book/price: '[NEW]' -> '99.0'",
                "Jdo Book/publisher: '[NEW]' -> 'Sample Publisher'",
                "Jdo Book/isbn: '[NEW]' -> 'Sample ISBN'",
                "Jdo Book/description: '[NEW]' -> 'A sample book for testing.'",
                "Jdo Book/name: '[NEW]' -> 'Sample Book'",
                "Jdo Inventory/name: '[NEW]' -> 'Sample Inventory'");

        val actualAudits = AuditerServiceForTesting.getAuditEntries(kvStoreForTesting)
                .stream()
                .collect(Collectors.toCollection(TreeSet::new));

        assertEquals(expectedAudits, actualAudits);

        AuditerServiceForTesting.clearAuditEntries(kvStoreForTesting);

    }

    protected void assertJdoBookDeleteAudits() {

        val expectedAudits = _Sets.ofSorted(
                "Jdo Book/author: 'Sample Author' -> '[DELETED]'", 
                "Jdo Book/description: 'A sample book for testing.' -> '[DELETED]'",
                "Jdo Book/isbn: 'Sample ISBN' -> '[DELETED]'",
                "Jdo Book/name: 'Sample Book' -> '[DELETED]'", 
                "Jdo Book/price: '12.0' -> '[DELETED]'",
                "Jdo Book/publisher: 'Sample Publisher' -> '[DELETED]'",
                "Jdo Inventory/name: 'Sample Inventory' -> '[DELETED]'");

        val actualAudits = AuditerServiceForTesting.getAuditEntries(kvStoreForTesting)
                .stream()
                .collect(Collectors.toCollection(TreeSet::new));

        assertEquals(expectedAudits, actualAudits);

        AuditerServiceForTesting.clearAuditEntries(kvStoreForTesting);

    }

    protected void assertJdoBookPriceChangeAudit() {

        val expectedAudits = _Sets.ofSorted(
                "Jdo Book/price: '99.0' -> '12.0'");

        val actualAudits = AuditerServiceForTesting.getAuditEntries(kvStoreForTesting)
                .stream()
                .collect(Collectors.toCollection(TreeSet::new));

        assertEquals(expectedAudits, actualAudits);

        AuditerServiceForTesting.clearAuditEntries(kvStoreForTesting);
    }

    // -- UTILTITIES 
    
    protected void dumpAudits() {
        val audits = AuditerServiceForTesting.getAuditEntries(kvStoreForTesting);
        System.err.println("==AUDITS==");
        audits.forEach(System.err::println);
        System.err.println("==========");
    }

    
}
