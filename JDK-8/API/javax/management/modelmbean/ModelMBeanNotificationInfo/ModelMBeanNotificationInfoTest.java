/*
 * @test
 * @summary javax.management.modelmbean.ModelMBeanNotificationInfo conformance: concrete class reflection.
 * @run main ModelMBeanNotificationInfoTest
 */

import javax.management.Descriptor;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
public class ModelMBeanNotificationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.ModelMBeanNotificationInfo");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.MBeanNotificationInfo".equals(c.getSuperclass().getName()), "superclass javax.management.MBeanNotificationInfo");
        check(Class.forName("javax.management.DescriptorAccess").isAssignableFrom(c), "implements/extends javax.management.DescriptorAccess");
        check(hasMethod(c, "clone"), "declares clone");
        check(hasMethod(c, "getDescriptor"), "declares getDescriptor");
        check(hasMethod(c, "setDescriptor"), "declares setDescriptor");
        testDeepNotifTypesAndDefaults();
        testDeepSetDescriptorRules();
        testDeepCopyCloneSerializationEquals();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch h): type array semantics, descriptor rules, clone/serialization ----

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrowsROE(ThrowingRunnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected RuntimeOperationsException but nothing was thrown");
        } catch (javax.management.RuntimeOperationsException roe) {
            check(roe.getMessage() != null, msg + ": message non-null");
        } catch (AssertionError ae) {
            throw ae;
        } catch (Throwable t) {
            throw new AssertionError(msg + ": expected RuntimeOperationsException but got " + t.getClass().getName(), t);
        }
    }

    static void testDeepNotifTypesAndDefaults() {
        ModelMBeanNotificationInfo n = new ModelMBeanNotificationInfo(
                new String[]{"a.b", "c.d"}, "N", "d");
        // types content and defensive copy
        String[] types = n.getNotifTypes();
        check(types.length == 2 && "a.b".equals(types[0]) && "c.d".equals(types[1]), "getNotifTypes content");
        types[0] = "mutated";
        check("a.b".equals(n.getNotifTypes()[0]), "getNotifTypes returns a defensive copy");
        // name and description getters
        check("N".equals(n.getName()) && "d".equals(n.getDescription()), "name and description");
        // default descriptor: name, descriptorType=notification, severity=6, displayName
        Descriptor d = n.getDescriptor();
        check("N".equals(d.getFieldValue("name")), "default descriptor name");
        check("notification".equals(d.getFieldValue("descriptorType")), "default descriptorType=notification");
        check("6".equals(String.valueOf(d.getFieldValue("severity"))), "default severity=6");
        check("N".equals(d.getFieldValue("displayName")), "default displayName");
        // descriptor form preserves custom fields
        ModelMBeanNotificationInfo nd = new ModelMBeanNotificationInfo(
                new String[]{"a.b"}, "N", "d",
                new DescriptorSupport(new String[]{"name", "descriptorType", "severity"},
                        new Object[]{"N", "notification", "3"}));
        check("3".equals(String.valueOf(nd.getDescriptor().getFieldValue("severity"))),
                "descriptor form preserves severity");
        // empty types array is allowed
        ModelMBeanNotificationInfo empty = new ModelMBeanNotificationInfo(new String[0], "N", "d");
        check(empty.getNotifTypes().length == 0, "empty notif types allowed");
    }

    static void testDeepSetDescriptorRules() {
        ModelMBeanNotificationInfo n = new ModelMBeanNotificationInfo(new String[]{"a.b"}, "N", "d");
        n.setDescriptor(new DescriptorSupport(new String[]{"name", "descriptorType", "k"},
                new Object[]{"N", "notification", "v"}));
        check("v".equals(n.getDescriptor().getFieldValue("k")), "setDescriptor persists");
        checkThrowsROE(new ThrowingRunnable() { public void run() {
            new ModelMBeanNotificationInfo(new String[]{"a.b"}, "N", "d").setDescriptor(
                    new DescriptorSupport(new String[]{"name", "descriptorType"},
                            new Object[]{"Other", "notification"}));
        } }, "setDescriptor with wrong name");
        checkThrowsROE(new ThrowingRunnable() { public void run() {
            new ModelMBeanNotificationInfo(new String[]{"a.b"}, "N", "d").setDescriptor(
                    new DescriptorSupport(new String[]{"name", "descriptorType"},
                            new Object[]{"N", "attribute"}));
        } }, "setDescriptor with wrong descriptorType");
        n.setDescriptor(null);
        check("6".equals(String.valueOf(n.getDescriptor().getFieldValue("severity"))),
                "setDescriptor(null) resets to defaults");
    }

    static void testDeepCopyCloneSerializationEquals() throws Exception {
        ModelMBeanNotificationInfo n = new ModelMBeanNotificationInfo(new String[]{"a.b", "c.d"}, "N", "d");
        // copy constructor
        ModelMBeanNotificationInfo cp = new ModelMBeanNotificationInfo(n);
        check(cp.equals(n), "copy ctor equals");
        // clone
        ModelMBeanNotificationInfo cl = (ModelMBeanNotificationInfo) n.clone();
        check(cl != n && cl.equals(n), "clone distinct but equal");
        // equals matrix: same types true; different or superset types false
        check(!n.equals(new ModelMBeanNotificationInfo(new String[]{"c.d", "a.b"}, "N", "d")),
                "type order affects equality (arrays compared positionally)");
        check(n.equals(new ModelMBeanNotificationInfo(new String[]{"a.b", "c.d"}, "N", "d")),
                "same types in same order are equal");
        check(!n.equals(new ModelMBeanNotificationInfo(new String[]{"x.y"}, "N", "d")),
                "different types not equal");
        check(!n.equals(new ModelMBeanNotificationInfo(new String[]{"a.b"}, "N", "d")),
                "subset of types not equal");
        check(!n.equals(new ModelMBeanNotificationInfo(new String[]{"a.b", "c.d", "e.f"}, "N", "d")),
                "superset of types not equal");
        // serialization round-trip
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(n);
        oos.close();
        Object back = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof ModelMBeanNotificationInfo, "round-trip class preserved");
        check(back.equals(n), "round-trip equals");
        check(java.util.Arrays.equals(((ModelMBeanNotificationInfo) back).getNotifTypes(),
                new String[]{"a.b", "c.d"}), "round-trip keeps notif types");
        // equality against plain parent class instance is false
        javax.management.MBeanNotificationInfo plain = new javax.management.MBeanNotificationInfo(
                new String[]{"a.b", "c.d"}, "N", "d");
        check(!n.equals(plain) && !plain.equals(n), "not equal to plain MBeanNotificationInfo");
        check(n.toString().contains("N"), "toString contains name");
    }
}
