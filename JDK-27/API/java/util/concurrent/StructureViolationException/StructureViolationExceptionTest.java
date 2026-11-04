/*
 * @test
 * @summary java.util.concurrent.StructureViolationException conformance: white-box structure + black-box behavior.
 * @enablePreview
 * @run main StructureViolationExceptionTest
 */

import java.util.concurrent.*;

public class StructureViolationExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasCtor(Class<?> c, Class<?>... p) {
        try { c.getConstructor(p); } catch (NoSuchMethodException e) { throw new AssertionError("missing ctor on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.StructureViolationException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        hasCtor(c);
        hasCtor(c, String.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        StructureViolationException e = new StructureViolationException("m");
        check(e.getMessage().equals("m"), "message");
        check(e instanceof RuntimeException, "instanceof RuntimeException");
        check(new StructureViolationException().getMessage() == null, "no-arg null message");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
