/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanParameterInfo conformance: white-box structure + black-box behavior.
 * @run main OpenMBeanParameterInfoTest
 */

import javax.management.openmbean.*;
import javax.management.*;
import java.lang.reflect.*;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanNotificationInfo;
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
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanParameterInfo");
        check(c.isInterface(), "is interface");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public interface");
        check(c.getFields().length == 0, "no public fields");
        checkMethodSet(c, new String[] {"getName/0", "getDescription/0", "getOpenType/0", "getDefaultValue/0", "getLegalValues/0", "getMinValue/0", "getMaxValue/0", "hasDefaultValue/0", "hasLegalValues/0", "hasMinValue/0", "hasMaxValue/0", "isValue/1", "equals/1", "hashCode/0", "toString/0"});
        check(!javax.management.openmbean.OpenMBeanAttributeInfo.class.isAssignableFrom(c), "parameter info does not extend attribute info");
    }

    static void testContractG() throws Exception {
        OpenMBeanParameterInfo p = new OpenMBeanParameterInfoSupport("p", "pd", SimpleType.INTEGER, 5, 1, 10);
        check("p".equals(p.getName()), "getName via interface");
        check("pd".equals(p.getDescription()), "getDescription via interface");
        check(p.getOpenType() == SimpleType.INTEGER, "getOpenType via interface");
        check(Integer.valueOf(5).equals(p.getDefaultValue()), "getDefaultValue via interface");
        check(p.hasDefaultValue(), "hasDefaultValue via interface");
        check(Integer.valueOf(1).equals(p.getMinValue()), "getMinValue via interface");
        check(Integer.valueOf(10).equals(p.getMaxValue()), "getMaxValue via interface");
        check(p.hasMinValue() && p.hasMaxValue(), "hasMinValue/hasMaxValue via interface");
        check(p.getLegalValues() == null && !p.hasLegalValues(), "legal absent via interface");
        check(p.isValue(3) && !p.isValue(33), "isValue via interface");
        check(p.isValue(null), "isValue null true when default present via interface");
        check(p.equals(new OpenMBeanParameterInfoSupport("p", "pd", SimpleType.INTEGER, 5, 1, 10)), "equals via interface");
        check(!p.equals(new OpenMBeanParameterInfoSupport("p", "pd", SimpleType.INTEGER, 6, 1, 10)), "equals default via interface");
        check(p.hashCode() == new OpenMBeanParameterInfoSupport("p", "pd", SimpleType.INTEGER, 5, 1, 10).hashCode(), "hashCode via interface");
        check(p.toString().contains("name=p"), "toString via interface");
        OpenMBeanParameterInfo l = new OpenMBeanParameterInfoSupport("l", "d", SimpleType.STRING, "m", new String[] {"m", "n"});
        check(l.hasLegalValues() && l.getLegalValues().size() == 2, "legal via interface");
        check(l.isValue("n") && !l.isValue("x"), "legal isValue via interface");
        OpenMBeanParameterInfo plain = new OpenMBeanParameterInfoSupport("q", "d", SimpleType.BOOLEAN);
        check(!plain.hasDefaultValue() && !plain.hasMinValue() && !plain.hasMaxValue() && !plain.hasLegalValues(),
            "plain form no constraints via interface");
        check(plain.isValue(Boolean.TRUE) && !plain.isValue("yes"), "plain isValue via interface");
        check(!plain.isValue(null), "isValue null false without default via interface");
    }
}
