/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanAttributeInfo conformance: interface contract via Support implementation, exact method set, no public fields.
 * @run main OpenMBeanAttributeInfoTest
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

public class OpenMBeanAttributeInfoTest {
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
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanAttributeInfo");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("javax.management.openmbean.OpenMBeanParameterInfo").isAssignableFrom(c), "implements/extends javax.management.openmbean.OpenMBeanParameterInfo");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        check(hasMethod(c, "isIs"), "declares isIs");

        // ---- deepened: batch g ----
        testStructure();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: exact structure (method set from javap, no public fields) ----
    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanAttributeInfo");
        check(c.isInterface(), "is interface");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public interface");
        check(c.getFields().length == 0, "no public fields");
        checkMethodSet(c, new String[] {"getName/0", "getDescription/0", "getOpenType/0", "getDefaultValue/0", "getLegalValues/0", "getMinValue/0", "getMaxValue/0", "hasDefaultValue/0", "hasLegalValues/0", "hasMinValue/0", "hasMaxValue/0", "isValue/1", "isReadable/0", "isWritable/0", "isIs/0", "equals/1", "hashCode/0", "toString/0"});
        check(javax.management.openmbean.OpenMBeanParameterInfo.class.isAssignableFrom(c), "extends OpenMBeanParameterInfo");

    }

    // ---- deepened: every method called through the interface reference on the Support impl ----
    static void testContract() throws Exception {
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
        // legal-values form through the interface
        OpenMBeanAttributeInfo l = new OpenMBeanAttributeInfoSupport("l", "d", SimpleType.STRING, true, true, false, "m", new String[] {"m", "n"});
        check(l.hasLegalValues() && l.getLegalValues().contains("n"), "legal values via interface");
        check(l.isValue("m") && !l.isValue("z"), "legal isValue via interface");
        // isIs form through the interface
        OpenMBeanAttributeInfo is = new OpenMBeanAttributeInfoSupport("ready", "d", SimpleType.BOOLEAN, true, false, true);
        check(is.isIs() && is.isReadable() && !is.isWritable(), "isIs form via interface");
        // also usable as OpenMBeanParameterInfo (sub-interface)
        OpenMBeanParameterInfo asParam = a;
        check(asParam.getOpenType() == SimpleType.INTEGER, "attribute as parameter view");

    }
}
