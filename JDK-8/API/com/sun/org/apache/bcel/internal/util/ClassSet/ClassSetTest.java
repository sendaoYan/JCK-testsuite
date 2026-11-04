/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.util.ClassSet conformance: behavioral construction.
 * @run main ClassSetTest
 */

public class ClassSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.apache.bcel.internal.util.ClassSet s = new com.sun.org.apache.bcel.internal.util.ClassSet();
        check(s.empty(), "initially empty");
        check(s.toArray().length == 0, "empty toArray");
        check(s.getClassNames().length == 0, "empty class names");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
