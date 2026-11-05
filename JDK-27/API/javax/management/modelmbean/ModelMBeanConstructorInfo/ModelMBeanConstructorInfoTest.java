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
        testDeepConstructorForm();
        testDeepSetDescriptorRules();
        testDeepCloneSerializationEquals();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch h): Constructor form, descriptor rules, clone/serialization ----

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

    static void testDeepConstructorForm() throws Exception {
        // no-arg Constructor: name is the constructor (simple/binary) name, empty signature
        ModelMBeanConstructorInfo noArg = new ModelMBeanConstructorInfo("d", Helper.class.getConstructor());
        check(Helper.class.getSimpleName().equals(noArg.getName())
                || noArg.getName().endsWith(Helper.class.getSimpleName()),
                "name comes from Constructor.getName()");
        check(noArg.getSignature().length == 0, "no-arg constructor has empty signature");
        // one-arg Constructor of java.lang.String
        ModelMBeanConstructorInfo s = new ModelMBeanConstructorInfo("d",
                String.class.getConstructor(String.class));
        check("java.lang.String".equals(s.getName()), "String ctor name");
        check(s.getSignature().length == 1 && "java.lang.String".equals(s.getSignature()[0].getType()),
                "String ctor signature type");
        // default descriptor: descriptorType is "operation" with role "constructor"
        Descriptor d = noArg.getDescriptor();
        check(noArg.getName().equals(d.getFieldValue("name")), "default descriptor name");
        check("operation".equals(d.getFieldValue("descriptorType")),
                "default descriptorType=operation (constructor descriptors reuse the operation type)");
        check("constructor".equals(d.getFieldValue("role")), "default role=constructor");
        check(noArg.getName().equals(d.getFieldValue("displayName")), "default displayName");
        // parameterized form
        javax.management.MBeanParameterInfo[] params = {
            new javax.management.MBeanParameterInfo("p", "int", "pd") };
        ModelMBeanConstructorInfo pf = new ModelMBeanConstructorInfo("Ctor", "d", params);
        check("Ctor".equals(pf.getName()) && pf.getSignature().length == 1, "param form basics");
        // descriptor form
        ModelMBeanConstructorInfo df = new ModelMBeanConstructorInfo("Ctor", "d", params,
                new DescriptorSupport(new String[]{"name", "descriptorType", "role"},
                        new Object[]{"Ctor", "operation", "constructor"}));
        check("constructor".equals(df.getDescriptor().getFieldValue("role")), "descriptor form role");
    }

    static void testDeepSetDescriptorRules() {
        ModelMBeanConstructorInfo c = new ModelMBeanConstructorInfo("Ctor", "d",
                new javax.management.MBeanParameterInfo[0]);
        // matching name accepted
        c.setDescriptor(new DescriptorSupport(new String[]{"name", "descriptorType", "k"},
                new Object[]{"Ctor", "operation", "v"}));
        check("v".equals(c.getDescriptor().getFieldValue("k")), "setDescriptor persists");
        // wrong name -> RuntimeOperationsException
        checkThrowsROE(new ThrowingRunnable() { public void run() {
            new ModelMBeanConstructorInfo("Ctor", "d", new javax.management.MBeanParameterInfo[0])
                .setDescriptor(new DescriptorSupport(new String[]{"name", "descriptorType"},
                        new Object[]{"Other", "operation"}));
        } }, "setDescriptor with wrong name");
        // null resets to defaults
        c.setDescriptor(null);
        check(c.getDescriptor().getFieldValue("k") == null, "setDescriptor(null) resets to default");
        check("constructor".equals(c.getDescriptor().getFieldValue("role")), "reset default role");
    }

    static void testDeepCloneSerializationEquals() throws Exception {
        ModelMBeanConstructorInfo c = new ModelMBeanConstructorInfo("d", Helper.class.getConstructor());
        ModelMBeanConstructorInfo cl = (ModelMBeanConstructorInfo) c.clone();
        check(cl != c && cl.equals(c), "clone distinct but equal");
        // serialization round-trip
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(c);
        oos.close();
        Object back = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof ModelMBeanConstructorInfo, "round-trip class preserved");
        check(back.equals(c), "round-trip equals");
        // equality against plain parent class instance is false
        javax.management.MBeanConstructorInfo plain = new javax.management.MBeanConstructorInfo(
                c.getName(), "d", new javax.management.MBeanParameterInfo[0]);
        check(!c.equals(plain) && !plain.equals(c), "not equal to plain MBeanConstructorInfo");
        // same-config instances equal
        ModelMBeanConstructorInfo c2 = new ModelMBeanConstructorInfo("d", Helper.class.getConstructor());
        check(c.equals(c2) && c.hashCode() == c2.hashCode(), "same config equals with hashCode");
        check(c.toString().contains(c.getName()), "toString contains name");
    }
}
