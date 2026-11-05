/*
 * @test
 * @summary javax.management.modelmbean.ModelMBeanAttributeInfo conformance: white-box structure + black-box behavior.
 * @run main ModelMBeanAttributeInfoTest
 */

import javax.management.Descriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanAttributeInfo;

public class ModelMBeanAttributeInfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Helper bean for Method-based constructor.
    public static class Bean {
        private String foo;
        public String getFoo() { return foo; }
        public void setFoo(String foo) { this.foo = foo; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.ModelMBeanAttributeInfo");
        check(MBeanAttributeInfo.class.isAssignableFrom(c), "extends MBeanAttributeInfo");
        check(javax.management.DescriptorAccess.class.isAssignableFrom(c), "implements DescriptorAccess");
        check(c.getConstructors().length >= 4, "multiple public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ModelMBeanAttributeInfo ai = new ModelMBeanAttributeInfo(
                "Name", "java.lang.String", "desc", true, true, false);
        check("Name".equals(ai.getName()), "name");
        check("java.lang.String".equals(ai.getType()), "type");
        check("desc".equals(ai.getDescription()), "description");
        check(ai.isReadable(), "isReadable");
        check(ai.isWritable(), "isWritable");
        check(!ai.isIs(), "isIs false");

        Descriptor d = ai.getDescriptor();
        check(d != null, "descriptor non-null");
        check("Name".equals(d.getFieldValue("name")), "descriptor name field");
        check("attribute".equals(d.getFieldValue("descriptorType")), "descriptor type field");

        // Descriptor-bearing constructor (descriptor "name" must match attribute name)
        DescriptorSupport custom = new DescriptorSupport(
                "name=N2", "descriptorType=attribute", "extra=x");
        ModelMBeanAttributeInfo ai2 = new ModelMBeanAttributeInfo(
                "N2", "int", "d2", true, false, false, custom);
        check("N2".equals(ai2.getName()), "descriptor ctor name");
        check("x".equals(ai2.getDescriptor().getFieldValue("extra")), "descriptor ctor preserves extra field");

        // Copy constructor
        ModelMBeanAttributeInfo copy = new ModelMBeanAttributeInfo(ai);
        check("Name".equals(copy.getName()), "copy ctor name");
        check(ai.equals(copy), "copy equals original");

        // Method-based constructor
        ModelMBeanAttributeInfo ai3 = new ModelMBeanAttributeInfo(
                "foo", "java.lang.String",
                Bean.class.getMethod("getFoo"), Bean.class.getMethod("setFoo", String.class));
        check("foo".equals(ai3.getName()), "method ctor name");
        check("java.lang.String".equals(ai3.getType()), "method ctor type");
        check(ai3.isReadable() && ai3.isWritable(), "method ctor read/write");

        // clone
        ModelMBeanAttributeInfo cloned = (ModelMBeanAttributeInfo) ai.clone();
        check("Name".equals(cloned.getName()), "clone name");
        check(ai.equals(cloned), "clone equals original");

        // setDescriptor (must keep a matching "name"/"descriptorType")
        DescriptorSupport nd = new DescriptorSupport(
                "name=Name", "descriptorType=attribute", "extra=y");
        ai.setDescriptor(nd);
        check("y".equals(ai.getDescriptor().getFieldValue("extra")), "setDescriptor updates descriptor");
        check("Name".equals(ai.getDescriptor().getFieldValue("name")), "setDescriptor keeps name");

        // toString contains the name
        check(ai.toString().contains("Name"), "toString contains name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepDescriptorDefaults();
        testDeepMethodForms();
        testDeepSetDescriptorRules();
        testDeepCloneSerializationEquals();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch h): descriptor rules, Method forms, clone/serialization ----

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

    static void testDeepDescriptorDefaults() throws Exception {
        // plain constructor builds the documented default descriptor
        ModelMBeanAttributeInfo plain = new ModelMBeanAttributeInfo("N", "java.lang.String", "d", true, false, false);
        Descriptor d = plain.getDescriptor();
        check("N".equals(d.getFieldValue("name")), "default descriptor name");
        check("attribute".equals(d.getFieldValue("descriptorType")), "default descriptorType=attribute");
        check("N".equals(d.getFieldValue("displayName")), "default displayName=attribute name");
        check(d.getFieldNames().length == 3, "default descriptor has exactly name/descriptorType/displayName");
        // null descriptor in the 7-arg constructor falls back to the defaults
        ModelMBeanAttributeInfo n = new ModelMBeanAttributeInfo("M", "boolean", "d", true, true, true, null);
        check("M".equals(n.getDescriptor().getFieldValue("name")), "null descriptor -> defaulted");
        check(n.isIs(), "isIs flag preserved");
        // isIs=true is only legal for boolean attribute types
        try {
            new ModelMBeanAttributeInfo("M", "int", "d", true, true, true);
            check(false, "isIs for non-boolean must throw");
        } catch (IllegalArgumentException iae) {
            check(true, "isIs for non-boolean -> IllegalArgumentException");
        }
        // the descriptor passed to the constructor is cloned, not aliased
        DescriptorSupport ext = new DescriptorSupport(
                new String[]{"name", "descriptorType", "k"}, new Object[]{"K", "attribute", "v"});
        ModelMBeanAttributeInfo c = new ModelMBeanAttributeInfo("K", "int", "d", true, true, false, ext);
        ext.setField("k", "changed");
        check("v".equals(c.getDescriptor().getFieldValue("k")), "ctor clones the passed descriptor");
        // getDescriptor returns a clone: mutating it does not change the stored descriptor
        c.getDescriptor().setField("k", "w");
        check("v".equals(c.getDescriptor().getFieldValue("k")),
                "getDescriptor returns an independent clone (mutation not persisted)");
    }

    static void testDeepMethodForms() throws Exception {
        // getter only -> readable, not writable
        ModelMBeanAttributeInfo ro = new ModelMBeanAttributeInfo(
                "Foo", "d", Bean.class.getMethod("getFoo"), null);
        check(ro.isReadable() && !ro.isWritable(), "getter-only -> read-only");
        check("java.lang.String".equals(ro.getType()), "getter-only type from Method return");
        // setter only -> writable, not readable
        ModelMBeanAttributeInfo wo = new ModelMBeanAttributeInfo(
                "Foo", "d", null, Bean.class.getMethod("setFoo", String.class));
        check(!wo.isReadable() && wo.isWritable(), "setter-only -> write-only");
        // swapped getter/setter methods -> IntrospectionException
        try {
            new ModelMBeanAttributeInfo("Foo", "d",
                    Bean.class.getMethod("setFoo", String.class), Bean.class.getMethod("getFoo"));
            check(false, "swapped getter/setter must throw IntrospectionException");
        } catch (javax.management.IntrospectionException ie) {
            check(true, "swapped getter/setter -> IntrospectionException");
        }
        // method-form descriptor does not auto-populate getMethod/setMethod fields
        ModelMBeanAttributeInfo both = new ModelMBeanAttributeInfo(
                "Foo", "d", Bean.class.getMethod("getFoo"), Bean.class.getMethod("setFoo", String.class));
        check(both.getDescriptor().getFieldValue("getMethod") == null,
                "method-form descriptor has no getMethod field (routing needs explicit fields)");
        // method-form with explicit descriptor
        ModelMBeanAttributeInfo wd = new ModelMBeanAttributeInfo(
                "Foo", "d", Bean.class.getMethod("getFoo"), Bean.class.getMethod("setFoo", String.class),
                new DescriptorSupport(new String[]{"name", "descriptorType", "getMethod", "setMethod"},
                        new Object[]{"Foo", "attribute", "getFoo", "setFoo"}));
        check("getFoo".equals(wd.getDescriptor().getFieldValue("getMethod")),
                "method-form with descriptor preserves getMethod");
    }

    static void testDeepSetDescriptorRules() {
        ModelMBeanAttributeInfo a = new ModelMBeanAttributeInfo("N", "java.lang.String", "d", true, true, false);
        // name mismatch -> RuntimeOperationsException
        checkThrowsROE(new ThrowingRunnable() { public void run() {
            new ModelMBeanAttributeInfo("N", "t", "d", true, true, false).setDescriptor(
                    new DescriptorSupport(new String[]{"name", "descriptorType"}, new Object[]{"Other", "attribute"}));
        } }, "setDescriptor with wrong name");
        // wrong descriptorType -> RuntimeOperationsException
        checkThrowsROE(new ThrowingRunnable() { public void run() {
            new ModelMBeanAttributeInfo("N", "t", "d", true, true, false).setDescriptor(
                    new DescriptorSupport(new String[]{"name", "descriptorType"}, new Object[]{"N", "operation"}));
        } }, "setDescriptor with wrong descriptorType");
        // name match is case-insensitive
        ModelMBeanAttributeInfo lower = new ModelMBeanAttributeInfo("Count", "int", "d", true, true, false);
        lower.setDescriptor(new DescriptorSupport(new String[]{"name", "descriptorType"},
                new Object[]{"count", "attribute"}));
        check("count".equals(lower.getDescriptor().getFieldValue("name")),
                "case-insensitive name match accepted");
        // null resets to the default descriptor
        ModelMBeanAttributeInfo reset = new ModelMBeanAttributeInfo("N", "java.lang.String", "d", true, true, false,
                new DescriptorSupport(new String[]{"name", "descriptorType", "extra"},
                        new Object[]{"N", "attribute", "x"}));
        reset.setDescriptor(null);
        check(reset.getDescriptor().getFieldValue("extra") == null, "setDescriptor(null) resets to default");
        check("N".equals(reset.getDescriptor().getFieldValue("name")), "reset default keeps name");
    }

    static void testDeepCloneSerializationEquals() throws Exception {
        ModelMBeanAttributeInfo a = new ModelMBeanAttributeInfo("N", "java.lang.String", "d", true, true, false,
                new DescriptorSupport(new String[]{"name", "descriptorType", "getMethod"},
                        new Object[]{"N", "attribute", "getN"}));
        // clone is a distinct object with an independent descriptor
        ModelMBeanAttributeInfo cl = (ModelMBeanAttributeInfo) a.clone();
        check(cl != a && cl.equals(a), "clone distinct but equal");
        cl.setDescriptor(new DescriptorSupport(new String[]{"name", "descriptorType", "getMethod"},
                new Object[]{"N", "attribute", "other"}));
        check("getN".equals(a.getDescriptor().getFieldValue("getMethod")),
                "clone descriptor replacement does not affect original");
        // serialization round-trip
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(a);
        oos.close();
        Object back = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof ModelMBeanAttributeInfo, "round-trip class preserved");
        check(back.equals(a), "round-trip equals");
        check("getN".equals(((ModelMBeanAttributeInfo) back).getDescriptor().getFieldValue("getMethod")),
                "round-trip keeps descriptor fields");
        // equality with the plain MBeanAttributeInfo parent class instance is false both ways
        javax.management.MBeanAttributeInfo plain = new javax.management.MBeanAttributeInfo(
                "N", "java.lang.String", "d", true, true, false);
        check(!a.equals(plain), "ModelMBeanAttributeInfo not equal to plain MBeanAttributeInfo");
        check(!plain.equals(a), "plain MBeanAttributeInfo not equal to ModelMBeanAttributeInfo");
        // same-configuration ModelMBean instances are equal
        ModelMBeanAttributeInfo a2 = new ModelMBeanAttributeInfo("N", "java.lang.String", "d", true, true, false,
                new DescriptorSupport(new String[]{"name", "descriptorType", "getMethod"},
                        new Object[]{"N", "attribute", "getN"}));
        check(a.equals(a2) && a.hashCode() == a2.hashCode(), "same config equals with hashCode");
        // toString mentions the attribute name and its descriptor
        check(a.toString().contains("N") && a.toString().contains("ModelMBeanAttributeInfo"),
                "toString contains class and name");
    }
}
