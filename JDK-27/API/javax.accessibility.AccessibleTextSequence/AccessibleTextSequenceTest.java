/*
 * @test
 * @summary javax.accessibility.AccessibleTextSequence conformance: text sequence.
 * @run main AccessibleTextSequenceTest
 */

public class AccessibleTextSequenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.accessibility.AccessibleTextSequence s = new javax.accessibility.AccessibleTextSequence(0, 5, "hello");
        check(s.startIndex == 0, "startIndex");
        check(s.endIndex == 5, "endIndex");
        check("hello".equals(s.text), "text");
        s.text = "bye";
        check("bye".equals(s.text), "mutable text");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
