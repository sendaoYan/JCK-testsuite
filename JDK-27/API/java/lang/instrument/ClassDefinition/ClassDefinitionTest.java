/*
 * @test
 * @summary java.lang.instrument.ClassDefinition conformance: white-box structure + black-box behavior.
 * @run main ClassDefinitionTest
 */
import java.lang.instrument.ClassDefinition;
import java.lang.reflect.Modifier;

public class ClassDefinitionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.instrument.ClassDefinition");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(c.getConstructor(Class.class, byte[].class) != null, "constructor (Class, byte[])");
        check(c.getMethod("getDefinitionClass").getReturnType() == Class.class, "getDefinitionClass returns Class");
        check(c.getMethod("getDefinitionClassFile").getReturnType() == byte[].class, "getDefinitionClassFile returns byte[]");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        byte[] bytes = {1, 2, 3, 4, 5};
        ClassDefinition cd = new ClassDefinition(String.class, bytes);
        check(cd.getDefinitionClass() == String.class, "getDefinitionClass returns the class");
        byte[] got = cd.getDefinitionClassFile();
        check(got.length == bytes.length, "getDefinitionClassFile length preserved");
        boolean equal = true;
        for (int i = 0; i < bytes.length; i++) equal &= (got[i] == bytes[i]);
        check(equal, "getDefinitionClassFile content preserved");

        ClassDefinition empty = new ClassDefinition(Integer.class, new byte[0]);
        check(empty.getDefinitionClass() == Integer.class, "empty class file still keeps class");
        check(empty.getDefinitionClassFile().length == 0, "empty class file length 0");
    }


    static void expectIAE(Runnable op, String what, String... fragments) {
        try {
            op.run();
            check(false, what + " should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            check(true, what + " throws IllegalArgumentException");
            for (String f : fragments) {
                check(e.getMessage() != null && e.getMessage().contains(f),
                      what + " message contains '" + f + "' (got: " + e.getMessage() + ")");
            }
        }
    }

    static void expectNPE(Runnable op, String what, String... fragments) {
        try {
            op.run();
            check(false, what + " should throw NullPointerException");
        } catch (NullPointerException e) {
            check(true, what + " throws NullPointerException");
            for (String f : fragments) {
                check(e.getMessage() == null || e.getMessage().contains(f),
                      what + " message contains '" + f + "' or is null (got: " + e.getMessage() + ")");
            }
        }
    }

    static void expectUOE(Runnable op, String what, String... fragments) {
        try {
            op.run();
            check(false, what + " should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            check(true, what + " throws UnsupportedOperationException");
            for (String f : fragments) {
                check(e.getMessage() != null && e.getMessage().contains(f),
                      what + " message contains '" + f + "' (got: " + e.getMessage() + ")");
            }
        }
    }

    static void testDeepNullArguments() {
        final byte[] bytes = {1, 2, 3};
        expectNPE(new Runnable() { public void run() { new ClassDefinition(null, bytes); } },
                  "ClassDefinition(null, byte[])");
        expectNPE(new Runnable() { public void run() { new ClassDefinition(String.class, null); } },
                  "ClassDefinition(Class, null)");
    }

    static void testDeepArraySharingSemantics() {
        // No defensive copy in either direction (probed and locked):
        // mutating the caller's array is visible through getDefinitionClassFile(),
        // and the very same array reference is returned on every call.
        byte[] bytes = {9, 8, 7};
        ClassDefinition cd = new ClassDefinition(String.class, bytes);
        bytes[0] = 1;
        check(cd.getDefinitionClassFile()[0] == 1,
              "no defensive copy on construction: caller mutation is visible");
        byte[] a = cd.getDefinitionClassFile();
        byte[] b = cd.getDefinitionClassFile();
        check(a == b, "getDefinitionClassFile returns the same array reference each call");
        a[2] = 42;
        check(cd.getDefinitionClassFile()[2] == 42, "no defensive copy on read either");
    }

    static void testDeepValueSemantics() {
        ClassDefinition c1 = new ClassDefinition(String.class, new byte[]{1});
        ClassDefinition c2 = new ClassDefinition(String.class, new byte[]{1});
        check(c1.equals(c1), "equals self (no value equals override)");
        check(!c1.equals(c2), "identity semantics: equal-looking instances differ");
        check(!c1.equals(null), "equals(null) false");
        check(c1.toString() != null, "toString non-null");
        check(c1.hashCode() == c1.hashCode(), "hashCode stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepNullArguments();
        testDeepArraySharingSemantics();
        testDeepValueSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
