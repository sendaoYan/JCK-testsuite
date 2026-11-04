/*
 * @test
 * @summary java.lang.Boolean conformance (white-box structural + black-box
 *          behavioral): TRUE/FALSE constants, parseBoolean, valueOf singleton,
 *          booleanValue, hashCode (1231/1237), equals/compareTo/compare,
 *          logicalAnd/Or/Xor, boxing, and getBoolean.
 *
 * Design case
 * -----------
 * 1. Contract: Boolean is an immutable wrapper with exactly two singleton instances,
 *    TRUE and FALSE; parseBoolean is case-insensitive for "true" and never throws.
 * 2. Usage: valueOf(boolean) -> booleanValue; valueOf(String) -> parse; toString;
 *    compare/compareTo order false before true; logicalAnd/Or/Xor combine values.
 * 3. Composition: valueOf(parseBoolean(s)).booleanValue() round-trip; autobox/unbox;
 *    logicalAnd/Or/Xor -> compareTo -> equals/hashCode.
 * 4. Invariants: TRUE/FALSE singletons; equals by boolean value only; hashCode 1231/1237.
 * 5. Boundary/exception: parseBoolean(null)==false (no NPE); getBoolean defaults false.
 * @run main BooleanTest
 */

public class BooleanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- White-box / structural ----
    static void testWhiteBox() {
        try {
            Class<?> c = Class.forName("java.lang.Boolean");
            check(!c.isInterface(), "Boolean is not an interface");
            check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Boolean is final");
            check(c.getSuperclass() == Object.class, "Boolean extends Object directly");
            check(Comparable.class.isAssignableFrom(c), "Boolean implements Comparable");
            check(c.getField("TRUE").getType() == Boolean.class
                  && java.lang.reflect.Modifier.isStatic(c.getField("TRUE").getModifiers()),
                  "TRUE is public static final Boolean");
            check(c.getMethod("parseBoolean", String.class).getReturnType() == boolean.class,
                  "parseBoolean(String) returns boolean");
            check(c.getMethod("valueOf", boolean.class).getReturnType() == Boolean.class,
                  "valueOf(boolean) returns Boolean");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    // ---- Black-box / behavioral ----
    static void testBlackBox() {
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

        // Logical combinators (Java 8+).
        check(Boolean.logicalAnd(true, true) == true && Boolean.logicalAnd(true, false) == false,
              "logicalAnd");
        check(Boolean.logicalOr(false, true) == true && Boolean.logicalOr(false, false) == false,
              "logicalOr");
        check(Boolean.logicalXor(true, false) == true && Boolean.logicalXor(true, true) == false,
              "logicalXor");

        // Autoboxing/unboxing: literal -> valueOf(boolean), and back via booleanValue().
        Boolean boxed = true;
        check(boxed == Boolean.TRUE, "autobox true == TRUE");
        boolean unboxed = boxed;
        check(unboxed, "unboxing recovers boolean");
        check((boxed && Boolean.FALSE) == false, "boxed logical composition");

        // getBoolean reads a system property and defaults to false.
        System.setProperty("jck.bool.get", "true");
        try {
            check(Boolean.getBoolean("jck.bool.get"), "getBoolean(set property)");
            check(!Boolean.getBoolean("jck.bool.missing"), "getBoolean(missing) == false");
        } finally {
            System.clearProperty("jck.bool.get");
        }
        testBoundaryEquivalence();
    }

    static void testBoundaryEquivalence() {
        // parseBoolean accepts exactly the case-insensitive spellings of "true".
        for (String s : new String[]{"true", "TRUE", "True", "tRuE"}) {
            check(Boolean.parseBoolean(s), "parseBoolean(" + s + ")");
        }

        // Logical combinator truth table.
        check(Boolean.logicalAnd(true, false) == false, "and(T,F)");
        check(Boolean.logicalAnd(false, true) == false, "and(F,T)");
        check(Boolean.logicalAnd(false, false) == false, "and(F,F)");
        check(Boolean.logicalOr(true, false) == true, "or(T,F)");
        check(Boolean.logicalOr(false, true) == true, "or(F,T)");
        check(Boolean.logicalOr(false, false) == false, "or(F,F)");
        check(Boolean.logicalXor(false, false) == false, "xor(F,F)");
        check(Boolean.logicalXor(true, true) == false, "xor(T,T)");
        check(Boolean.logicalXor(false, true) == true, "xor(F,T)");
        check(Boolean.logicalXor(true, false) == true, "xor(T,F)");

        // compareTo antisymmetry.
        check(Boolean.FALSE.compareTo(Boolean.TRUE) == -Boolean.TRUE.compareTo(Boolean.FALSE),
              "compareTo antisymmetry");

        // valueOf(parseBoolean) consistency.
        check(Boolean.valueOf(Boolean.parseBoolean("true")) == Boolean.TRUE,
              "valueOf(parseBoolean) consistency");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
