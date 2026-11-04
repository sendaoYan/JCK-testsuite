/*
 * @test
 * @summary java.text.ListFormat.Style conformance: enum FULL/SHORT/NARROW.
 * @run main StyleTest
 */
import java.text.ListFormat;

public class StyleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(ListFormat.Style.class.isEnum(), "is enum");
        check(ListFormat.Style.FULL != null, "FULL");
        check(ListFormat.Style.SHORT != null, "SHORT");
        check(ListFormat.Style.NARROW != null, "NARROW");
        check(ListFormat.Style.values().length == 3, "three constants");
        check(ListFormat.Style.valueOf("NARROW") == ListFormat.Style.NARROW, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
