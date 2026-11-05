/*
 * @test
 * @summary javax.management.modelmbean.ModelMBeanNotificationInfo conformance: white-box structure + black-box behavior.
 * @run main ModelMBeanNotificationInfoTest
 */

import javax.management.Descriptor;
import javax.management.MBeanNotificationInfo;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;

public class ModelMBeanNotificationInfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.ModelMBeanNotificationInfo");
        check(MBeanNotificationInfo.class.isAssignableFrom(c), "extends MBeanNotificationInfo");
        check(javax.management.DescriptorAccess.class.isAssignableFrom(c), "implements DescriptorAccess");
        check(c.getConstructors().length >= 3, "multiple public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ModelMBeanNotificationInfo ni = new ModelMBeanNotificationInfo(
                new String[]{"a.b", "c.d"}, "javax.management.Notification", "desc");
        check("javax.management.Notification".equals(ni.getName()), "name");
        check("desc".equals(ni.getDescription()), "description");
        check(ni.getNotifTypes().length == 2, "notif types length");
        check("a.b".equals(ni.getNotifTypes()[0]), "notif type 0");
        check("c.d".equals(ni.getNotifTypes()[1]), "notif type 1");

        Descriptor d = ni.getDescriptor();
        check(d != null, "descriptor non-null");
        check("javax.management.Notification".equals(d.getFieldValue("name")), "descriptor name");
        check("notification".equals(d.getFieldValue("descriptorType")), "descriptor type");

        // Descriptor-bearing constructor
        DescriptorSupport custom = new DescriptorSupport(
                "name=N2", "descriptorType=notification", "extra=q");
        ModelMBeanNotificationInfo ni2 = new ModelMBeanNotificationInfo(
                new String[]{"x.y"}, "N2", "d2", custom);
        check("q".equals(ni2.getDescriptor().getFieldValue("extra")), "descriptor ctor extra");

        // copy constructor
        ModelMBeanNotificationInfo copy = new ModelMBeanNotificationInfo(ni);
        check("javax.management.Notification".equals(copy.getName()), "copy ctor name");
        check(ni.equals(copy), "copy equals original");

        // clone
        ModelMBeanNotificationInfo cloned = (ModelMBeanNotificationInfo) ni.clone();
        check(ni.equals(cloned), "clone equals original");
        check(cloned.getNotifTypes().length == 2, "clone notif types");

        // setDescriptor (matching name + descriptorType)
        DescriptorSupport nd = new DescriptorSupport(
                "name=javax.management.Notification", "descriptorType=notification", "extra=r");
        ni.setDescriptor(nd);
        check("r".equals(ni.getDescriptor().getFieldValue("extra")), "setDescriptor updates descriptor");

        check(ni.toString().contains("javax.management.Notification"), "toString contains name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
