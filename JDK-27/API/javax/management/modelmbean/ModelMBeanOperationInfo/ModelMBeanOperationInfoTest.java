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
        testDeepImpactConstants();
        testDeepMethodFormDefaults();
        testDeepFullFormAndDescriptor();
        testDeepCopyCloneSerializationEquals();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch h): descriptor defaults, impact matrix, clone/serialization ----

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

    static void testDeepImpactConstants() {
        check(MBeanOperationInfo.INFO == 0, "INFO == 0");
        check(MBeanOperationInfo.ACTION == 1, "ACTION == 1");
        check(MBeanOperationInfo.ACTION_INFO == 2, "ACTION_INFO == 2");
        check(MBeanOperationInfo.UNKNOWN == 3, "UNKNOWN == 3");
    }

    static void testDeepMethodFormDefaults() throws Exception {
        ModelMBeanOperationInfo m = new ModelMBeanOperationInfo(
                "desc", Helper.class.getMethod("concat", String.class, int.class));
        check("concat".equals(m.getName()), "name from Method");
        check("desc".equals(m.getDescription()), "description from ctor");
        check("java.lang.String".equals(m.getReturnType()), "return type from Method");
        check(m.getImpact() == MBeanOperationInfo.UNKNOWN, "method-form default impact is UNKNOWN");
        Descriptor d = m.getDescriptor();
        check("concat".equals(d.getFieldValue("name")), "default descriptor name from Method");
        check("operation".equals(d.getFieldValue("descriptorType")), "default descriptorType=operation");
        check("operation".equals(d.getFieldValue("role")), "default role=operation");
        check("concat".equals(d.getFieldValue("displayName")), "default displayName from Method name");
        // signature carried over from the Method parameters; array is a defensive copy
        check(m.getSignature().length == 2, "signature length from Method");
        check("p1".equals(m.getSignature()[0].getName()) && "java.lang.String".equals(m.getSignature()[0].getType()),
                "first parameter name/type");
        check("int".equals(m.getSignature()[1].getType()), "second parameter type");
        javax.management.MBeanParameterInfo[] sig = m.getSignature();
        sig[0] = null;
        check(m.getSignature()[0] != null, "getSignature returns a defensive copy");
    }

    static void testDeepFullFormAndDescriptor() {
        // full constructor with parameters, impact and descriptor
        javax.management.MBeanParameterInfo[] params = {
            new javax.management.MBeanParameterInfo("amount", "int", "how much") };
        DescriptorSupport od = new DescriptorSupport(new String[]{"name", "descriptorType", "role"},
                new Object[]{"bump", "operation", "operation"});
        ModelMBeanOperationInfo f = new ModelMBeanOperationInfo(
                "bump", "bump op", params, "int", MBeanOperationInfo.ACTION_INFO, od);
        check("bump".equals(f.getName()) && "int".equals(f.getReturnType()), "full form basics");
        check(f.getImpact() == MBeanOperationInfo.ACTION_INFO, "full form impact");
        check("amount".equals(f.getSignature()[0].getName()), "full form signature element");
        check("operation".equals(f.getDescriptor().getFieldValue("role")), "full form descriptor role");
        // 5-arg form without descriptor gets the defaults
        ModelMBeanOperationInfo f5 = new ModelMBeanOperationInfo(
                "bump", "bump op", params, "int", MBeanOperationInfo.ACTION);
        check("operation".equals(f5.getDescriptor().getFieldValue("descriptorType")),
                "5-arg form default descriptorType");
        // setDescriptor round-trip with matching name
        f.setDescriptor(new DescriptorSupport(new String[]{"name", "descriptorType", "k"},
                new Object[]{"bump", "operation", "v"}));
        check("v".equals(f.getDescriptor().getFieldValue("k")), "setDescriptor persists");
        // name mismatch -> RuntimeOperationsException
        checkThrowsROE(new ThrowingRunnable() { public void run() {
            new ModelMBeanOperationInfo("x", "d", new javax.management.MBeanParameterInfo[0], "void",
                    MBeanOperationInfo.INFO).setDescriptor(new DescriptorSupport(
                    new String[]{"name", "descriptorType"}, new Object[]{"other", "operation"}));
        } }, "setDescriptor with wrong name");
        // descriptorType mismatch -> RuntimeOperationsException
        checkThrowsROE(new ThrowingRunnable() { public void run() {
            new ModelMBeanOperationInfo("x", "d", new javax.management.MBeanParameterInfo[0], "void",
                    MBeanOperationInfo.INFO).setDescriptor(new DescriptorSupport(
                    new String[]{"name", "descriptorType"}, new Object[]{"x", "attribute"}));
        } }, "setDescriptor with wrong descriptorType");
    }

    static void testDeepCopyCloneSerializationEquals() throws Exception {
        ModelMBeanOperationInfo m = new ModelMBeanOperationInfo(
                "desc", Helper.class.getMethod("concat", String.class, int.class));
        // copy constructor
        ModelMBeanOperationInfo cp = new ModelMBeanOperationInfo(m);
        check(cp.equals(m) && cp != m, "copy ctor equals");
        check("concat".equals(cp.getName()), "copy ctor keeps name");
        // clone
        ModelMBeanOperationInfo cl = (ModelMBeanOperationInfo) m.clone();
        check(cl.equals(m), "clone equals");
        // serialization round-trip
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(m);
        oos.close();
        Object back = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof ModelMBeanOperationInfo, "round-trip class preserved");
        check(back.equals(m), "round-trip equals");
        check("operation".equals(((ModelMBeanOperationInfo) back).getDescriptor().getFieldValue("role")),
                "round-trip keeps descriptor");
        // equality against the plain parent class instance is false
        javax.management.MBeanOperationInfo plain = new javax.management.MBeanOperationInfo(
                "concat", "desc", new javax.management.MBeanParameterInfo[0], "java.lang.String",
                MBeanOperationInfo.UNKNOWN);
        check(!m.equals(plain) && !plain.equals(m), "not equal to plain MBeanOperationInfo");
        check(m.toString().contains("concat"), "toString contains operation name");
    }
}
