/*
 * @test
 * @summary javax.management.modelmbean.ModelMBeanConstructorInfo conformance: concrete class reflection.
 * @run main ModelMBeanConstructorInfoTest
 */

import javax.management.Descriptor;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
public class ModelMBeanConstructorInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.ModelMBeanConstructorInfo");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.MBeanConstructorInfo".equals(c.getSuperclass().getName()), "superclass javax.management.MBeanConstructorInfo");
        check(Class.forName("javax.management.DescriptorAccess").isAssignableFrom(c), "implements/extends javax.management.DescriptorAccess");
        check(hasMethod(c, "clone"), "declares clone");
        check(hasMethod(c, "getDescriptor"), "declares getDescriptor");
        check(hasMethod(c, "setDescriptor"), "declares setDescriptor");
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

    public static class Helper {
        public Helper() {}
        public Helper(String s) {}
    }
}
