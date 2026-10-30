/*
 * @test
 * @summary java.lang.Boolean API conformance: TRUE/FALSE constants, parseBoolean,
 *          valueOf (singleton caching), booleanValue, hashCode (1231/1237),
 *          equals/compareTo/compare (false < true), toString, and getBoolean.
 * @run main BooleanTest
 */

public class BooleanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(Boolean.TRUE, "TRUE is true");
        check(!Boolean.FALSE, "FALSE is false");
        check(Boolean.TYPE == boolean.class, "TYPE == boolean.class");

        // parseBoolean: case-insensitive "true", everything else false (never NPE).
        check(Boolean.parseBoolean("true"), "parseBoolean(true)");
        check(Boolean.parseBoolean("TRUE"), "parseBoolean(TRUE)");
        check(Boolean.parseBoolean("True"), "parseBoolean(True)");
        check(!Boolean.parseBoolean("yes"), "parseBoolean(yes)");
        check(!Boolean.parseBoolean(""), "parseBoolean(empty)");
        check(!Boolean.parseBoolean("false"), "parseBoolean(false)");
        check(!Boolean.parseBoolean(null), "parseBoolean(null) == false");

        // valueOf(boolean) returns the singleton TRUE/FALSE; valueOf(String) parses.
        check(Boolean.valueOf(true) == Boolean.TRUE, "valueOf(true) == TRUE");
        check(Boolean.valueOf(false) == Boolean.FALSE, "valueOf(false) == FALSE");
        check(Boolean.valueOf(true) == Boolean.valueOf(true), "valueOf(true) singleton");
        check(Boolean.valueOf("true") == Boolean.TRUE, "valueOf(\"true\") == TRUE");
        check(Boolean.valueOf("TRUE") == Boolean.TRUE, "valueOf(\"TRUE\") == TRUE");
        check(Boolean.valueOf("x") == Boolean.FALSE, "valueOf(\"x\") == FALSE");

        // booleanValue / toString.
        check(Boolean.TRUE.booleanValue(), "TRUE.booleanValue()");
        check(!Boolean.FALSE.booleanValue(), "FALSE.booleanValue()");
        check(Boolean.toString(true).equals("true"), "toString(true)");
        check(Boolean.toString(false).equals("false"), "toString(false)");
        check(Boolean.TRUE.toString().equals("true"), "TRUE.toString()");

        // hashCode contract: 1231 for true, 1237 for false.
        check(Boolean.TRUE.hashCode() == 1231, "hashCode(TRUE) == 1231");
        check(Boolean.FALSE.hashCode() == 1237, "hashCode(FALSE) == 1237");

        // equals / compareTo / compare: false is ordered before true.
        check(Boolean.TRUE.equals(Boolean.TRUE), "equals same");
        check(!Boolean.TRUE.equals(Boolean.FALSE), "!equals different");
        check(!Boolean.TRUE.equals(null), "!equals null");
        check(Boolean.TRUE.compareTo(Boolean.TRUE) == 0, "compareTo equal");
        check(Boolean.FALSE.compareTo(Boolean.TRUE) < 0, "FALSE.compareTo(TRUE) < 0");
        check(Boolean.TRUE.compareTo(Boolean.FALSE) > 0, "TRUE.compareTo(FALSE) > 0");
        check(Boolean.compare(false, true) < 0, "compare(false,true) < 0");
        check(Boolean.compare(true, false) > 0, "compare(true,false) > 0");
        check(Boolean.compare(true, true) == 0, "compare(true,true) == 0");

        // getBoolean reads a system property and defaults to false.
        System.setProperty("jck.bool.get", "true");
        check(Boolean.getBoolean("jck.bool.get"), "getBoolean(set property)");
        check(!Boolean.getBoolean("jck.bool.missing"), "getBoolean(missing) == false");
        System.clearProperty("jck.bool.get");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
