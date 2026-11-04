/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.util.ClassVector conformance: behavioral construction.
 * @run main ClassVectorTest
 */

public class ClassVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.apache.bcel.internal.util.ClassVector v = new com.sun.org.apache.bcel.internal.util.ClassVector();
        check(v.toArray().length == 0, "empty toArray");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
