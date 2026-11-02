/*
 * @test
 * @summary java.awt.Insets conformance: top/left/bottom/right.
 * @run main InsetsTest
 */


import java.awt.Insets;
public class InsetsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        Insets i = new Insets(1, 2, 3, 4);
        check(i.top == 1 && i.left == 2 && i.bottom == 3 && i.right == 4, "fields");
        check(i.equals(new Insets(1, 2, 3, 4)), "equals");
        Insets c = (Insets) i.clone();
        check(c.top == 1 && c.left == 2 && c.bottom == 3 && c.right == 4, "clone");
        check(!i.equals(new Insets(0, 0, 0, 0)), "not equal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
