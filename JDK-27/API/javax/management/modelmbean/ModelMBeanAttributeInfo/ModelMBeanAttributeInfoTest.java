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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
