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

    // ---- Contract deepening (2026-11): parse matrix, exact compare, singleton
    // identity, logical truth tables, hashCode/toString forms, getBoolean
    // lifecycle with try/finally restore, unboxing NPE.
    static void expectNPE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void testParseMatrix() {
        check(Boolean.parseBoolean("TRue"), "parseBoolean(TRue) mixed case");
        check(Boolean.parseBoolean("tRUe"), "parseBoolean(tRUe) mixed case");
        check(!Boolean.parseBoolean("true1"), "parseBoolean(true1) trailing junk -> false");
        check(!Boolean.parseBoolean("1"), "parseBoolean(1) -> false");
        check(!Boolean.parseBoolean("0"), "parseBoolean(0) -> false");
        check(!Boolean.parseBoolean(" true"), "parseBoolean(' true') no trim");
        check(!Boolean.parseBoolean("true "), "parseBoolean('true ') no trim");
        check(!Boolean.parseBoolean("null"), "parseBoolean(null string) -> false");
        check(!Boolean.parseBoolean("FALSE"), "parseBoolean(FALSE)");
        check(!Boolean.parseBoolean("False"), "parseBoolean(False)");
        check(!Boolean.parseBoolean("yes"), "parseBoolean(yes)");
        check(Boolean.valueOf("tRuE") == Boolean.TRUE, "valueOf(tRuE) == TRUE");
        check(Boolean.valueOf("TRue") == Boolean.TRUE, "valueOf(TRue) == TRUE");
        check(Boolean.valueOf("False") == Boolean.FALSE, "valueOf(False) == FALSE");
        check(Boolean.valueOf("FALSE") == Boolean.FALSE, "valueOf(FALSE) == FALSE");
        check(Boolean.valueOf(" true") == Boolean.FALSE, "valueOf(' true') no trim");
        check(Boolean.valueOf("true1") == Boolean.FALSE, "valueOf(true1) == FALSE");
        // valueOf(parseBoolean(s)).booleanValue() round-trips for any input.
        String[] inputs = {"true", "TRUE", "x", "", "false"};
        for (String s : inputs) {
            check(Boolean.valueOf(Boolean.parseBoolean(s)).booleanValue()
                  == Boolean.parseBoolean(s), "valueOf(parseBoolean) round-trip " + s);
        }
    }

    static void testCompareExact() {
        check(Boolean.compare(false, true) == -1, "compare(false,true) == -1 exactly");
        check(Boolean.compare(true, false) == 1, "compare(true,false) == +1 exactly");
        check(Boolean.compare(true, true) == 0, "compare(true,true) == 0");
        check(Boolean.compare(false, false) == 0, "compare(false,false) == 0");
        check(Boolean.TRUE.compareTo(Boolean.FALSE) == 1, "TRUE.compareTo(FALSE) == 1 exactly");
        check(Boolean.FALSE.compareTo(Boolean.TRUE) == -1, "FALSE.compareTo(TRUE) == -1 exactly");
        check(Boolean.TRUE.compareTo(Boolean.TRUE) == 0, "TRUE.compareTo(TRUE) == 0");
        expectNPE(() -> Boolean.TRUE.compareTo(null), "compareTo(null) throws NPE");
    }

    static void testSingletonAndBoxing() {
        // The constructor always makes a fresh instance; valueOf never does.
        check(new Boolean(true) != Boolean.TRUE, "new Boolean(true) distinct from TRUE");
        check(new Boolean(true).equals(Boolean.TRUE), "new Boolean(true) equals TRUE");
        check(new Boolean(true).booleanValue(), "new Boolean(true).booleanValue()");
        check(new Boolean(false) != Boolean.FALSE, "new Boolean(false) distinct from FALSE");
        check(Boolean.TRUE.equals(Boolean.valueOf("true")), "TRUE equals valueOf(\"true\")");
        check(!Boolean.TRUE.equals("true"), "equals is type-safe (never equals String)");
        check(!Boolean.TRUE.equals(Integer.valueOf(1)), "never equals Integer");
        check(Boolean.FALSE.equals(Boolean.FALSE), "FALSE equals FALSE");
        check(Boolean.TRUE.hashCode() == Boolean.TRUE.hashCode(), "hashCode stable");
        check(Boolean.TYPE.isPrimitive(), "TYPE is primitive");
        check(Boolean.TRUE instanceof Boolean, "TRUE instanceof Boolean");
        check(Boolean.class.isInstance(Boolean.FALSE), "class.isInstance(FALSE)");
        final Boolean nullBox = null;
        expectNPE(() -> {
            if (nullBox) {
                throw new AssertionError("unreachable");
            }
        }, "unboxing null Boolean in condition throws NPE");
        for (boolean b : new boolean[]{true, false}) {
            check(Boolean.valueOf(b) == (b ? Boolean.TRUE : Boolean.FALSE),
                  "valueOf(b) singleton for " + b);
            check(Boolean.toString(b).equals(b ? "true" : "false"), "toString(b) " + b);
            check(Boolean.hashCode(b) == (b ? 1231 : 1237), "hashCode(b) " + b);
            check(Boolean.valueOf(b).booleanValue() == b, "booleanValue round-trip " + b);
            check(Boolean.valueOf(b).compareTo(b ? Boolean.TRUE : Boolean.FALSE) == 0,
                  "compareTo self " + b);
        }
    }

    static void testLogicalMatrixFull() {
        for (boolean a : new boolean[]{true, false}) {
            for (boolean b : new boolean[]{true, false}) {
                check(Boolean.logicalAnd(a, b) == (a && b), "logicalAnd truth table " + a + "," + b);
                check(Boolean.logicalOr(a, b) == (a || b), "logicalOr truth table " + a + "," + b);
                check(Boolean.logicalXor(a, b) == (a ^ b), "logicalXor truth table " + a + "," + b);
                check(Boolean.compare(a, b) == (a == b ? 0 : (a ? 1 : -1)),
                      "compare truth table " + a + "," + b);
            }
        }
    }

    static void testGetBooleanLifecycle() {
        String old = System.getProperty("lcB.bool.key");
        try {
            System.setProperty("lcB.bool.key", "true");
            check(Boolean.getBoolean("lcB.bool.key"), "getBoolean(value true)");
            System.setProperty("lcB.bool.key", "TRUE");
            check(Boolean.getBoolean("lcB.bool.key"), "getBoolean(value TRUE) case-insensitive");
            System.setProperty("lcB.bool.key", "false");
            check(!Boolean.getBoolean("lcB.bool.key"), "getBoolean(value false)");
            System.setProperty("lcB.bool.key", "0");
            check(!Boolean.getBoolean("lcB.bool.key"), "getBoolean(value 0)");
            System.clearProperty("lcB.bool.key");
            check(!Boolean.getBoolean("lcB.bool.key"), "getBoolean(cleared)");
            check(!Boolean.getBoolean("lcB.bool.never.set"), "getBoolean(never set)");
            check(Boolean.valueOf(Boolean.getBoolean("lcB.bool.key")) == Boolean.FALSE,
                  "valueOf(getBoolean) composition");
        } finally {
            if (old == null) {
                System.clearProperty("lcB.bool.key");
            } else {
                System.setProperty("lcB.bool.key", old);
            }
        }
    }

    static void testToStringForms() {
        check(String.valueOf(Boolean.TRUE).equals("true"), "String.valueOf(TRUE)");
        check(String.valueOf(Boolean.FALSE).equals("false"), "String.valueOf(FALSE)");
        check(("b=" + Boolean.TRUE).equals("b=true"), "concatenation uses toString");
        check(Boolean.TRUE.toString().length() == 4 && Boolean.FALSE.toString().length() == 5,
              "toString lengths true/false");
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
        testParseMatrix();
        testCompareExact();
        testSingletonAndBoxing();
        testLogicalMatrixFull();
        testGetBooleanLifecycle();
        testToStringForms();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
