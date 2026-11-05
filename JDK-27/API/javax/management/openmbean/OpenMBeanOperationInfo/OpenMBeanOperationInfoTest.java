/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanOperationInfo conformance: white-box structure + black-box behavior.
 * @run main OpenMBeanOperationInfoTest
 */

import javax.management.openmbean.*;
import javax.management.*;
import java.lang.reflect.*;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanNotificationInfo;
import java.util.*;
public class OpenMBeanOperationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanOperationInfo");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "getDescription"), "getDescription");
        check(hasMethod(c, "getSignature"), "getSignature");
        check(hasMethod(c, "getImpact"), "getImpact");
        check(hasMethod(c, "getReturnType"), "getReturnType");
        check(hasMethod(c, "getReturnOpenType"), "getReturnOpenType");

    }

    static void testBlackBox() throws Exception {
        OpenMBeanOperationInfo oi = new OpenMBeanOperationInfoSupport("op", "operation", new OpenMBeanParameterInfo[0], SimpleType.INTEGER, MBeanOperationInfo.INFO);
        check(oi.getName().equals("op"), "name");
        check(oi.getDescription().equals("operation"), "description");
        check(oi.getSignature().length == 0, "signature empty");
        check(oi.getImpact() == MBeanOperationInfo.INFO, "impact");
        check(oi.getReturnOpenType() == SimpleType.INTEGER, "return open type");
        check(oi.getReturnType().equals("java.lang.Integer"), "return type name");
        check(oi.equals(oi), "equals");
        check(oi.hashCode() == oi.hashCode(), "hashCode");
        check(oi.toString() != null, "toString");

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
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanOperationInfo");
        check(c.isInterface(), "is interface");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public interface");
        check(c.getFields().length == 0, "no public fields");
        checkMethodSet(c, new String[] {"getName/0", "getDescription/0", "getSignature/0", "getImpact/0", "getReturnType/0", "getReturnOpenType/0", "equals/1", "hashCode/0", "toString/0"});
    }

    static void testContractG() throws Exception {
        OpenMBeanParameterInfo[] sig = { new OpenMBeanParameterInfoSupport("p", "d", SimpleType.STRING) };
        OpenMBeanOperationInfo o = new OpenMBeanOperationInfoSupport("op", "od", sig, SimpleType.INTEGER, MBeanOperationInfo.ACTION);
        check("op".equals(o.getName()), "getName via interface");
        check("od".equals(o.getDescription()), "getDescription via interface");
        check(o.getSignature().length == 1 && "p".equals(o.getSignature()[0].getName()), "getSignature via interface");
        check(o.getImpact() == MBeanOperationInfo.ACTION, "getImpact via interface");
        check("java.lang.Integer".equals(o.getReturnType()), "getReturnType via interface");
        check(o.getReturnOpenType() == SimpleType.INTEGER, "getReturnOpenType via interface");
        check(o.equals(new OpenMBeanOperationInfoSupport("op", "od", sig, SimpleType.INTEGER, MBeanOperationInfo.ACTION)), "equals via interface");
        check(!o.equals(new OpenMBeanOperationInfoSupport("op", "od", sig, SimpleType.LONG, MBeanOperationInfo.ACTION)), "equals return type via interface");
        check(o.hashCode() == new OpenMBeanOperationInfoSupport("op", "od", sig, SimpleType.INTEGER, MBeanOperationInfo.ACTION).hashCode(), "hashCode via interface");
        check(o.toString().contains("name=op"), "toString via interface");
        OpenMBeanOperationInfo v = new OpenMBeanOperationInfoSupport("reset", "d", null, SimpleType.VOID, MBeanOperationInfo.ACTION);
        check(v.getReturnOpenType() == SimpleType.VOID && "java.lang.Void".equals(v.getReturnType()), "void return via interface");
        check(v.getSignature().length == 0, "null signature via interface");
        int[] impacts = { MBeanOperationInfo.INFO, MBeanOperationInfo.ACTION, MBeanOperationInfo.ACTION_INFO, MBeanOperationInfo.UNKNOWN };
        for (int i = 0; i < impacts.length; i++) {
            OpenMBeanOperationInfo oi = new OpenMBeanOperationInfoSupport("op" + i, "d", null, SimpleType.STRING, impacts[i]);
            check(oi.getImpact() == impacts[i], "impact " + impacts[i] + " via interface");
        }
    }
}
