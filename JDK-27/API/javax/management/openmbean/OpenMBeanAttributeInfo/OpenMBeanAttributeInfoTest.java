/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanAttributeInfo conformance: white-box structure + black-box behavior.
 * @run main OpenMBeanAttributeInfoTest
 */

import javax.management.openmbean.*;
import javax.management.*;
import java.lang.reflect.*;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanNotificationInfo;
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
        // ---- deepened: batch g ----
        testStructureG();
        testContractG();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepened (batch g) =================
    static java.util.Set<String> ownMethods(Class<?> iface) throws Exception {
        java.util.Set<String> names = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : iface.getMethods()) {
            if (m.getDeclaringClass() == Object.class) continue;
            names.add(m.getName() + "/" + m.getParameterTypes().length);
        }
        return names;
    }
    static void checkMethodSet(Class<?> iface, String[] expected) {
        java.util.Set<String> got = null;
        try { got = ownMethods(iface); } catch (Exception e) { throw new AssertionError(e); }
        java.util.Set<String> exp = new java.util.TreeSet<String>();
        for (String e : expected) exp.add(e);
        if (!got.equals(exp)) {
            java.util.Set<String> missing = new java.util.TreeSet<String>(exp); missing.removeAll(got);
            java.util.Set<String> extra = new java.util.TreeSet<String>(got); extra.removeAll(exp);
            throw new AssertionError(iface.getName() + " method set mismatch, missing=" + missing + " extra=" + extra);
        }
        passed++;
    }

    static void testStructureG() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanAttributeInfo");
        check(c.isInterface(), "is interface");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public interface");
        check(c.getFields().length == 0, "no public fields");
        checkMethodSet(c, new String[] {"getName/0", "getDescription/0", "getOpenType/0", "getDefaultValue/0", "getLegalValues/0", "getMinValue/0", "getMaxValue/0", "hasDefaultValue/0", "hasLegalValues/0", "hasMinValue/0", "hasMaxValue/0", "isValue/1", "isReadable/0", "isWritable/0", "isIs/0", "equals/1", "hashCode/0", "toString/0"});
        check(javax.management.openmbean.OpenMBeanParameterInfo.class.isAssignableFrom(c), "extends OpenMBeanParameterInfo");
    }

    static void testContractG() throws Exception {
        OpenMBeanAttributeInfo a = new OpenMBeanAttributeInfoSupport("a", "ad", SimpleType.INTEGER, true, true, false, 5, 1, 10);
        check("a".equals(a.getName()), "getName via interface");
        check("ad".equals(a.getDescription()), "getDescription via interface");
        check(a.getOpenType() == SimpleType.INTEGER, "getOpenType via interface");
        check(Integer.valueOf(5).equals(a.getDefaultValue()), "getDefaultValue via interface");
        check(a.hasDefaultValue(), "hasDefaultValue via interface");
        check(Integer.valueOf(1).equals(a.getMinValue()) && Integer.valueOf(10).equals(a.getMaxValue()), "min/max via interface");
        check(a.hasMinValue() && a.hasMaxValue(), "hasMin/hasMax via interface");
        check(a.getLegalValues() == null && !a.hasLegalValues(), "legal absent via interface");
        check(a.isReadable() && a.isWritable() && !a.isIs(), "access flags via interface");
        check(a.isValue(7) && !a.isValue(77), "isValue via interface");
        check(a.equals(new OpenMBeanAttributeInfoSupport("a", "ad", SimpleType.INTEGER, true, true, false, 5, 1, 10)), "equals via interface");
        check(!a.equals(new OpenMBeanAttributeInfoSupport("a", "ad", SimpleType.INTEGER, false, true, false, 5, 1, 10)), "equals flags via interface");
        check(a.hashCode() == new OpenMBeanAttributeInfoSupport("a", "ad", SimpleType.INTEGER, true, true, false, 5, 1, 10).hashCode(), "hashCode via interface");
        check(a.toString().contains("name=a"), "toString via interface");
        OpenMBeanAttributeInfo l = new OpenMBeanAttributeInfoSupport("l", "d", SimpleType.STRING, true, true, false, "m", new String[] {"m", "n"});
        check(l.hasLegalValues() && l.getLegalValues().contains("n"), "legal values via interface");
        check(l.isValue("m") && !l.isValue("z"), "legal isValue via interface");
        OpenMBeanAttributeInfo is = new OpenMBeanAttributeInfoSupport("ready", "d", SimpleType.BOOLEAN, true, false, true);
        check(is.isIs() && is.isReadable() && !is.isWritable(), "isIs form via interface");
        OpenMBeanParameterInfo asParam = a;
        check(asParam.getOpenType() == SimpleType.INTEGER, "attribute as parameter view");
    }
}
