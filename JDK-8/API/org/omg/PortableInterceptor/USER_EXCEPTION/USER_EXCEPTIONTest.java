/*
 * @test
 * @summary org.omg.PortableInterceptor.USER_EXCEPTION conformance: constant holder interface.
 * @run main USER_EXCEPTIONTest
 */

public class USER_EXCEPTIONTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.USER_EXCEPTION");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == short.class, "value is short");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
