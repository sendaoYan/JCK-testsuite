/*
 * @test
 * @summary javax.management.ImmutableDescriptor conformance: JMX API type verification.
 * @run main ImmutableDescriptorTest
 */

import java.lang.reflect.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Date;
import java.util.Hashtable;
import java.util.Set;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.lang.management.*;
import java.net.*;
import java.security.*;
import java.rmi.*;
import java.rmi.server.*;
import javax.security.auth.Subject;
import javax.management.*;
import javax.management.loading.*;
import javax.management.modelmbean.*;
import javax.management.monitor.*;
import javax.management.openmbean.*;
import javax.management.relation.*;
import javax.management.remote.*;
import javax.management.remote.rmi.*;
import javax.management.timer.*;

public class ImmutableDescriptorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        ImmutableDescriptor d = new ImmutableDescriptor(new String[]{"name", "num"}, new Object[]{"value", 1});
        check(d.getFieldValue("name").equals("value"), "field value");
        check(d.getFieldNames().length == 2, "field names");
        check(d.isValid(), "isValid");
        check(d.equals(new ImmutableDescriptor(new String[]{"name", "num"}, new Object[]{"value", 1})), "equals");
        check(ImmutableDescriptor.EMPTY_DESCRIPTOR.getFieldNames().length == 0, "empty");
        check(ImmutableDescriptor.union(ImmutableDescriptor.EMPTY_DESCRIPTOR, d).getFieldNames().length == 2, "union");
        boolean threw = false;
        try { d.setField("x", "y"); } catch (RuntimeOperationsException ex) { threw = true; }
        check(threw, "immutable setField");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
