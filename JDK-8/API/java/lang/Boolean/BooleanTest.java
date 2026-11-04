/*
 * @test
 * @summary java.lang.Boolean conformance (white-box structural + black-box
 *          behavioral): TRUE/FALSE constants, parseBoolean, valueOf singleton,
 *          booleanValue, hashCode (1231/1237), equals/compareTo/compare,
 *          logicalAnd/Or/Xor, boxing, and getBoolean.
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

    static void testConstants() {
        check(Boolean.TRUE, "TRUE is true");
        check(!Boolean.FALSE, "FALSE is false");
        check(Boolean.TYPE == boolean.class, "TYPE == boolean.class");
    }

    static void testParseBoolean() {
        check(Boolean.parseBoolean("true"), "parseBoolean(true)");
        check(Boolean.parseBoolean("TRUE"), "parseBoolean(TRUE)");
        check(Boolean.parseBoolean("True"), "parseBoolean(True)");
        check(Boolean.parseBoolean("tRuE"), "parseBoolean(tRuE)");
        check(!Boolean.parseBoolean("yes"), "parseBoolean(yes)");
        check(!Boolean.parseBoolean(""), "parseBoolean(empty)");
        check(!Boolean.parseBoolean("false"), "parseBoolean(false)");
        check(!Boolean.parseBoolean(null), "parseBoolean(null) == false");
    }

    static void testValueOf() {
        check(Boolean.valueOf(true) == Boolean.TRUE, "valueOf(true) == TRUE");
        check(Boolean.valueOf(false) == Boolean.FALSE, "valueOf(false) == FALSE");
        check(Boolean.valueOf(true) == Boolean.valueOf(true), "valueOf(true) singleton");
        check(Boolean.valueOf("true") == Boolean.TRUE, "valueOf(\"true\") == TRUE");
        check(Boolean.valueOf("TRUE") == Boolean.TRUE, "valueOf(\"TRUE\") == TRUE");
        check(Boolean.valueOf("x") == Boolean.FALSE, "valueOf(\"x\") == FALSE");
    }

    static void testBooleanValueToString() {
        check(Boolean.TRUE.booleanValue(), "TRUE.booleanValue()");
        check(!Boolean.FALSE.booleanValue(), "FALSE.booleanValue()");
        check(Boolean.toString(true).equals("true"), "toString(true)");
        check(Boolean.toString(false).equals("false"), "toString(false)");
        check(Boolean.TRUE.toString().equals("true"), "TRUE.toString()");
    }

    static void testHashCodeEquals() {
        check(Boolean.TRUE.hashCode() == 1231, "hashCode(TRUE) == 1231");
        check(Boolean.FALSE.hashCode() == 1237, "hashCode(FALSE) == 1237");
        check(Boolean.hashCode(true) == 1231 && Boolean.hashCode(false) == 1237,
              "hashCode(boolean) == 1231/1237");
        check(Boolean.TRUE.equals(Boolean.TRUE), "equals same");
        check(!Boolean.TRUE.equals(Boolean.FALSE), "!equals different");
        check(!Boolean.TRUE.equals(null), "!equals null");
    }

    static void testCompare() {
        check(Boolean.TRUE.compareTo(Boolean.TRUE) == 0, "compareTo equal");
        check(Boolean.FALSE.compareTo(Boolean.TRUE) < 0, "FALSE.compareTo(TRUE) < 0");
        check(Boolean.TRUE.compareTo(Boolean.FALSE) > 0, "TRUE.compareTo(FALSE) > 0");
        check(Boolean.compare(false, true) < 0, "compare(false,true) < 0");
        check(Boolean.compare(true, false) > 0, "compare(true,false) > 0");
        check(Boolean.compare(true, true) == 0, "compare(true,true) == 0");
        check(Boolean.FALSE.compareTo(Boolean.TRUE) == -Boolean.TRUE.compareTo(Boolean.FALSE),
              "compareTo antisymmetry");
    }

    static void testLogicalCombinators() {
        check(Boolean.logicalAnd(true, true) == true && Boolean.logicalAnd(true, false) == false
              && Boolean.logicalAnd(false, true) == false && Boolean.logicalAnd(false, false) == false,
              "logicalAnd truth table");
        check(Boolean.logicalOr(false, true) == true && Boolean.logicalOr(false, false) == false
              && Boolean.logicalOr(true, false) == true && Boolean.logicalOr(true, true) == true,
              "logicalOr truth table");
        check(Boolean.logicalXor(true, false) == true && Boolean.logicalXor(true, true) == false
              && Boolean.logicalXor(false, false) == false && Boolean.logicalXor(false, true) == true,
              "logicalXor truth table");
    }

    static void testBoxing() {
        Boolean boxed = true;
        check(boxed == Boolean.TRUE, "autobox true == TRUE");
        boolean unboxed = boxed;
        check(unboxed, "unboxing recovers boolean");
        check((boxed && Boolean.FALSE) == false, "boxed logical composition");
        check(Boolean.valueOf(Boolean.parseBoolean("true")) == Boolean.TRUE,
              "valueOf(parseBoolean) consistency");
    }

    static void testGetBoolean() {
        System.setProperty("jck.bool.get", "true");
        try {
            check(Boolean.getBoolean("jck.bool.get"), "getBoolean(set property)");
            check(!Boolean.getBoolean("jck.bool.missing"), "getBoolean(missing) == false");
        } finally {
            System.clearProperty("jck.bool.get");
        }
    }

    public static void main(String[] args) {
        testWhiteBox();
        testConstants();
        testParseBoolean();
        testValueOf();
        testBooleanValueToString();
        testHashCodeEquals();
        testCompare();
        testLogicalCombinators();
        testBoxing();
        testGetBoolean();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
