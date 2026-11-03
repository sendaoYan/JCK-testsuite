/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanAttributeInfo conformance: white-box structure + black-box behavior.
 * @run main OpenMBeanAttributeInfoTest
 */

import javax.management.openmbean.*;
import javax.management.*;
import java.lang.reflect.*;
import java.util.*;
public class OpenMBeanAttributeInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanAttributeInfo");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(OpenMBeanParameterInfo.class.isAssignableFrom(c), "extends OpenMBeanParameterInfo");
        check(hasMethod(c, "getOpenType"), "getOpenType");
        check(hasMethod(c, "getDefaultValue"), "getDefaultValue");
        check(hasMethod(c, "getLegalValues"), "getLegalValues");
        check(hasMethod(c, "getMinValue"), "getMinValue");
        check(hasMethod(c, "getMaxValue"), "getMaxValue");
        check(hasMethod(c, "hasDefaultValue"), "hasDefaultValue");
        check(hasMethod(c, "isValue"), "isValue");
        check(hasMethod(c, "isReadable"), "isReadable");
        check(hasMethod(c, "isWritable"), "isWritable");
        check(hasMethod(c, "isIs"), "isIs");

    }

    static void testBlackBox() throws Exception {
        OpenType<String> t = SimpleType.STRING;
        OpenMBeanAttributeInfo info = new OpenMBeanAttributeInfoSupport("name", "desc", t, true, true, false);
        check(info.getName().equals("name"), "name");
        check(info.getDescription().equals("desc"), "description");
        check(info.isReadable(), "isReadable");
        check(info.isWritable(), "isWritable");
        check(!info.isIs(), "isIs false");
        check(info.getOpenType() == SimpleType.STRING, "open type");
        check(!info.hasDefaultValue(), "no default");
        check(info.getDefaultValue() == null, "null default");
        check(!info.hasLegalValues(), "no legal values");
        check(!info.hasMinValue(), "no min");
        check(!info.hasMaxValue(), "no max");
        check(info.isValue("x"), "isValue true for valid String");
        check(info.equals(info), "equals");
        check(info.hashCode() == info.hashCode(), "hashCode");
        check(info.toString() != null, "toString");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
