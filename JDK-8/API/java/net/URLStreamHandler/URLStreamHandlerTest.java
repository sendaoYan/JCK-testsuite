/*
 * @test
 * @summary java.net.URLStreamHandler conformance: abstract class hierarchy and members.
 * @run main URLStreamHandlerTest
 */


public class URLStreamHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.URLStreamHandler");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");        System.out.println("PASS (" + passed + " assertions)");
    }
}
