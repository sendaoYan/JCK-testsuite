/*
 * @test
 * @summary javax.management.DescriptorAccess conformance: interface reflection.
 *          Deepened: subinterface of DescriptorRead declaring exactly
 *          setDescriptor, and the full read/write round-trip contract on the
 *          real implementers - the ModelMBean*Info classes: default descriptor
 *          carries name + descriptorType, setDescriptor clones and stores the
 *          incoming descriptor (defaulting displayName), mismatched name or
 *          descriptorType -> RuntimeOperationsException(IllegalArgumentException),
 *          setDescriptor(null) rebuilds the defaulted descriptor.
 * @run main DescriptorAccessTest
 */

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanFeatureInfo;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

public class DescriptorAccessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    static void expectROE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected RuntimeOperationsException");
        } catch (RuntimeOperationsException ex) {
            passed++;
        } catch (Throwable t) {
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": got " + t.getClass().getName(), t);
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.DescriptorAccess");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("javax.management.DescriptorRead").isAssignableFrom(c), "implements/extends javax.management.DescriptorRead");
        check(hasMethod(c, "setDescriptor"), "declares setDescriptor");
        check(c.getDeclaredMethods().length == 1, "declares exactly one method");
        // javax.management.MBean*Info are read-only: they implement DescriptorRead only
        check(!(new MBeanFeatureInfo("n", "d") instanceof javax.management.DescriptorAccess),
                "MBeanFeatureInfo is not a DescriptorAccess (read-only metadata)");
        // the ModelMBean metadata family is the standard DescriptorAccess implementer
        check(new ModelMBeanAttributeInfo("a", "int", "d", true, false, false)
                instanceof javax.management.DescriptorAccess,
                "ModelMBeanAttributeInfo implements DescriptorAccess");

        testModelMBeanAttributeRoundTrip();
        testOtherModelMBeanImplementers();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testModelMBeanAttributeRoundTrip() {
        ModelMBeanAttributeInfo mai = new ModelMBeanAttributeInfo("Attr", "int", "d", true, true, false);
        // default descriptor: name defaults to the feature name, descriptorType to "attribute"
        check("Attr".equals(mai.getDescriptor().getFieldValue("name")), "default descriptor name");
        check("attribute".equals(mai.getDescriptor().getFieldValue("descriptorType")),
                "default descriptorType");
        // setDescriptor/getDescriptor round-trip; displayName is defaulted when absent
        mai.setDescriptor(new DescriptorSupport(
                new String[]{"name", "descriptorType"}, new Object[]{"Attr", "attribute"}));
        check("attribute".equals(mai.getDescriptor().getFieldValue("descriptorType")),
                "setDescriptor round-trip");
        check("Attr".equals(mai.getDescriptor().getFieldValue("displayName")),
                "displayName defaulted to feature name");
        // setDescriptor stores a CLONE: later mutation of the source is invisible
        DescriptorSupport src = new DescriptorSupport(
                new String[]{"name", "descriptorType"}, new Object[]{"Attr", "attribute"});
        mai.setDescriptor(src);
        src.setField("name", "Changed");
        check("Attr".equals(mai.getDescriptor().getFieldValue("name")),
                "setDescriptor clones the incoming descriptor");
        // mismatched name / descriptorType -> RuntimeOperationsException wrapping IAE
        expectROE(new Runnable() { public void run() {
            new ModelMBeanAttributeInfo("Attr", "int", "d", true, true, false).setDescriptor(
                    new DescriptorSupport(new String[]{"name", "descriptorType"},
                            new Object[]{"Other", "attribute"}));
        } }, "setDescriptor(name mismatch) -> ROE");
        expectROE(new Runnable() { public void run() {
            new ModelMBeanAttributeInfo("Attr", "int", "d", true, true, false).setDescriptor(
                    new DescriptorSupport(new String[]{"name", "descriptorType"},
                            new Object[]{"Attr", "operation"}));
        } }, "setDescriptor(descriptorType mismatch) -> ROE");
        // null descriptor rebuilds the defaulted one instead of clearing it
        ModelMBeanAttributeInfo nulled = new ModelMBeanAttributeInfo("Attr", "int", "d", true, true, false);
        nulled.setDescriptor(null);
        check("Attr".equals(nulled.getDescriptor().getFieldValue("name")),
                "setDescriptor(null) rebuilds defaulted descriptor (name)");
        check("attribute".equals(nulled.getDescriptor().getFieldValue("descriptorType")),
                "setDescriptor(null) rebuilds defaulted descriptor (descriptorType)");
        check("Attr".equals(nulled.getDescriptor().getFieldValue("displayName")),
                "setDescriptor(null) rebuilds defaulted descriptor (displayName)");
    }

    static void testOtherModelMBeanImplementers() {
        check(new ModelMBeanOperationInfo("op", "d", null, "void",
                ModelMBeanOperationInfo.INFO) instanceof javax.management.DescriptorAccess,
                "ModelMBeanOperationInfo implements DescriptorAccess");
        check(new ModelMBeanConstructorInfo("k", "d", null) instanceof javax.management.DescriptorAccess,
                "ModelMBeanConstructorInfo implements DescriptorAccess");
        check(new ModelMBeanNotificationInfo(new String[]{"t"}, "n", "d")
                instanceof javax.management.DescriptorAccess,
                "ModelMBeanNotificationInfo implements DescriptorAccess");
        // operation flavour requires descriptorType "operation" and matching name
        final ModelMBeanOperationInfo moi = new ModelMBeanOperationInfo("op", "d", null, "void",
                ModelMBeanOperationInfo.INFO);
        moi.setDescriptor(new DescriptorSupport(
                new String[]{"name", "descriptorType", "role"}, new Object[]{"op", "operation", "operation"}));
        check("operation".equals(moi.getDescriptor().getFieldValue("descriptorType")),
                "operation setDescriptor round-trip");
        // anonymous implementation of the interface contract itself
        final Descriptor[] holder = { new ImmutableDescriptor("k=v") };
        javax.management.DescriptorAccess access = new javax.management.DescriptorAccess() {
            public Descriptor getDescriptor() { return holder[0]; }
            public void setDescriptor(Descriptor d) { holder[0] = d; }
        };
        Descriptor repl = new ImmutableDescriptor("z=9");
        access.setDescriptor(repl);
        check(access.getDescriptor() == repl, "anonymous set/getDescriptor round-trip");
    }
}
