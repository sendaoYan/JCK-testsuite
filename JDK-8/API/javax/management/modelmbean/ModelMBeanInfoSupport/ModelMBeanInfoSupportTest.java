/*
 * @test
 * @summary javax.management.modelmbean.ModelMBeanInfoSupport conformance: concrete class reflection.
 * @run main ModelMBeanInfoSupportTest
 */

import javax.management.Descriptor;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

public class ModelMBeanInfoSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.ModelMBeanInfoSupport");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.MBeanInfo".equals(c.getSuperclass().getName()), "superclass javax.management.MBeanInfo");
        check(Class.forName("javax.management.modelmbean.ModelMBeanInfo").isAssignableFrom(c), "implements/extends javax.management.modelmbean.ModelMBeanInfo");
        check(hasMethod(c, "clone"), "declares clone");
        check(hasMethod(c, "getAttribute"), "declares getAttribute");
        check(hasMethod(c, "getConstructor"), "declares getConstructor");
        testDefaultMBeanDescriptorFields();
        testDescriptorRetrievalMatrix();
        testDescriptorMutation();
        testEqualsHashCodeAndSharing();
        testSerializationRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch h): descriptor matrix, defensive copies, equals/serialization ----

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

    public static class MmbRes8 {
        private int count = 3;
        public int getCount() { return count; }
        public void setCount(int v) { this.count = v; }
        public MmbRes8() {}
        public MmbRes8(int seed) { this.count = seed; }
    }

    static ModelMBeanInfoSupport buildFullInfo() throws Exception {
        ModelMBeanAttributeInfo attr = new ModelMBeanAttributeInfo("Count", "int", "count attr",
                true, true, false, new DescriptorSupport(new String[]{"name", "descriptorType", "getMethod", "setMethod"},
                        new Object[]{"Count", "attribute", "getCount", "setCount"}));
        ModelMBeanConstructorInfo ctor = new ModelMBeanConstructorInfo("ctor", MmbRes8.class.getConstructor());
        ModelMBeanOperationInfo op = new ModelMBeanOperationInfo("op", MmbRes8.class.getMethod("getCount"));
        ModelMBeanNotificationInfo notif = new ModelMBeanNotificationInfo(new String[]{"jck.type"}, "N", "d");
        return new ModelMBeanInfoSupport(MmbRes8.class.getName(), "full info",
                new ModelMBeanAttributeInfo[]{attr}, new ModelMBeanConstructorInfo[]{ctor},
                new ModelMBeanOperationInfo[]{op}, new ModelMBeanNotificationInfo[]{notif});
    }

    static void testDefaultMBeanDescriptorFields() throws Exception {
        // null Descriptor (and no-descriptor) constructor builds the documented default MBean descriptor
        ModelMBeanInfoSupport m = new ModelMBeanInfoSupport("C", "d", null, null, null, null);
        Descriptor md = m.getMBeanDescriptor();
        check("mbean".equals(md.getFieldValue("descriptorType")), "default descriptorType=mbean");
        check("C".equals(md.getFieldValue("name")), "default name=className");
        check("C".equals(md.getFieldValue("displayName")), "default displayName=className");
        check("never".equalsIgnoreCase(String.valueOf(md.getFieldValue("persistPolicy"))),
                "default persistPolicy=never");
        check("F".equals(md.getFieldValue("log")), "default log=F");
        check("1".equals(String.valueOf(md.getFieldValue("visibility"))), "default visibility=1");
        // explicit descriptor constructor is preserved (with defaulted missing fields)
        DescriptorSupport custom = new DescriptorSupport(new String[]{"name", "descriptorType", "x"},
                new Object[]{"C", "mbean", "1"});
        ModelMBeanInfoSupport m2 = new ModelMBeanInfoSupport("C", "d", null, null, null, null, custom);
        check("1".equals(m2.getMBeanDescriptor().getFieldValue("x")), "explicit mbean descriptor preserved");
        // copy constructor from ModelMBeanInfo
        ModelMBeanInfoSupport m3 = new ModelMBeanInfoSupport((ModelMBeanInfo) m2);
        check("1".equals(m3.getMBeanDescriptor().getFieldValue("x")), "copy ctor preserves mbean descriptor");
    }

    static void testDescriptorRetrievalMatrix() throws Exception {
        ModelMBeanInfoSupport m = buildFullInfo();
        // getDescriptors per type and "all" (sum of all entries plus the mbean descriptor)
        check(m.getDescriptors("attribute").length == 1, "getDescriptors(attribute)");
        check(m.getDescriptors("operation").length == 1, "getDescriptors(operation)");
        check(m.getDescriptors("constructor").length == 1, "getDescriptors(constructor)");
        check(m.getDescriptors("notification").length == 1, "getDescriptors(notification)");
        check(m.getDescriptors("mbean").length == 1, "getDescriptors(mbean)");
        check(m.getDescriptors("all").length == 5, "getDescriptors(all) = 4 entries + mbean descriptor");
        check(m.getDescriptors("ATTRIBUTE").length == 1, "getDescriptors type is case-insensitive");
        checkThrowsROE(new ThrowingRunnable() { public void run() throws Exception {
            buildFullInfo().getDescriptors("bogus");
        } }, "getDescriptors(bogus type)");
        // typed getDescriptor
        check("getCount".equals(m.getDescriptor("Count", "attribute").getFieldValue("getMethod")),
                "getDescriptor(name,attribute) returns attribute descriptor");
        check("mbean".equals(m.getDescriptor(MmbRes8.class.getName(), "mbean").getFieldValue("descriptorType")),
                "getDescriptor(name,mbean)");
        check(m.getDescriptor("nosuch", "attribute") == null, "getDescriptor unknown name returns null");
        // single-arg getDescriptor finds the attribute descriptor across types
        check("attribute".equals(m.getDescriptor("Count").getFieldValue("descriptorType")),
                "single-arg getDescriptor resolves attribute descriptor");
        // lookup helpers
        check(m.getAttribute("Count") instanceof ModelMBeanAttributeInfo, "getAttribute typed");
        check(m.getAttribute("nosuch") == null, "getAttribute unknown -> null");
        checkThrowsROE(new ThrowingRunnable() { public void run() throws Exception {
            buildFullInfo().getAttribute(null);
        } }, "getAttribute(null)");
        check(m.getOperation("getCount") != null && m.getOperation("nosuch") == null,
                "getOperation hit and miss");
        check(m.getConstructor(MmbRes8.class.getName()) != null && m.getConstructor("nosuch") == null,
                "getConstructor hit and miss");
        check(m.getNotification("N") != null && m.getNotification("nosuch") == null,
                "getNotification hit and miss");
        // MBeanInfo-level getters return defensive copies
        javax.management.MBeanAttributeInfo[] arr = m.getAttributes();
        check(arr[0] instanceof ModelMBeanAttributeInfo, "getAttributes element type preserved");
        arr[0] = null;
        check(m.getAttributes()[0] != null, "getAttributes returns a defensive copy");
    }

    static void testDescriptorMutation() throws Exception {
        ModelMBeanInfoSupport m = buildFullInfo();
        // setMBeanDescriptor round-trip
        m.setMBeanDescriptor(new DescriptorSupport(new String[]{"name", "descriptorType", "k"},
                new Object[]{MmbRes8.class.getName(), "mbean", "V"}));
        check("V".equals(m.getMBeanDescriptor().getFieldValue("k")), "setMBeanDescriptor persisted");
        // setDescriptor with an existing name updates the corresponding descriptor
        m.setDescriptor(new DescriptorSupport(new String[]{"name", "descriptorType", "getMethod"},
                new Object[]{"Count", "attribute", "getCount2"}), "attribute");
        check("getCount2".equals(m.getDescriptor("Count", "attribute").getFieldValue("getMethod")),
                "setDescriptor(attribute) updates stored descriptor");
        // setDescriptors batch updates
        m.setDescriptors(new Descriptor[]{new DescriptorSupport(
                new String[]{"name", "descriptorType", "getMethod"},
                new Object[]{"Count", "attribute", "getCount3"})});
        check("getCount3".equals(m.getDescriptor("Count", "attribute").getFieldValue("getMethod")),
                "setDescriptors updates existing entries");
        // setDescriptors cannot introduce a name that has no backing ModelMBean*Info entry
        checkThrowsROE(new ThrowingRunnable() { public void run() throws Exception {
            buildFullInfo().setDescriptors(new Descriptor[]{new DescriptorSupport(
                    new String[]{"name", "descriptorType"}, new Object[]{"NewAttr", "attribute"})});
        } }, "setDescriptors with unknown attribute name");
        checkThrowsROE(new ThrowingRunnable() { public void run() throws Exception {
            buildFullInfo().setDescriptors(null);
        } }, "setDescriptors(null)");
        // setDescriptor name must match an existing entry of that type
        checkThrowsROE(new ThrowingRunnable() { public void run() throws Exception {
            buildFullInfo().setDescriptor(new DescriptorSupport(
                    new String[]{"name", "descriptorType"}, new Object[]{"Wrong", "attribute"}), "attribute");
        } }, "setDescriptor wrong name");
    }

    static void testEqualsHashCodeAndSharing() throws Exception {
        ModelMBeanInfoSupport a = buildFullInfo();
        ModelMBeanInfoSupport b = buildFullInfo();
        check(a.equals(b), "two identically built infos are equal");
        check(a.hashCode() == b.hashCode(), "hashCode consistent for equal infos");
        check(a.equals(a), "equals reflexive");
        check(!a.equals(null), "not equal to null");
        // differing content breaks equality
        ModelMBeanInfoSupport other = new ModelMBeanInfoSupport("Other", "d", null, null, null, null);
        check(!a.equals(other), "different className -> not equal");
        // copy constructor shares the ModelMBean*Info objects: mutating through the copy is visible in the original
        ModelMBeanInfoSupport copy = new ModelMBeanInfoSupport(a);
        check(copy.equals(a), "copy ctor equals original");
        copy.setDescriptor(new DescriptorSupport(new String[]{"name", "descriptorType", "shared"},
                new Object[]{"Count", "attribute", "yes"}), "attribute");
        check("yes".equals(a.getDescriptor("Count", "attribute").getFieldValue("shared")),
                "copy ctor is shallow: setDescriptor through the copy mutates the shared attribute info");
        // clone() is likewise a shallow copy
        ModelMBeanInfoSupport cl = (ModelMBeanInfoSupport) a.clone();
        check(cl.equals(a) && cl != a, "clone equal but distinct");
        cl.setDescriptor(new DescriptorSupport(new String[]{"name", "descriptorType", "cloned"},
                new Object[]{"Count", "attribute", "yes"}), "attribute");
        check("yes".equals(a.getDescriptor("Count", "attribute").getFieldValue("cloned")),
                "clone is shallow: descriptor replacement is visible through shared attribute infos");
        // mbean descriptor itself is not shared between original and clone
        cl.setMBeanDescriptor(new DescriptorSupport(new String[]{"name", "descriptorType", "m"},
                new Object[]{"X", "mbean", "1"}));
        check(!"X".equals(a.getMBeanDescriptor().getFieldValue("name")),
                "setMBeanDescriptor on clone does not affect original");
    }

    static void testSerializationRoundTrip() throws Exception {
        ModelMBeanInfoSupport m = buildFullInfo();
        m.setMBeanDescriptor(new DescriptorSupport(new String[]{"name", "descriptorType", "marker"},
                new Object[]{MmbRes8.class.getName(), "mbean", "M"}));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(m);
        oos.close();
        Object back = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof ModelMBeanInfoSupport, "round-trip class preserved");
        ModelMBeanInfoSupport rm = (ModelMBeanInfoSupport) back;
        check(rm.equals(m), "round-trip equals");
        check("M".equals(rm.getMBeanDescriptor().getFieldValue("marker")), "round-trip mbean descriptor field");
        check("getCount".equals(rm.getDescriptor("Count", "attribute").getFieldValue("getMethod")),
                "round-trip attribute descriptor getMethod");
        check(rm.getAttributes()[0] instanceof ModelMBeanAttributeInfo,
                "round-trip keeps ModelMBean typed arrays");
    }}
