/*
 * @test
 * @summary java.text.ListFormat.Style conformance: enum FULL/SHORT/NARROW.
 * @run main StyleTest
 */
import java.text.ListFormat;

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

    static void testEnumContract() {
        ListFormat.Style[] vals = ListFormat.Style.values();
        check(vals.length == 3, "exactly three constants");
        check(vals[0] == ListFormat.Style.FULL && vals[0].ordinal() == 0, "FULL ordinal 0");
        check(vals[1] == ListFormat.Style.SHORT && vals[1].ordinal() == 1, "SHORT ordinal 1");
        check(vals[2] == ListFormat.Style.NARROW && vals[2].ordinal() == 2, "NARROW ordinal 2");
        check("FULL".equals(ListFormat.Style.FULL.name()), "name FULL");
        check("SHORT".equals(ListFormat.Style.SHORT.name()), "name SHORT");
        check("NARROW".equals(ListFormat.Style.NARROW.name()), "name NARROW");
        expectX(IllegalArgumentException.class, () -> ListFormat.Style.valueOf("bogus"),
                "valueOf(bogus) -> IAE");
        expectX(NullPointerException.class, () -> ListFormat.Style.valueOf(null),
                "valueOf(null) -> NPE");
        check(java.util.EnumSet.allOf(ListFormat.Style.class).size() == 3, "EnumSet round-trip");
    }

    static void testDrivesListFormatContract() {
        ListFormat full = ListFormat.getInstance(java.util.Locale.US, ListFormat.Type.STANDARD, ListFormat.Style.FULL);
        ListFormat shortF = ListFormat.getInstance(java.util.Locale.US, ListFormat.Type.STANDARD, ListFormat.Style.SHORT);
        ListFormat narrow = ListFormat.getInstance(java.util.Locale.US, ListFormat.Type.STANDARD, ListFormat.Style.NARROW);
        check("x and y".equals(full.format(new String[]{"x", "y"})), "FULL uses 'and'");
        check("x & y".equals(shortF.format(new String[]{"x", "y"})), "SHORT uses '&'");
        check("x, y".equals(narrow.format(new String[]{"x", "y"})), "NARROW drops connective");
        check(full.getPatterns()[3].contains("and") && shortF.getPatterns()[3].contains("&"),
              "patterns expose the style difference");
    }
    public static void main(String[] args) {
        testEnumContract();
        testDrivesListFormatContract();
        check(ListFormat.Style.class.isEnum(), "is enum");
        check(ListFormat.Style.FULL != null, "FULL");
        check(ListFormat.Style.SHORT != null, "SHORT");
        check(ListFormat.Style.NARROW != null, "NARROW");
        check(ListFormat.Style.values().length == 3, "three constants");
        check(ListFormat.Style.valueOf("NARROW") == ListFormat.Style.NARROW, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
