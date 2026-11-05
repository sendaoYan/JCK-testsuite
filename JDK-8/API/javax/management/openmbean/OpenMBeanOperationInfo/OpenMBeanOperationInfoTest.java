/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanOperationInfo conformance: interface contract via Support implementation, exact method set, no public fields.
 * @run main OpenMBeanOperationInfoTest
 */

import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanConstructorInfo;
import javax.management.openmbean.OpenMBeanConstructorInfoSupport;
import javax.management.openmbean.OpenMBeanInfo;
import javax.management.openmbean.OpenMBeanInfoSupport;
import javax.management.openmbean.OpenMBeanOperationInfo;
import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfo;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.SimpleType;

public class OpenMBeanOperationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    interface ThrowingCall { void run() throws Throwable; }
    static Object roundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
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

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanOperationInfo");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "getReturnOpenType"), "declares getReturnOpenType");

        // ---- deepened: batch g ----
        testStructure();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: exact structure (method set from javap, no public fields) ----
    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanOperationInfo");
        check(c.isInterface(), "is interface");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public interface");
        check(c.getFields().length == 0, "no public fields");
        checkMethodSet(c, new String[] {"getName/0", "getDescription/0", "getSignature/0", "getImpact/0", "getReturnType/0", "getReturnOpenType/0", "equals/1", "hashCode/0", "toString/0"});

    }

    // ---- deepened: every method called through the interface reference on the Support impl ----
    static void testContract() throws Exception {
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
        // void operation
        OpenMBeanOperationInfo v = new OpenMBeanOperationInfoSupport("reset", "d", null, SimpleType.VOID, MBeanOperationInfo.ACTION);
        check(v.getReturnOpenType() == SimpleType.VOID && "java.lang.Void".equals(v.getReturnType()), "void return via interface");
        check(v.getSignature().length == 0, "null signature via interface");
        // all four impact constants usable through the interface
        int[] impacts = { MBeanOperationInfo.INFO, MBeanOperationInfo.ACTION, MBeanOperationInfo.ACTION_INFO, MBeanOperationInfo.UNKNOWN };
        for (int i = 0; i < impacts.length; i++) {
            OpenMBeanOperationInfo oi = new OpenMBeanOperationInfoSupport("op" + i, "d", null, SimpleType.STRING, impacts[i]);
            check(oi.getImpact() == impacts[i], "impact " + impacts[i] + " via interface");
        }

    }
}
