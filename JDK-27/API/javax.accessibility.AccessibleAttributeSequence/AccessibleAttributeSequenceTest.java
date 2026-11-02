/*
 * @test
 * @summary javax.accessibility.AccessibleAttributeSequence conformance: attribute sequence.
 * @run main AccessibleAttributeSequenceTest
 */

public class AccessibleAttributeSequenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.swing.text.SimpleAttributeSet as = new javax.swing.text.SimpleAttributeSet();
        javax.accessibility.AccessibleAttributeSequence s = new javax.accessibility.AccessibleAttributeSequence(2, 7, as);
        check(s.startIndex == 2, "startIndex");
        check(s.endIndex == 7, "endIndex");
        check(s.attributes == as, "attributes");
        s.startIndex = 3; s.endIndex = 9; s.attributes = null;
        check(s.startIndex == 3, "mutable startIndex");
        check(s.endIndex == 9, "mutable endIndex");
        check(s.attributes == null, "mutable attributes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
