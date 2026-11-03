/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanAttributeInfo conformance: white-box structure.
 * @run main OpenMBeanAttributeInfoTest
 */

import java.lang.reflect.Modifier;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanParameterInfo;

public class OpenMBeanAttributeInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean has(java.lang.reflect.Method[] mm, String n) {
        for (java.lang.reflect.Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanAttributeInfo");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(OpenMBeanParameterInfo.class.isAssignableFrom(c), "extends OpenMBeanParameterInfo");
        java.lang.reflect.Method[] mm = c.getMethods();
        check(has(mm, "isReadable"), "has isReadable");
        check(has(mm, "isWritable"), "has isWritable");
        check(has(mm, "isIs"), "has isIs");
        check(has(mm, "equals"), "has equals");
        check(has(mm, "hashCode"), "has hashCode");
        check(has(mm, "toString"), "has toString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
