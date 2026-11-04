/*
 * @test
 * @summary java.text.NumberFormat.Field conformance: constants/equality/hashCode.
 * @run main FieldTest
 */

import java.text.NumberFormat;

public class FieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testField() {
        NumberFormat.Field integer = NumberFormat.Field.INTEGER;
        check(integer != null, "INTEGER");
        check(NumberFormat.Field.FRACTION != null, "FRACTION");
        check(NumberFormat.Field.EXPONENT != null, "EXPONENT");
        check(NumberFormat.Field.SIGN != null, "SIGN");
        check(NumberFormat.Field.DECIMAL_SEPARATOR != null, "DECIMAL_SEPARATOR");
        check(NumberFormat.Field.GROUPING_SEPARATOR != null, "GROUPING_SEPARATOR");
        check(NumberFormat.Field.PERCENT != null, "PERCENT");
        check(NumberFormat.Field.PERMILLE != null, "PERMILLE");
        check(NumberFormat.Field.CURRENCY != null, "CURRENCY");

        check(integer.equals(NumberFormat.Field.INTEGER), "equals");
        check(integer.hashCode() == NumberFormat.Field.INTEGER.hashCode(), "hashCode");
        check(!integer.equals(NumberFormat.Field.FRACTION), "distinct fields not equal");
        check(integer.toString() != null, "toString");
    }


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

    static void testContractDeepening() throws Exception {
        java.util.Set<Object> ids = new java.util.HashSet<>();
        String[] names = {"INTEGER", "FRACTION", "EXPONENT", "DECIMAL_SEPARATOR", "SIGN",
                "GROUPING_SEPARATOR", "PERCENT", "PERMILLE", "CURRENCY", "EXPONENT_SIGN",
                "EXPONENT_SYMBOL"};
        for (String n : names)
            try {
                Object v = java.text.NumberFormat.Field.class.getField(n).get(null);
                check(ids.add(v), n + " distinct identity");
            } catch (NoSuchFieldException e) { throw new AssertionError(n + " missing"); }
        check(java.text.NumberFormat.Field.class.getSuperclass().equals(java.text.Format.Field.class),
              "NumberFormat.Field extends Format.Field directly");
        java.text.DecimalFormat f = new java.text.DecimalFormat("#,##0.0",
                java.text.DecimalFormatSymbols.getInstance(java.util.Locale.US));
        java.text.AttributedCharacterIterator it = f.formatToCharacterIterator(1234.5);
        boolean integer = false, fraction = false, group = false;
        for (int i = it.getBeginIndex(); i < it.getEndIndex(); i++) {
            it.setIndex(i);
            integer |= it.getAttribute(java.text.NumberFormat.Field.INTEGER) != null;
            fraction |= it.getAttribute(java.text.NumberFormat.Field.FRACTION) != null;
            group |= it.getAttribute(java.text.NumberFormat.Field.GROUPING_SEPARATOR) != null;
        }
        check(integer && fraction && group,
              "INTEGER, FRACTION and GROUPING_SEPARATOR runs cover 1,234.5");
    }
    public static void main(String[] args) throws Exception {
        testContractDeepening();
        testField();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
