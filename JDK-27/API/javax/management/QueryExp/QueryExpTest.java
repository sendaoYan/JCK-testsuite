/*
 * @test
 * @summary javax.management.QueryExp conformance: white-box structure + light behavior.
 * @run main QueryExpTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class QueryExpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.QueryExp");
        check(c.isInterface(), "is interface");
        check(java.io.Serializable.class.isAssignableFrom(c), "extends Serializable");
        check(hasMethod(c, "apply"), "has apply");
        check(hasMethod(c, "setMBeanServer"), "has setMBeanServer");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        // An anonymous implementation is callable.
        final boolean[] called = { false };
        QueryExp exp = new QueryExp() {
            public boolean apply(ObjectName name) { called[0] = true; return "d:k=v".equals(name.getCanonicalName()); }
            public void setMBeanServer(MBeanServer s) { }
        };
        check(!exp.apply(new ObjectName("d:k=w")), "apply returns false for non-matching");
        check(exp.apply(new ObjectName("d:k=v")), "apply returns true for matching");
        check(called[0], "apply was invoked");

        exp.setMBeanServer(null);
        check(true, "setMBeanServer callable");

        // ObjectName is itself a QueryExp whose apply matches object-name patterns.
        QueryExp pattern = new ObjectName("d:k=*");
        check(pattern.apply(new ObjectName("d:k=x")), "ObjectName as QueryExp applies pattern");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
