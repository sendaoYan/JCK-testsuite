/*
 * @test
 * @summary org.omg.PortableInterceptor.ObjectReferenceTemplate conformance: interface shape.
 * @run main ObjectReferenceTemplateTest
 */

public class ObjectReferenceTemplateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.ObjectReferenceTemplate");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableInterceptor.ObjectReferenceFactory.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.ObjectReferenceFactory");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
