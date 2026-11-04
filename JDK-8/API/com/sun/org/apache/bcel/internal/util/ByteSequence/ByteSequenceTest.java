/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.util.ByteSequence conformance: behavioral construction.
 * @run main ByteSequenceTest
 */

public class ByteSequenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.apache.bcel.internal.util.ByteSequence b = new com.sun.org.apache.bcel.internal.util.ByteSequence(new byte[]{1, 2, 3, 4});
        check(b.getIndex() == 0, "index starts 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
