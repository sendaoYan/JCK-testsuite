/*
 * @test
 * @summary java.awt.Font conformance: family, style, size.
 * @run main FontTest
 */


import java.awt.Font;
public class FontTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        Font f = new Font("Dialog", Font.BOLD | Font.ITALIC, 14);
        check(f.getName().equals("Dialog"), "family name");
        check(f.getSize() == 14, "size");
        check(f.isBold() && f.isItalic(), "bold+italic");
        check(!f.isPlain(), "not plain");
        check(f.getStyle() == (Font.BOLD | Font.ITALIC), "style mask");
        check(Font.SERIF.equals("Serif"), "SERIF constant");
        Font plain = f.deriveFont(Font.PLAIN);
        check(plain.isPlain() && plain.getSize() == 14, "deriveFont style");
        Font bigger = f.deriveFont(20f);
        check(bigger.getSize() == 20, "deriveFont size");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
