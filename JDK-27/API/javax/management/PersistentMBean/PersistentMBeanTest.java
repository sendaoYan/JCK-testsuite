/*
 * @test
 * @summary javax.management.PersistentMBean conformance: white-box structure + light behavior.
 * @run main PersistentMBeanTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class PersistentMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.PersistentMBean");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "load"), "has load");
        check(hasMethod(c, "store"), "has store");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        final List<String> calls = new ArrayList<String>();
        PersistentMBean pmb = new PersistentMBean() {
            public void load() throws MBeanException, RuntimeOperationsException, InstanceNotFoundException {
                calls.add("load");
            }
            public void store() throws MBeanException, RuntimeOperationsException, InstanceNotFoundException {
                calls.add("store");
            }
        };
        pmb.load();
        pmb.store();
        check(calls.contains("load") && calls.contains("store"), "load/store callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
