/*
 * @test
 * @summary java.lang.invoke.StringConcatFactory conformance: white-box + black-box behavior.
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

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
