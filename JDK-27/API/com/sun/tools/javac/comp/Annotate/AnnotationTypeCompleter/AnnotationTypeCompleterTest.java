/*
 * @test
 * @summary com.sun.tools.javac.comp.Annotate.AnnotationTypeCompleter conformance: internal interface reflection.
 * @modules jdk.compiler/com.sun.tools.javac.comp:+open
 * @run main AnnotationTypeCompleterTest
 */
public class AnnotationTypeCompleterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.comp.Annotate$AnnotationTypeCompleter", false, AnnotationTypeCompleterTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.comp.Annotate$AnnotationTypeCompleter"), "name");
        check(c.isInterface(), "interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
