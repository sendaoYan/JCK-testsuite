/*
 * @test
 * @summary javax.management.openmbean.CompositeType conformance: JMX API type verification.
 * @run main CompositeTypeTest
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

public class CompositeTypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        String[] names = {"name", "value"};
        String[] descs = {"name desc", "value desc"};
        OpenType<?>[] types = {SimpleType.STRING, SimpleType.INTEGER};
        CompositeType ct = new CompositeType("MyType", "desc", names, descs, types);
        check(ct.getTypeName().equals("MyType"), "typeName");
        check(ct.getClassName().equals("javax.management.openmbean.CompositeData"), "className");
        check(ct.containsKey("name"), "containsKey");
        check(ct.getType("name") == SimpleType.STRING, "getType");
        check(ct.getDescription("value").equals("value desc"), "getDescription");
        check(ct.keySet().size() == 2, "keySet");
        check(!ct.isArray(), "not array");
        CompositeDataSupport cd = new CompositeDataSupport(ct, names, new Object[]{"n", 5});
        check(ct.isValue(cd), "isValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
