package org.nakedobjects.persistence.cache;

import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.MockClassManager;
import org.nakedobjects.object.MockLoadedObjects;
import org.nakedobjects.object.MockObjectManager;
import org.nakedobjects.object.MockReflector;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.Person;
import org.nakedobjects.object.Role;
import org.nakedobjects.object.SimpleOid;
import org.nakedobjects.object.reflect.simple.JavaReflector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import junit.framework.TestCase;


public class InstancesTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(InstancesTest.class);
    }

    private Instances ins;
    private Person p1;
    private SimpleOid p1Oid;
    private Person p2;
    private SimpleOid p2Oid;

    public void setUp() {
        Logger.getRoot().setLevel(Level.OFF);
        
        NakedClass nc = NakedClass.createNakedClass(Person.class.getName(), MockReflector.class.getName());
        LoadedObjects loaded = new MockLoadedObjects();
        ins = new Instances(nc, loaded);
        assertEquals(0, ins.numberInstances());

        p1 = new Person();
        p1.getName().setValue("Harry");
        p1.getSalary().setValue(1234);
        p1Oid = new SimpleOid(1);
        p1.setOid(p1Oid);
        ins.create(p1);
        
        p2 = new Person();
        p2.getName().setValue("Fred");
        p1.getSalary().setValue(2345);
        p2Oid = new SimpleOid(2);
        p2.setOid(p2Oid);
        ins.create(p2);
      }

    public void testCount() {
        assertEquals(2, ins.numberInstances());
    }

    public void testInstances() {
        Enumeration e = ins.instances();
        assertEquals(p1, e.nextElement());
        assertEquals(p2, e.nextElement());
    }

    public void testPersistOids() throws Exception {
        MockClassManager manager = MockClassManager.setup();
        manager.setupAddNakedClass(NakedObject.class);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        ins.saveIdentities(oos);
        oos.close();
        
        byte[] buffer = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        ObjectInputStream ois = new ObjectInputStream(bais);
        
        NakedClass personClass = NakedClass.createNakedClass(Person.class.getName(), JavaReflector.class.getName());
        Instances ins2 = new Instances(personClass, new MockLoadedObjects());
        ins2.loadIdentities(ois);
        ois.close();
        
        assertEquals(2, ins2.numberInstances());
        Enumeration e = ins2.instances();
        Object p1r = e.nextElement();
        assertEquals(p1, p1r);
        assertFalse(p1 == p1r);
        Object p2r = e.nextElement();
        assertEquals(p2, p2r);
        assertFalse(p2 == p2r);

    }

    public void testPersistData() throws Exception {
        MockClassManager classManager = MockClassManager.setup();
        classManager.setupAddNakedClass(NakedObject.class);
        classManager.setupAddNakedClass(Person.class);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        ins.saveData(oos);
        oos.close();
        
        byte[] buffer = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        ObjectInputStream ois = new ObjectInputStream(bais);
        
        MockObjectManager manager = MockObjectManager.setup();
        manager.setupAddClass(Person.class);
        
        
        NakedClass personClass = NakedClass.createNakedClass(Person.class.getName(), JavaReflector.class.getName());
        MockLoadedObjects loadedWithOids = new MockLoadedObjects();
        
        // skeleton objects - loaded objects
        Person p1r = new Person();
        p1r.setOid(p1.getOid());
        Person p2r = new Person();
        p2r.setOid(p2.getOid());
        loadedWithOids.setupLoadedObjects(new NakedObject[] {p1r, p2r});
        
        Instances ins2 = new Instances(personClass, loadedWithOids);
        assertEquals(2,  ins2.loadData(ois));
        ois.close();
        
        assertEquals("this method does not load up the instances", 0, ins2.numberInstances());
        
        assertEquals(p1, p1r);
        assertFalse(p1 == p1r);
        assertEquals(p1.getName(), p1r.getName());
        assertEquals(p1.getSalary(), p1r.getSalary());
        
        assertEquals(p2, p2r);
        assertFalse(p2 == p2r);
        assertEquals(p2.getName(), p2r.getName());
        assertEquals(p2.getSalary(), p2r.getSalary());
    }

    public void testPersistData2() throws Exception {
        LoadedObjects loaded = new MockLoadedObjects();
        NakedClass nc = NakedClass.createNakedClass(Person.class.getName(), MockReflector.class.getName());
        ins = new Instances(nc, loaded);
       
        Role r = new Role();
        r.setPerson(p1);
        r.setOid(new SimpleOid(6));
        ins.create(r);
        
        MockClassManager classManager = MockClassManager.setup();
        classManager.setupAddNakedClass(Role.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        ins.saveData(oos);
        oos.close();
        
        byte[] buffer = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        ObjectInputStream ois = new ObjectInputStream(bais);
        
        MockObjectManager manager = MockObjectManager.setup();
        manager.setupAddClass(Person.class);
        
        
        NakedClass roleClass = NakedClass.createNakedClass(Person.class.getName(), JavaReflector.class.getName());
        MockLoadedObjects loadedWithOids = new MockLoadedObjects();
        
        // skeleton objects - loaded objects
        Person p1r = new Person();
        p1r.setOid(p1.getOid());
        Person p2r = new Person();
        p2r.setOid(p2.getOid());
        Role r1r = new Role();
        r1r.setOid(r.getOid());
        loadedWithOids.setupLoadedObjects(new NakedObject[] {r1r, p1r, p2r});
        
        Instances ins2 = new Instances(roleClass, loadedWithOids);
        assertEquals(1,  ins2.loadData(ois));
        ois.close();
        /*
        assertEquals("this method does not load up the instances", 0, ins2.numberInstances());
        
        assertEquals(p1, p1r);
        assertFalse(p1 == p1r);
        assertEquals(p1.getName(), p1r.getName());
        assertEquals(p1.getSalary(), p1r.getSalary());
        
        assertEquals(p2, p2r);
        assertFalse(p2 == p2r);
        assertEquals(p2.getName(), p2r.getName());
        assertEquals(p2.getSalary(), p2r.getSalary());
        */
    }

   public void testRead() {
        assertEquals(p2, ins.read(p2Oid));
        assertEquals(p1, ins.read(p1Oid));
    }

    public void testRemove() {
        ins.remove(p1);
        assertEquals(1, ins.numberInstances());
        Enumeration e = ins.instances();
        assertEquals(p2, e.nextElement());
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2004 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */