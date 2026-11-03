/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanConstructorInfo conformance: white-box structure.
 * @run main OpenMBeanConstructorInfoTest
 */

import java.lang.reflect.Modifier;

public class OpenMBeanConstructorInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean has(java.lang.reflect.Method[] mm, String n) {
        for (java.lang.reflect.Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanConstructorInfo");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        java.lang.reflect.Method[] mm = c.getMethods();
        check(has(mm, "getDescription"), "has getDescription");
        check(has(mm, "getName"), "has getName");
        check(has(mm, "getSignature"), "has getSignature");
        check(has(mm, "equals"), "has equals");
        check(has(mm, "hashCode"), "has hashCode");
        check(has(mm, "toString"), "has toString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
