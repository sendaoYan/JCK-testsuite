/*
 * @test
 * @summary javax.management.DescriptorAccess conformance: white-box structure + light behavior.
 *          Deepened: the ModelMBean*Info family as the real read/write
 *          implementers - defaulted descriptor (name + descriptorType),
 *          setDescriptor cloning + displayName defaulting, name/descriptorType
 *          mismatch -> RuntimeOperationsException, setDescriptor(null)
 *          rebuilding the defaults, and plain MBean*Info staying read-only.
 * @run main DescriptorAccessTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

public class DescriptorAccessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.DescriptorAccess");
        check(c.isInterface(), "is interface");
        check(DescriptorRead.class.isAssignableFrom(c), "extends DescriptorRead");
        check(hasMethod(c, "setDescriptor"), "has setDescriptor");
        check(hasMethod(c, "getDescriptor"), "has getDescriptor (from DescriptorRead)");
        check(c.getDeclaredMethods().length == 1, "declares exactly one method");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        final Descriptor[] current = { null };
        DescriptorAccess access = new DescriptorAccess() {
            public Descriptor getDescriptor() { return current[0]; }
            public void setDescriptor(Descriptor d) { current[0] = d; }
        };
        Descriptor d = new ImmutableDescriptor("name=n");
        access.setDescriptor(d);
        check(access.getDescriptor() == d, "set/getDescriptor round-trip");

        // StandardMBean is a DescriptorAccess? MBeanFeatureInfo is a DescriptorRead.
        MBeanFeatureInfo fi = new MBeanFeatureInfo("n", "d");
        check(fi instanceof DescriptorRead, "MBeanFeatureInfo is DescriptorRead");
        check(!(fi instanceof DescriptorAccess), "plain MBeanFeatureInfo is NOT a DescriptorAccess");
    }

    // ---- deepened: ModelMBean*Info implementers ----------------------------

    static void testModelMBeanImplementers() {
        check(new ModelMBeanAttributeInfo("a", "int", "d", true, false, false)
                instanceof DescriptorAccess, "ModelMBeanAttributeInfo implements DescriptorAccess");
        check(new ModelMBeanOperationInfo("op", "d", null, "void",
                ModelMBeanOperationInfo.INFO) instanceof DescriptorAccess,
                "ModelMBeanOperationInfo implements DescriptorAccess");
        check(new ModelMBeanConstructorInfo("k", "d", null) instanceof DescriptorAccess,
                "ModelMBeanConstructorInfo implements DescriptorAccess");
        check(new ModelMBeanNotificationInfo(new String[]{"t"}, "n", "d")
                instanceof DescriptorAccess,
                "ModelMBeanNotificationInfo implements DescriptorAccess");
    }

    static void testModelMBeanAttributeRoundTrip() {
        ModelMBeanAttributeInfo mai = new ModelMBeanAttributeInfo("Attr", "int", "d", true, true, false);
        check("Attr".equals(mai.getDescriptor().getFieldValue("name")), "default descriptor name");
        check("attribute".equals(mai.getDescriptor().getFieldValue("descriptorType")),
                "default descriptorType");
        mai.setDescriptor(new DescriptorSupport(
                new String[]{"name", "descriptorType"}, new Object[]{"Attr", "attribute"}));
        check("attribute".equals(mai.getDescriptor().getFieldValue("descriptorType")),
                "setDescriptor round-trip");
        check("Attr".equals(mai.getDescriptor().getFieldValue("displayName")),
                "displayName defaulted to feature name");
        DescriptorSupport src = new DescriptorSupport(
                new String[]{"name", "descriptorType"}, new Object[]{"Attr", "attribute"});
        mai.setDescriptor(src);
        src.setField("name", "Changed");
        check("Attr".equals(mai.getDescriptor().getFieldValue("name")),
                "setDescriptor clones the incoming descriptor");
        try {
            new ModelMBeanAttributeInfo("Attr", "int", "d", true, true, false).setDescriptor(
                    new DescriptorSupport(new String[]{"name", "descriptorType"},
                            new Object[]{"Other", "attribute"}));
            check(false, "name mismatch should throw");
        } catch (RuntimeOperationsException ex) {
            check(ex.getCause() instanceof IllegalArgumentException,
                    "name mismatch -> ROE(IllegalArgumentException)");
        }
        try {
            new ModelMBeanAttributeInfo("Attr", "int", "d", true, true, false).setDescriptor(
                    new DescriptorSupport(new String[]{"name", "descriptorType"},
                            new Object[]{"Attr", "operation"}));
            check(false, "descriptorType mismatch should throw");
        } catch (RuntimeOperationsException ex) {
            check(ex.getCause() instanceof IllegalArgumentException,
                    "descriptorType mismatch -> ROE(IllegalArgumentException)");
        }
        ModelMBeanAttributeInfo nulled = new ModelMBeanAttributeInfo("Attr", "int", "d", true, true, false);
        nulled.setDescriptor(null);
        check("Attr".equals(nulled.getDescriptor().getFieldValue("name")),
                "setDescriptor(null) rebuilds defaults (name)");
        check("attribute".equals(nulled.getDescriptor().getFieldValue("descriptorType")),
                "setDescriptor(null) rebuilds defaults (descriptorType)");
        check("Attr".equals(nulled.getDescriptor().getFieldValue("displayName")),
                "setDescriptor(null) rebuilds defaults (displayName)");
    }

    static void testOperationFlavour() {
        ModelMBeanOperationInfo moi = new ModelMBeanOperationInfo("op", "d", null, "void",
                ModelMBeanOperationInfo.INFO);
        moi.setDescriptor(new DescriptorSupport(
                new String[]{"name", "descriptorType", "role"},
                new Object[]{"op", "operation", "operation"}));
        check("operation".equals(moi.getDescriptor().getFieldValue("descriptorType")),
                "operation setDescriptor round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testModelMBeanImplementers();
        testModelMBeanAttributeRoundTrip();
        testOperationFlavour();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
