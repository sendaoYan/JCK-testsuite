/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanAttributeInfoSupport conformance: white-box structure + black-box behavior.
 * @run main OpenMBeanAttributeInfoSupportTest
 */

import java.lang.reflect.Modifier;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

public class OpenMBeanAttributeInfoSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanAttributeInfoSupport");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(MBeanAttributeInfo.class.isAssignableFrom(c), "extends MBeanAttributeInfo");
        check(OpenMBeanAttributeInfo.class.isAssignableFrom(c), "implements OpenMBeanAttributeInfo");
        check(c.getMethod("isReadable") != null, "has isReadable");
        check(c.getMethod("isWritable") != null, "has isWritable");
        check(c.getMethod("isIs") != null, "has isIs");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        OpenMBeanAttributeInfoSupport ai = new OpenMBeanAttributeInfoSupport(
            "Name", "desc", SimpleType.STRING, true, true, false);
        check(ai.getName().equals("Name"), "name");
        check(ai.getDescription().equals("desc"), "description");
        check(ai.getOpenType() == SimpleType.STRING, "openType");
        check(ai.isReadable(), "isReadable");
        check(ai.isWritable(), "isWritable");
        check(!ai.isIs(), "isIs false");
        check(!ai.hasDefaultValue() && !ai.hasLegalValues() && !ai.hasMinValue() && !ai.hasMaxValue(), "no constraints");
        check(ai.isValue("hello"), "isValue string");
        check(!ai.isValue(5), "reject integer");

        // isIs flag propagates
        OpenMBeanAttributeInfoSupport aiIs = new OpenMBeanAttributeInfoSupport(
            "Active", "d", SimpleType.BOOLEAN, true, false, true);
        check(aiIs.isIs(), "isIs true");
        check(aiIs.isReadable() && !aiIs.isWritable(), "readable not writable");

        // default value variant
        OpenMBeanAttributeInfoSupport aiDef = new OpenMBeanAttributeInfoSupport(
            "N", "d", SimpleType.INTEGER, true, true, false, 42);
        check(aiDef.hasDefaultValue() && aiDef.getDefaultValue().equals(42), "default value");
        check(aiDef.isValue(null), "null value allowed when default set");

        // equals / hashCode
        OpenMBeanAttributeInfoSupport ai2 = new OpenMBeanAttributeInfoSupport(
            "Name", "desc", SimpleType.STRING, true, true, false);
        check(ai.equals(ai2), "equals");
        check(ai.hashCode() == ai2.hashCode(), "hashCode equal");
        check(!ai.equals(null), "equals null false");
        check(!ai.equals("x"), "equals non-info false");
        check(!ai.equals(new OpenMBeanAttributeInfoSupport("Other", "desc", SimpleType.STRING, true, true, false)), "different name");
        check(!ai.equals(new OpenMBeanAttributeInfoSupport("Name", "desc", SimpleType.STRING, false, true, false)), "different readable");

        check(ai.toString().startsWith("javax.management.openmbean.OpenMBeanAttributeInfoSupport("), "toString prefix");
        check(ai.toString().contains("name=Name"), "toString name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
