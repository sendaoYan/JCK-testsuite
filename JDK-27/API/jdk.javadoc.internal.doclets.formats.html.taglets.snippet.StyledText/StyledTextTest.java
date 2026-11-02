/*
 * @test
 * @summary jdk.javadoc.internal.doclets.formats.html.taglets.snippet.StyledText conformance: internal class reflection.
 * @modules jdk.javadoc/jdk.javadoc.internal.doclets.formats.html.taglets.snippet:+open
 * @run main StyledTextTest
 */
public class StyledTextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.internal.doclets.formats.html.taglets.snippet.StyledText", false, StyledTextTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 17, "17 declared methods");
        check(c.getDeclaredFields().length == 5, "5 declared fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
