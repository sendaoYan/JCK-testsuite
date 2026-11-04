/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.compiler.OpMapVector conformance: behavioral construction.
 * @run main OpMapVectorTest
 */

public class OpMapVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.apache.xpath.internal.compiler.OpMapVector v = new com.sun.org.apache.xpath.internal.compiler.OpMapVector(4, 4, 4);
        v.setElementAt(42, 0);
        check(v.elementAt(0) == 42, "setElementAt/elementAt");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
