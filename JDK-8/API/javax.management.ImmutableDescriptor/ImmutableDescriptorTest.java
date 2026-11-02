/*
 * @test
 * @summary javax.management.ImmutableDescriptor conformance: field access, validity, empty descriptor.
 * @run main ImmutableDescriptorTest
 */

import javax.management.*;
public class ImmutableDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        ImmutableDescriptor d = new ImmutableDescriptor(new String[]{"a"}, new Object[]{"b"});
        check("b".equals(d.getFieldValue("a")), "field value");
        check(d.getFieldNames().length == 1 && "a".equals(d.getFieldNames()[0]), "field names");
        check(d.isValid(), "isValid");
        check(ImmutableDescriptor.EMPTY_DESCRIPTOR != null, "EMPTY_DESCRIPTOR");
        check(d.equals(new ImmutableDescriptor(new String[]{"a"}, new Object[]{"b"})), "equals");
        check(!d.equals(new ImmutableDescriptor(new String[]{"a"}, new Object[]{"c"})), "not equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
