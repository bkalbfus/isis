/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2003  Naked Objects Group Ltd

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

    The authors can be contacted via www.nakedobjects.org (the
    registered address of Naked Objects Group is Kingsway House, 123 Goldworth
    Road, Woking GU21 1NR, UK).
*/

package org.nakedobjects.persistence.file;

import java.util.Vector;

import org.nakedobjects.object.SimpleOid;

public class ReferenceVector {
	private final SimpleOid oid;
	private final Vector elements = new Vector();
	
	public ReferenceVector(SimpleOid oid) {
		this.oid = oid;
	}

	public void add(SimpleOid oid) {
		elements.addElement(oid);
	}
	
	public void remove(SimpleOid oid) {
		elements.removeElement(oid);
	}

//	public Object first() {
//		return references.firstElement();	
//	}
//	
//	public Object last() {
//		return references.lastElement();	
//	}
//	
//	public Enumeration elements() {
//		return references.elements();	
//	}
//	
	public SimpleOid getOid() {
		return oid;
	}
	
	public int size() {
		return elements.size();
	}
	
	public SimpleOid elementAt(int index) {
		return (SimpleOid) elements.elementAt(index);
	}

//	public boolean contains(Object reference) {
//		return references.contains(reference);
//	}
//	
	public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (obj instanceof ReferenceVector) {
         return ((ReferenceVector) obj).elements.equals(elements);
      }

      return false;
	}

	public int hashCode() {
		int h = 17;
		h = 37 * h + elements.hashCode();
		return h;
	}	
}
