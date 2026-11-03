/*
 * @test
 * @summary javax.management.modelmbean.ModelMBeanConstructorInfo conformance: white-box structure + black-box behavior.
 * @run main ModelMBeanConstructorInfoTest
 */

import javax.management.Descriptor;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanParameterInfo;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanConstructorInfo;

public class ModelMBeanConstructorInfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static class Helper {
        public Helper() {}
        public Helper(String s) {}
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.ModelMBeanConstructorInfo");
        check(MBeanConstructorInfo.class.isAssignableFrom(c), "extends MBeanConstructorInfo");
        check(javax.management.DescriptorAccess.class.isAssignableFrom(c), "implements DescriptorAccess");
        check(c.getConstructors().length >= 3, "multiple public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MBeanParameterInfo[] params = new MBeanParameterInfo[]{
            new MBeanParameterInfo("p", "java.lang.String", "pdesc")
        };
        ModelMBeanConstructorInfo ci = new ModelMBeanConstructorInfo("Test", "desc", params);
        check("Test".equals(ci.getName()), "name");
        check("desc".equals(ci.getDescription()), "description");
        check(ci.getSignature().length == 1, "signature length");
        check("p".equals(ci.getSignature()[0].getName()), "signature param name");
        check("java.lang.String".equals(ci.getSignature()[0].getType()), "signature param type");

        Descriptor d = ci.getDescriptor();
        check(d != null, "descriptor non-null");
        check("Test".equals(d.getFieldValue("name")), "descriptor name");
        check("operation".equals(d.getFieldValue("descriptorType")), "descriptor type");

        // Descriptor-bearing constructor
        DescriptorSupport custom = new DescriptorSupport(
                "name=C2", "descriptorType=operation", "extra=z");
        ModelMBeanConstructorInfo ci2 = new ModelMBeanConstructorInfo(
                "C2", "d2", new MBeanParameterInfo[0], custom);
        check("z".equals(ci2.getDescriptor().getFieldValue("extra")), "descriptor ctor extra");

        // Constructor<?>-based constructor
        ModelMBeanConstructorInfo ci3 = new ModelMBeanConstructorInfo(
                "Helper", Helper.class.getConstructor());
        check(Helper.class.getName().equals(ci3.getName()), "ctor-based name");
        check("Helper".equals(ci3.getDescription()), "ctor-based description");
        check(ci3.getSignature().length == 0, "ctor-based no-arg signature");

        // clone
        ModelMBeanConstructorInfo cloned = (ModelMBeanConstructorInfo) ci.clone();
        check("Test".equals(cloned.getName()), "clone name");
        check(ci.equals(cloned), "clone equals original");

        // setDescriptor (matching name + descriptorType)
        DescriptorSupport nd = new DescriptorSupport(
                "name=Test", "descriptorType=operation", "extra=w");
        ci.setDescriptor(nd);
        check("w".equals(ci.getDescriptor().getFieldValue("extra")), "setDescriptor updates descriptor");

        check(ci.toString().contains("Test"), "toString contains name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
