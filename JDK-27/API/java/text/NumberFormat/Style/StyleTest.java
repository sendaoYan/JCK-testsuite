/*
 * @test
 * @summary java.text.NumberFormat.Style conformance: enum SHORT/LONG.
 * @run main StyleTest
 */
import java.text.NumberFormat;

public class StyleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(NumberFormat.Style.class.isEnum(), "is enum");
        check(NumberFormat.Style.SHORT != null, "SHORT");
        check(NumberFormat.Style.LONG != null, "LONG");
        check(NumberFormat.Style.values().length == 2, "two constants");
        check(NumberFormat.Style.valueOf("LONG") == NumberFormat.Style.LONG, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
