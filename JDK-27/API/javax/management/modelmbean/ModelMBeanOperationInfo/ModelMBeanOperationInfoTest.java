/*
 * @test
 * @summary javax.management.modelmbean.ModelMBeanOperationInfo conformance: white-box structure + black-box behavior.
 * @run main ModelMBeanOperationInfoTest
 */

import javax.management.Descriptor;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanOperationInfo;

public class ModelMBeanOperationInfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static class Helper {
        public String concat(String a, int b) { return a + b; }
        public void nothing() {}
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.ModelMBeanOperationInfo");
        check(MBeanOperationInfo.class.isAssignableFrom(c), "extends MBeanOperationInfo");
        check(javax.management.DescriptorAccess.class.isAssignableFrom(c), "implements DescriptorAccess");
        check(c.getConstructors().length >= 4, "multiple public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MBeanParameterInfo[] params = new MBeanParameterInfo[]{
            new MBeanParameterInfo("a", "java.lang.String", "adesc"),
            new MBeanParameterInfo("b", "int", "bdesc")
        };
        ModelMBeanOperationInfo oi = new ModelMBeanOperationInfo(
                "op", "desc", params, "java.lang.String", MBeanOperationInfo.ACTION);
        check("op".equals(oi.getName()), "name");
        check("desc".equals(oi.getDescription()), "description");
        check("java.lang.String".equals(oi.getReturnType()), "return type");
        check(oi.getImpact() == MBeanOperationInfo.ACTION, "impact");
        check(oi.getSignature().length == 2, "signature length");
        check("a".equals(oi.getSignature()[0].getName()), "signature param 0 name");
        check("int".equals(oi.getSignature()[1].getType()), "signature param 1 type");

        Descriptor d = oi.getDescriptor();
        check(d != null, "descriptor non-null");
        check("op".equals(d.getFieldValue("name")), "descriptor name");
        check("operation".equals(d.getFieldValue("descriptorType")), "descriptor type");
        check("operation".equals(d.getFieldValue("role")), "descriptor role");

        // Descriptor-bearing constructor
        DescriptorSupport custom = new DescriptorSupport(
                "name=op2", "descriptorType=operation", "role=operation", "extra=s");
        ModelMBeanOperationInfo oi2 = new ModelMBeanOperationInfo(
                "op2", "d2", new MBeanParameterInfo[0], "void", MBeanOperationInfo.INFO, custom);
        check("s".equals(oi2.getDescriptor().getFieldValue("extra")), "descriptor ctor extra");
        check(oi2.getImpact() == MBeanOperationInfo.INFO, "descriptor ctor impact");

        // Method-based constructor
        ModelMBeanOperationInfo oi3 = new ModelMBeanOperationInfo(
                "concat", Helper.class.getMethod("concat", String.class, int.class));
        check("concat".equals(oi3.getName()), "method ctor name");
        check("java.lang.String".equals(oi3.getReturnType()), "method ctor return type");
        check(oi3.getSignature().length == 2, "method ctor signature length");

        // copy constructor
        ModelMBeanOperationInfo copy = new ModelMBeanOperationInfo(oi);
        check("op".equals(copy.getName()), "copy ctor name");
        check(oi.equals(copy), "copy equals original");

        // clone
        ModelMBeanOperationInfo cloned = (ModelMBeanOperationInfo) oi.clone();
        check(oi.equals(cloned), "clone equals original");

        // setDescriptor (matching name + descriptorType + role)
        DescriptorSupport nd = new DescriptorSupport(
                "name=op", "descriptorType=operation", "role=operation", "extra=t");
        oi.setDescriptor(nd);
        check("t".equals(oi.getDescriptor().getFieldValue("extra")), "setDescriptor updates descriptor");

        check(oi.toString().contains("op"), "toString contains name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
