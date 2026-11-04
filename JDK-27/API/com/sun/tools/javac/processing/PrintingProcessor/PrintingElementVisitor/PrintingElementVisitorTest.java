/*
 * @test
 * @summary com.sun.tools.javac.processing.PrintingProcessor.PrintingElementVisitor conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.processing:+open
 * @run main PrintingElementVisitorTest
 */
public class PrintingElementVisitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.processing.PrintingProcessor$PrintingElementVisitor", false, PrintingElementVisitorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.processing.PrintingProcessor$PrintingElementVisitor"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.lang.model.util.SimpleElementVisitor14"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
