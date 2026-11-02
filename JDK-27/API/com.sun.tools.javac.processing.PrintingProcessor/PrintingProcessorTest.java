/*
 * @test
 * @summary com.sun.tools.javac.processing.PrintingProcessor conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.processing:+open
 * @run main PrintingProcessorTest
 */
public class PrintingProcessorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.processing.PrintingProcessor", false, PrintingProcessorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.processing.PrintingProcessor"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.annotation.processing.AbstractProcessor"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
