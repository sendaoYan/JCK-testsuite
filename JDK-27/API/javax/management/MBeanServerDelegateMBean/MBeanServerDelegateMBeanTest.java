/*
 * @test
 * @summary javax.management.MBeanServerDelegateMBean conformance: white-box structure + light behavior.
 * @run main MBeanServerDelegateMBeanTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class MBeanServerDelegateMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanServerDelegateMBean");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "getMBeanServerId"), "has getMBeanServerId");
        check(hasMethod(c, "getSpecificationName"), "has getSpecificationName");
        check(hasMethod(c, "getSpecificationVersion"), "has getSpecificationVersion");
        check(hasMethod(c, "getSpecificationVendor"), "has getSpecificationVendor");
        check(hasMethod(c, "getImplementationName"), "has getImplementationName");
        check(hasMethod(c, "getImplementationVersion"), "has getImplementationVersion");
        check(hasMethod(c, "getImplementationVendor"), "has getImplementationVendor");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        MBeanServerDelegateMBean m = new MBeanServerDelegate();
        check(m.getMBeanServerId() != null, "getMBeanServerId non-null");
        check("Java Management Extensions".equals(m.getSpecificationName()), "getSpecificationName");
        check("1.4".equals(m.getSpecificationVersion()), "getSpecificationVersion");
        check("Oracle Corporation".equals(m.getSpecificationVendor()), "getSpecificationVendor");
        check("JMX".equals(m.getImplementationName()), "getImplementationName");
        check(m.getImplementationVersion() != null, "getImplementationVersion non-null");
        check("Oracle Corporation".equals(m.getImplementationVendor()), "getImplementationVendor");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
