/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanParameterInfo conformance: white-box structure.
 * @run main OpenMBeanParameterInfoTest
 */

import java.lang.reflect.Modifier;
import javax.management.openmbean.OpenMBeanParameterInfo;

public class OpenMBeanParameterInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean has(java.lang.reflect.Method[] mm, String n) {
        for (java.lang.reflect.Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanParameterInfo");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        java.lang.reflect.Method[] mm = c.getMethods();
        check(has(mm, "getDescription"), "has getDescription");
        check(has(mm, "getName"), "has getName");
        check(has(mm, "getOpenType"), "has getOpenType");
        check(has(mm, "getDefaultValue"), "has getDefaultValue");
        check(has(mm, "getLegalValues"), "has getLegalValues");
        check(has(mm, "getMinValue"), "has getMinValue");
        check(has(mm, "getMaxValue"), "has getMaxValue");
        check(has(mm, "hasDefaultValue"), "has hasDefaultValue");
        check(has(mm, "hasLegalValues"), "has hasLegalValues");
        check(has(mm, "hasMinValue"), "has hasMinValue");
        check(has(mm, "hasMaxValue"), "has hasMaxValue");
        check(has(mm, "isValue"), "has isValue");
        check(has(mm, "equals"), "has equals");
        check(has(mm, "hashCode"), "has hashCode");
        check(has(mm, "toString"), "has toString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
