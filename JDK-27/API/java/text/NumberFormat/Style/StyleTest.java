/*
 * @test
 * @summary java.text.NumberFormat.Style conformance: enum SHORT/LONG.
 * @run main StyleTest
 */
import java.text.NumberFormat;

public class StyleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }


    // ---- deepening helper (contract-level additions) ----
    interface TestBlock { void go() throws Exception; }

    static void expectX(Class<? extends Throwable> expected, TestBlock r, String m) {
        try {
            r.go();
        } catch (Throwable x) {
            if (expected.isInstance(x)) { passed++; return; }
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + x.getClass().getName(), x);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }


    // ================= contract deepening =================

    static void testContractDeepening() {
        NumberFormat.Style[] vals = NumberFormat.Style.values();
        check(vals.length == 2, "exactly 2 constants");
        check(vals[0] == NumberFormat.Style.SHORT && vals[0].ordinal() == 0, "SHORT ordinal 0");
        check(vals[1] == NumberFormat.Style.LONG && vals[1].ordinal() == 1, "LONG ordinal 1");
        try {
            NumberFormat.Style.valueOf("short");
            check(false, "valueOf is case-sensitive");
        } catch (IllegalArgumentException expected) { passed++; }
        check("1K".equals(NumberFormat.getCompactNumberInstance(java.util.Locale.US,
                NumberFormat.Style.SHORT).format(1234)), "SHORT drives compact suffixes");
        check("1 thousand".equals(NumberFormat.getCompactNumberInstance(java.util.Locale.US,
                NumberFormat.Style.LONG).format(1234)), "LONG drives spelled-out scales");
    }
    public static void main(String[] args) {
        testContractDeepening();
        check(NumberFormat.Style.class.isEnum(), "is enum");
        check(NumberFormat.Style.SHORT != null, "SHORT");
        check(NumberFormat.Style.LONG != null, "LONG");
        check(NumberFormat.Style.values().length == 2, "two constants");
        check(NumberFormat.Style.valueOf("LONG") == NumberFormat.Style.LONG, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
