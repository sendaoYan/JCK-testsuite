/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanConstructorInfo conformance: white-box structure + black-box behavior.
 * @run main OpenMBeanConstructorInfoTest
 */

import javax.management.openmbean.*;
import javax.management.*;
import java.lang.reflect.*;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanNotificationInfo;
import java.util.*;
public class OpenMBeanConstructorInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanConstructorInfo");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "getDescription"), "getDescription");
        check(hasMethod(c, "getSignature"), "getSignature");

    }

    static void testBlackBox() throws Exception {
        OpenMBeanParameterInfo[] sig = { new OpenMBeanParameterInfoSupport("p", "param", SimpleType.INTEGER) };
        OpenMBeanConstructorInfo ci = new OpenMBeanConstructorInfoSupport("ctor", "constructor", sig);
        check(ci.getName().equals("ctor"), "name");
        check(ci.getDescription().equals("constructor"), "description");
        check(ci.getSignature().length == 1, "signature length");
        check(ci.getSignature()[0].getName().equals("p"), "signature param name");
        check(ci.equals(ci), "equals");
        check(ci.hashCode() == ci.hashCode(), "hashCode");
        check(ci.toString() != null, "toString");

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
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanConstructorInfo");
        check(c.isInterface(), "is interface");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public interface");
        check(c.getFields().length == 0, "no public fields");
        checkMethodSet(c, new String[] {"getName/0", "getDescription/0", "getSignature/0", "equals/1", "hashCode/0", "toString/0"});
    }

    static void testContractG() throws Exception {
        OpenMBeanParameterInfo[] sig = { new OpenMBeanParameterInfoSupport("p", "d", SimpleType.STRING) };
        OpenMBeanConstructorInfo k = new OpenMBeanConstructorInfoSupport("C", "cd", sig);
        check("C".equals(k.getName()), "getName via interface");
        check("cd".equals(k.getDescription()), "getDescription via interface");
        check(k.getSignature().length == 1 && "p".equals(k.getSignature()[0].getName()), "getSignature via interface");
        check(k.equals(new OpenMBeanConstructorInfoSupport("C", "cd", sig)), "equals via interface");
        check(!k.equals(new OpenMBeanConstructorInfoSupport("D", "cd", sig)), "equals name via interface");
        check(!k.equals(new OpenMBeanConstructorInfoSupport("C", "cd", null)), "equals signature via interface");
        check(k.hashCode() == new OpenMBeanConstructorInfoSupport("C", "cd", sig).hashCode(), "hashCode via interface");
        check(k.toString().contains("name=C"), "toString via interface");
        OpenMBeanConstructorInfo n = new OpenMBeanConstructorInfoSupport("N", "d", null);
        check(n.getSignature() != null && n.getSignature().length == 0, "null signature via interface");
        check(n.equals(new OpenMBeanConstructorInfoSupport("N", "other", null)), "description not in equals via interface");
    }
}
