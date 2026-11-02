/*
 * @test
 * @summary javax.swing.text.DefaultStyledDocument conformance: styled document (headless-safe).
 * @run main DefaultStyledDocumentTest
 */

import javax.swing.text.DefaultStyledDocument;

public class DefaultStyledDocumentTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        DefaultStyledDocument d = new DefaultStyledDocument();
        check(d.getLength() == 0, "empty");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
