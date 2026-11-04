/*
 * @test
 * @summary java.lang.reflect.InaccessibleObjectException conformance: white-box structure + black-box behavior.
 * @run main InaccessibleObjectExceptionTest
 */
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;

public class InaccessibleObjectExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() {
        check(java.lang.RuntimeException.class.isAssignableFrom(InaccessibleObjectException.class),
              "extends RuntimeException");
        InaccessibleObjectException e = new InaccessibleObjectException("msg");
        check("msg".equals(e.getMessage()), "message");
        check(new InaccessibleObjectException().getMessage() == null, "no-arg");
        check(InaccessibleObjectException.class.getConstructors().length == 2,
              "two public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. real trigger: setAccessible on a private member of an unopened java.base class
        Field value = String.class.getDeclaredField("value");
        try {
            value.setAccessible(true);
            check(false, "setAccessible on java.base private member must be refused");
        } catch (InaccessibleObjectException e) {
            check(true, "InaccessibleObjectException on unopened module");
            check(e.getMessage() != null && e.getMessage().contains("java.base"),
                  "message names the refusing module");
        }
        check(!value.trySetAccessible(), "trySetAccessible false (no throw)");

        // 2. same refusal on a JDK private method
        Method internal = Class.class.getDeclaredMethod("getConstantPool");
        try {
            internal.setAccessible(true);
            check(false, "method setAccessible must be refused");
        } catch (InaccessibleObjectException e) {
            check(true, "InaccessibleObjectException on private JDK method");
        }

        // 3. members of open-to-caller classes (own nest) never raise it
        Field own = Fixture.class.getDeclaredField("hidden");
        own.setAccessible(true);
        check(own.isAccessible(), "own class setAccessible fine");
        check(own.trySetAccessible(), "own class trySetAccessible true");

        // 4. public members of java.base classes remain accessible without any flag
        Field hash = String.class.getDeclaredField("hash");
        hash.setAccessible(false); // no exception expected for public-typed flag off
        check(!hash.isAccessible(), "flag off readable");

        // 5. constructor surface and throwable mechanics
        InaccessibleObjectException bare = new InaccessibleObjectException();
        check(bare.getMessage() == null && bare.getCause() == null, "bare nulls");
        InaccessibleObjectException with = new InaccessibleObjectException("denied");
        check("denied".equals(with.getMessage()), "message kept");
        check(with.toString().startsWith("java.lang.reflect.InaccessibleObjectException"),
              "toString prefix");
        RuntimeException root = new RuntimeException("r");
        with.initCause(root);
        check(with.getCause() == root, "initCause wiring");
        boolean caughtAsRte = false;
        try {
            throw new InaccessibleObjectException("x");
        } catch (RuntimeException e) {
            caughtAsRte = e instanceof InaccessibleObjectException;
        }
        check(caughtAsRte, "catch as RuntimeException");
    }

    static class Fixture {
        private int hidden = 1;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
