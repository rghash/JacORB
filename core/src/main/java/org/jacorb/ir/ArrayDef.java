package org.jacorb.ir;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import org.slf4j.Logger;
import org.omg.CORBA.INTF_REPOS;
import org.omg.PortableServer.POA;

public class ArrayDef
    extends IDLType
    implements org.omg.CORBA.ArrayDefOperations
{
    int size = -1;
    org.omg.CORBA.TypeCode element_type;
    org.omg.CORBA.IDLType element_type_def;
    private org.omg.CORBA.Repository ir;

    private Logger logger;
    private POA poa;

    public ArrayDef( org.omg.CORBA.TypeCode tc, 
                     org.omg.CORBA.Repository ir,
                     Logger logger,
                     POA poa )
    {
        this.logger = logger;
        this.poa = poa;

        if (tc.kind() != org.omg.CORBA.TCKind.tk_array)
        {
            throw new INTF_REPOS ("Precondition volation: TypeCode must be of kind array");
        }

         def_kind = org.omg.CORBA.DefinitionKind.dk_Array;
         this.ir = ir;
         type = tc;
         try
         {
             size = tc.length();
             element_type = tc.content_type();
             element_type_def = IDLType.create(  element_type, ir,
                                                 this.logger, this.poa);
         }
         catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
         {
             // cannot happen because of above test
         }

        if (element_type_def == null)
        {
            throw new INTF_REPOS ("Precondition volation: TypeCode must be of kind array");
        }

        this.logger.debug("New ArrayDef");
    }

    public int length()
    {
        return size;
    }

    public void length(int a)
    {
        size = a;
    }

    public org.omg.CORBA.TypeCode element_type()
    {
        return element_type;
    }

    public org.omg.CORBA.IDLType element_type_def()
    {
        return element_type_def;
    }

    public void element_type_def(org.omg.CORBA.IDLType a)
    {
        element_type_def = a;
    }

    public void destroy()
    {
        type = null;
        element_type = null;
        element_type_def = null;
    }
    public void define()
    {

    }
}
