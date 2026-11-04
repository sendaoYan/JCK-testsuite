/*
 * @test
 * @summary java.lang.invoke.StringConcatFactory conformance: white-box + black-box behavior, full primitive matrix, recipe constants, linkage independence and precise StringConcatException negatives.
 * @run main StringConcatFactoryTest
 */

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.StringConcatFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class StringConcatFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.StringConcatFactory");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        int pubCtors = 0;
        for (Constructor<?> ct : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor");
        String[] names = {"makeConcat", "makeConcatWithConstants"};
        for (String n : names) {
            boolean found = false;
            for (Method mm : c.getDeclaredMethods()) if (mm.getName().equals(n)) found = true;
            check(found, "declares " + n);
        }
        check(c.getDeclaredMethod("makeConcat", MethodHandles.Lookup.class, String.class,
                      MethodType.class).getReturnType() == CallSite.class,
              "makeConcat returns CallSite");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // makeConcat: straight concatenation of dynamic arguments in order
        CallSite cs = StringConcatFactory.makeConcat(lookup, "concat",
                MethodType.methodType(String.class, String.class, int.class));
        check("a1".equals((String) cs.dynamicInvoker().invoke("a", 1)),
              "makeConcat two args");
        check("0".equals((String) cs.dynamicInvoker().invoke("", 0)),
              "empty string still shows the numeric argument");
        CallSite cs0 = StringConcatFactory.makeConcat(lookup, "concat",
                MethodType.methodType(String.class));
        check("".equals((String) cs0.dynamicInvoker().invoke()), "no-arg concat is empty");

        // every common type stringifies as String.valueOf would
        CallSite mixed = StringConcatFactory.makeConcat(lookup, "concat",
                MethodType.methodType(String.class, String.class, int.class, long.class,
                        boolean.class, char.class, double.class, Object.class));
        Object nil = null;
        String out = (String) mixed.dynamicInvoker().invoke("s", 1, 2L, true, 'c', 2.5d, nil);
        check(out.equals("s1" + 2 + true + 'c' + 2.5 + null), "mixed types: " + out);

        // makeConcatWithConstants: recipe \u0001 marks a dynamic argument slot,
        // other characters are constants
        CallSite r1 = StringConcatFactory.makeConcatWithConstants(lookup, "concat",
                MethodType.methodType(String.class, String.class, String.class),
                "<\u0001+\u0001>");
        check("<x+y>".equals((String) r1.dynamicInvoker().invoke("x", "y")),
              "recipe with two slots");
        CallSite r2 = StringConcatFactory.makeConcatWithConstants(lookup, "concat",
                MethodType.methodType(String.class, int.class, int.class, int.class),
                "\u0001\u0001\u0001");
        check("123".equals((String) r2.dynamicInvoker().invoke(1, 2, 3)),
              "three slots consume three arguments in order");
        // constant-only recipe ignores the (absent) arguments
        CallSite r3 = StringConcatFactory.makeConcatWithConstants(lookup, "concat",
                MethodType.methodType(String.class), "just text");
        check("just text".equals((String) r3.dynamicInvoker().invoke()), "constant-only recipe");

        // extra constants reachable via \u0002 placeholders
        CallSite r4 = StringConcatFactory.makeConcatWithConstants(lookup, "concat",
                MethodType.methodType(String.class, String.class),
                "\u0001=\u0002", "VALUE");
        check("a=VALUE".equals((String) r4.dynamicInvoker().invoke("a")),
              "constant \u0002 substitution");

        // the returned CallSite behaves like any call site
        check(cs.type() == MethodType.methodType(String.class, String.class, int.class),
              "call site type");
        MethodHandle inv = cs.dynamicInvoker();
        check("ab2".equals(inv.invoke("ab", 2)), "dynamicInvoker reusable");

        // recipes with too few \u0001 slots for the arguments are rejected
        try {
            StringConcatFactory.makeConcatWithConstants(lookup, "concat",
                    MethodType.methodType(String.class, int.class, int.class), "\u0001");
            check(false, "bad recipe rejected");
        } catch (java.lang.invoke.StringConcatException e) {
            check(true, "slot/arity mismatch -> StringConcatException");
        }
        // null linkage arguments are rejected up front
        try {
            StringConcatFactory.makeConcat(lookup, "c", null);
            check(false, "null concat type rejected");
        } catch (NullPointerException expected) {
            check(true, "null concat type -> NullPointerException");
        }
        // void parameter types cannot participate in concatenation
        try {
            StringConcatFactory.makeConcat(lookup, "c",
                    MethodType.methodType(String.class, void.class));
            check(false, "void parameter rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "void parameter -> IllegalArgumentException");
        }
        // the result of the concatenation must be a String
        try {
            StringConcatFactory.makeConcat(lookup, "c",
                    MethodType.methodType(void.class, int.class));
            check(false, "void return rejected");
        } catch (java.lang.invoke.StringConcatException expected) {
            check(true, "void return type -> StringConcatException");
        }
    }

    // ---- helpers ----

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r,
            String frag, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                if (t.getMessage() != null && t.getMessage().contains(frag)) {
                    passed++; return;
                }
                throw new AssertionError(msg + ": expected message fragment \"" + frag
                        + "\" but got \"" + t.getMessage() + "\"");
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static final String SLOT_1 = "\u0001"; // recipe tag: one dynamic argument
    static final String SLOT_2 = "\u0002"; // recipe tag: one constant from the constants list

    static String concat(MethodType mt, Object... args) throws Throwable {
        return (String) StringConcatFactory.makeConcat(MethodHandles.lookup(), "concat", mt)
                .dynamicInvoker().invokeWithArguments(java.util.Arrays.asList(args));
    }

    // ---- every primitive and reference conversion, exact outputs ----
    static void testPrimitiveMatrix() throws Throwable {
        check("x2".equals(concat(MethodType.methodType(String.class, String.class, int.class),
                "x", 2)), "(String,int) -> x2");
        check("2x".equals(concat(MethodType.methodType(String.class, int.class, String.class),
                2, "x")), "(int,String) -> 2x");
        check("AB".equals(concat(MethodType.methodType(String.class, char.class, char.class),
                'A', 'B')), "(char,char) -> AB");
        check("1.52.259falseZ".equals(concat(
                MethodType.methodType(String.class, float.class, double.class, long.class,
                        boolean.class, char.class),
                1.5f, 2.25d, 9L, false, 'Z')),
                "float/double/long/boolean/char sequence");
        check("12".equals(concat(MethodType.methodType(String.class, byte.class, short.class),
                (byte) 1, (short) 2)), "(byte,short) -> 12");
        Object nil = null;
        check("xnull".equals(concat(MethodType.methodType(String.class, String.class,
                Object.class), "x", nil)), "null reference renders as null");
        check("null".equals(concat(MethodType.methodType(String.class, Object.class), nil)),
                "lone null renders as null");
        check("-7true".equals(concat(MethodType.methodType(String.class, int.class,
                boolean.class), -7, true)), "negative int and true");
        StringBuilder expect = new StringBuilder();
        for (int i = 0; i < 10; i++) expect.append(i);
        Class<?>[] many = new Class<?>[10];
        for (int i = 0; i < 10; i++) many[i] = int.class;
        Object[] vals = new Integer[10];
        for (int i = 0; i < 10; i++) vals[i] = i;
        check(expect.toString().equals(concat(MethodType.methodType(String.class, many), vals)),
                "ten dynamic arguments keep their order");
    }

    // ---- makeConcatWithConstants recipe algebra ----
    static void testConstantsRecipes() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        CallSite multi = StringConcatFactory.makeConcatWithConstants(lookup, "concat",
                MethodType.methodType(String.class, String.class),
                SLOT_1 + "=" + SLOT_2 + "/" + SLOT_2, "A", "B");
        check("x=A/B".equals((String) multi.dynamicInvoker().invoke("x")),
                "multiple constants substituted in order");
        // a tag below \u0002 is ordinary text: no unknown-escape rejection
        CallSite odd = StringConcatFactory.makeConcatWithConstants(lookup, "concat",
                MethodType.methodType(String.class, String.class),
                SLOT_1 + "\u0003");
        check(("x" + "\u0003").equals((String) odd.dynamicInvoker().invoke("x")),
                "\\u0003 is a literal, not an error");
        CallSite empty = StringConcatFactory.makeConcatWithConstants(lookup, "concat",
                MethodType.methodType(String.class), "");
        check("".equals((String) empty.dynamicInvoker().invoke()), "empty recipe, no args");
        CallSite prefix = StringConcatFactory.makeConcatWithConstants(lookup, "concat",
                MethodType.methodType(String.class, int.class, int.class),
                "p" + SLOT_1 + "m" + SLOT_2 + "s" + SLOT_1 + "t", "K");
        check("p1mKs2t".equals((String) prefix.dynamicInvoker().invoke(1, 2)),
                "text and tags interleave");
        check(multi.type().equals(MethodType.methodType(String.class, String.class)),
                "with-constants CallSite type");
    }

    // ---- linkage identity and name leniency (observed) ----
    static void testLinkageIdentity() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType mt = MethodType.methodType(String.class, String.class, String.class);
        CallSite a = StringConcatFactory.makeConcat(lookup, "concat", mt);
        CallSite b = StringConcatFactory.makeConcat(lookup, "concat", mt);
        check(a != b, "same-type linkages are independent CallSites");
        check(a.type().equals(b.type()) && a.type() == mt,
                "both CallSites carry the requested type");
        check("ab".equals((String) a.dynamicInvoker().invoke("a", "b"))
                && "ab".equals((String) b.dynamicInvoker().invoke("a", "b")),
                "both linkages compute identically");
        // name is only null-checked: '.' and '' link fine (observed)
        CallSite dotted = StringConcatFactory.makeConcat(lookup, "a.b", mt);
        check("cd".equals((String) dotted.dynamicInvoker().invoke("c", "d")),
                "dotted name still links (observed)");
        CallSite bare = StringConcatFactory.makeConcat(lookup, "", mt);
        check("ef".equals((String) bare.dynamicInvoker().invoke("e", "f")),
                "empty name still links (observed)");
    }

    // ---- precise linkage-time failures ----
    static void testNegativeLinkage() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType twoStr = MethodType.methodType(String.class, String.class, String.class);
        checkThrowsMsg(NullPointerException.class,
                () -> StringConcatFactory.makeConcat(lookup, null, twoStr),
                "Name is null", "null name -> NPE");
        checkThrowsMsg(NullPointerException.class,
                () -> StringConcatFactory.makeConcatWithConstants(lookup, "c", twoStr, null, "K"),
                "Recipe is null", "null recipe -> NPE");
        checkThrowsMsg(NullPointerException.class,
                () -> StringConcatFactory.makeConcatWithConstants(lookup, "c",
                        MethodType.methodType(String.class, String.class),
                        SLOT_2, (Object[]) null),
                "Constants are null", "null constants array -> NPE");
        checkThrowsMsg(NullPointerException.class,
                () -> StringConcatFactory.makeConcatWithConstants(lookup, "c",
                        MethodType.methodType(String.class, String.class),
                        SLOT_2, new Object[] { null }),
                "Cannot accept null constants", "null constant element -> NPE");
        checkThrowsMsg(java.lang.invoke.StringConcatException.class,
                () -> StringConcatFactory.makeConcatWithConstants(lookup, "c",
                        MethodType.methodType(String.class, String.class),
                        SLOT_1 + SLOT_2, "A", "B"),
                "Mismatched number of concat constants",
                "more constants than tags -> StringConcatException");
        checkThrowsMsg(java.lang.invoke.StringConcatException.class,
                () -> StringConcatFactory.makeConcatWithConstants(lookup, "c",
                        MethodType.methodType(String.class, String.class),
                        SLOT_1 + SLOT_2 + SLOT_2, "A"),
                "Mismatched number of concat constants",
                "fewer constants than tags -> StringConcatException");
        checkThrowsMsg(java.lang.invoke.StringConcatException.class,
                () -> StringConcatFactory.makeConcatWithConstants(lookup, "c",
                        MethodType.methodType(String.class, int.class, int.class), SLOT_1),
                "Mismatched number of concat arguments",
                "slot/arity mismatch -> StringConcatException");
        checkThrowsMsg(java.lang.invoke.StringConcatException.class,
                () -> StringConcatFactory.makeConcat(lookup, "c",
                        MethodType.methodType(Integer.class, String.class, String.class)),
                "The return type should be compatible with String",
                "non-String return -> StringConcatException");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testPrimitiveMatrix();
        testConstantsRecipes();
        testLinkageIdentity();
        testNegativeLinkage();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
