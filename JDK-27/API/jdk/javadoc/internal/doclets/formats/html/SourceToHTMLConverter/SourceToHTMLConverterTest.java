/*
 * @test
 * @summary jdk.javadoc.internal.doclets.formats.html.SourceToHTMLConverter conformance: internal class reflection.
 * @modules jdk.javadoc/jdk.javadoc.internal.doclets.formats.html:+open
 * @run main SourceToHTMLConverterTest
 */
public class SourceToHTMLConverterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.internal.doclets.formats.html.SourceToHTMLConverter", false, SourceToHTMLConverterTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 11, "11 declared methods");
        check(c.getDeclaredFields().length == 9, "9 declared fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
