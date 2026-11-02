/*
 * @test
 * @summary jdk.javadoc.internal.html.HtmlId conformance: internal interface reflection.
 * @modules jdk.javadoc/jdk.javadoc.internal.html:+open
 * @run main HtmlIdTest
 */
public class HtmlIdTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.internal.html.HtmlId", false, HtmlIdTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 3, "3 declared methods");
        check(c.getDeclaredFields().length == 0, "0 declared fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
