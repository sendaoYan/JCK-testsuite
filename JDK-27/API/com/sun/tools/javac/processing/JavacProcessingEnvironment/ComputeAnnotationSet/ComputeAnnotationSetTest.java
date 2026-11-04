/*
 * @test
 * @summary com.sun.tools.javac.processing.JavacProcessingEnvironment.ComputeAnnotationSet conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.processing:+open
 * @run main ComputeAnnotationSetTest
 */
public class ComputeAnnotationSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.processing.JavacProcessingEnvironment$ComputeAnnotationSet", false, ComputeAnnotationSetTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.processing.JavacProcessingEnvironment$ComputeAnnotationSet"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.lang.model.util.ElementScanner14"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
