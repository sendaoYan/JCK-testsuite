/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanParameterInfo conformance: white-box structure + black-box behavior.
 * @run main OpenMBeanParameterInfoTest
 */

import javax.management.openmbean.*;
import javax.management.*;
import java.lang.reflect.*;
import java.util.*;
public class OpenMBeanParameterInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanParameterInfo");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "getDescription"), "getDescription");
        check(hasMethod(c, "getOpenType"), "getOpenType");
        check(hasMethod(c, "getDefaultValue"), "getDefaultValue");
        check(hasMethod(c, "getLegalValues"), "getLegalValues");
        check(hasMethod(c, "getMinValue"), "getMinValue");
        check(hasMethod(c, "getMaxValue"), "getMaxValue");
        check(hasMethod(c, "hasDefaultValue"), "hasDefaultValue");
        check(hasMethod(c, "hasLegalValues"), "hasLegalValues");
        check(hasMethod(c, "hasMinValue"), "hasMinValue");
        check(hasMethod(c, "hasMaxValue"), "hasMaxValue");
        check(hasMethod(c, "isValue"), "isValue");

    }

    static void testBlackBox() throws Exception {
        OpenMBeanParameterInfo p = new OpenMBeanParameterInfoSupport("p", "param", SimpleType.INTEGER, 5);
        check(p.getName().equals("p"), "name");
        check(p.getDescription().equals("param"), "description");
        check(p.getOpenType() == SimpleType.INTEGER, "open type");
        check(p.hasDefaultValue(), "has default");
        check(p.getDefaultValue().equals(5), "default value");
        check(p.isValue(5), "isValue 5");
        check(!p.isValue("x"), "isValue false for wrong type");
        check(!p.hasLegalValues(), "no legal values");
        check(p.equals(p), "equals");
        check(p.hashCode() == p.hashCode(), "hashCode");
        check(p.toString() != null, "toString");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
