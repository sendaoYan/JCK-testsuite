/*
 * @test
 * @summary java.lang.instrument.ClassDefinition conformance: class and byte[] accessors.
 * @run main ClassDefinitionTest
 */

import java.lang.instrument.ClassDefinition;

public class ClassDefinitionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testClassDefinition() {
        byte[] bytes = new byte[] {1, 2, 3};
        ClassDefinition cd = new ClassDefinition(Object.class, bytes);
        check(cd.getDefinitionClass() == Object.class, "getDefinitionClass");
        check(cd.getDefinitionClassFile().length == 3, "getDefinitionClassFile length");
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
        testClassDefinition();
        testDeepNullArguments();
        testDeepArraySharingSemantics();
        testDeepValueSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
