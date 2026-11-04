/*
 * @test
 * @summary java.text.ListFormat.Type conformance: enum STANDARD/OR/UNIT.
 * @run main TypeTest
 */
import java.text.ListFormat;

public class TypeTest {
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
        ListFormat.Type[] vals = ListFormat.Type.values();
        check(vals.length == 3, "exactly three constants");
        check(vals[0] == ListFormat.Type.STANDARD && vals[0].ordinal() == 0, "STANDARD ordinal 0");
        check(vals[1] == ListFormat.Type.OR && vals[1].ordinal() == 1, "OR ordinal 1");
        check(vals[2] == ListFormat.Type.UNIT && vals[2].ordinal() == 2, "UNIT ordinal 2");
        check("STANDARD".equals(ListFormat.Type.STANDARD.name()), "name STANDARD");
        check("OR".equals(ListFormat.Type.OR.name()), "name OR");
        check("UNIT".equals(ListFormat.Type.UNIT.name()), "name UNIT");
        expectX(IllegalArgumentException.class, () -> ListFormat.Type.valueOf("bogus"),
                "valueOf(bogus) -> IAE");
        expectX(NullPointerException.class, () -> ListFormat.Type.valueOf(null),
                "valueOf(null) -> NPE");
        check(java.util.EnumSet.allOf(ListFormat.Type.class).size() == 3, "EnumSet round-trip");
    }

    static void testDrivesListFormatContract() {
        ListFormat std = ListFormat.getInstance(java.util.Locale.US, ListFormat.Type.STANDARD, ListFormat.Style.FULL);
        ListFormat or = ListFormat.getInstance(java.util.Locale.US, ListFormat.Type.OR, ListFormat.Style.FULL);
        ListFormat unit = ListFormat.getInstance(java.util.Locale.US, ListFormat.Type.UNIT, ListFormat.Style.FULL);
        check("x and y".equals(std.format(new String[]{"x", "y"})), "STANDARD conjunction");
        check("x or y".equals(or.format(new String[]{"x", "y"})), "OR disjunction");
        check("x, y".equals(unit.format(new String[]{"x", "y"})), "UNIT enumeration");
        check(std.getPatterns()[3].contains("and") && or.getPatterns()[3].contains("or"),
              "patterns expose the type difference");
        check("x y".equals(ListFormat.getInstance(java.util.Locale.US, ListFormat.Type.UNIT, ListFormat.Style.NARROW)
                .format(new String[]{"x", "y"})), "UNIT/NARROW joins with a single space");
    }
    public static void main(String[] args) {
        testEnumContract();
        testDrivesListFormatContract();
        check(ListFormat.Type.class.isEnum(), "is enum");
        check(ListFormat.Type.STANDARD != null, "STANDARD");
        check(ListFormat.Type.OR != null, "OR");
        check(ListFormat.Type.UNIT != null, "UNIT");
        check(ListFormat.Type.values().length == 3, "three constants");
        check(ListFormat.Type.valueOf("OR") == ListFormat.Type.OR, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
