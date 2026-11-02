/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.util.ClassStack conformance: behavioral construction.
 * @run main ClassStackTest
 */

public class ClassStackTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.apache.bcel.internal.util.ClassStack s = new com.sun.org.apache.bcel.internal.util.ClassStack();
        check(s.empty(), "initially empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
