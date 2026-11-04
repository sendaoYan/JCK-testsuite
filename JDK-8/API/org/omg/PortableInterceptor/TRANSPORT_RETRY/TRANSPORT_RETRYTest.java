/*
 * @test
 * @summary org.omg.PortableInterceptor.TRANSPORT_RETRY conformance: constant holder interface.
 * @run main TRANSPORT_RETRYTest
 */

public class TRANSPORT_RETRYTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.TRANSPORT_RETRY");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == short.class, "value is short");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
