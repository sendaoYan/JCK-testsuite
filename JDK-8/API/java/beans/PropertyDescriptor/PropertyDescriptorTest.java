/*
 * @test
 * @summary java.beans.PropertyDescriptor conformance: read/write methods, type, bound.
 * @run main PropertyDescriptorTest
 */

import java.beans.*;

public class PropertyDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class MyBean {
        public String getName() { return null; }
        public void setName(String v) {}
    }

    public static void main(String[] args) throws Exception {
        PropertyDescriptor pd = new PropertyDescriptor("name", MyBean.class);
        check(pd.getReadMethod() != null, "read method");
        check(pd.getWriteMethod() != null, "write method");
        check(pd.getPropertyType() == String.class, "property type");
        pd.setBound(true);
        check(pd.isBound(), "bound");
        pd.setConstrained(true);
        check(pd.isConstrained(), "constrained");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
