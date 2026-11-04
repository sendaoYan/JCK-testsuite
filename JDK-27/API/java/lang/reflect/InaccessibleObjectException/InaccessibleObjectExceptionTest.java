/*
 * @test
 * @summary java.lang.reflect.InaccessibleObjectException conformance: white-box structure + black-box behavior.
 *          Deepened: exact constructor surface, Throwable mechanics (suppressed/initCause),
 *          public members of exported packages never refused, catch hierarchy.
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

    // ---- Part 3: contract deepening (2026-11-05); surface probed on JDK 27.0.1 ----
    static void testConstructorAndThrowable() {
        // exactly two public constructors; there is NO (String, Throwable) overload (probed)
        java.lang.reflect.Constructor<?>[] cs = InaccessibleObjectException.class.getConstructors();
        boolean noArg = false, oneArg = false;
        for (java.lang.reflect.Constructor<?> c : cs) {
            if (c.getParameterCount() == 0) noArg = true;
            if (c.getParameterCount() == 1 && c.getParameterTypes()[0] == String.class) oneArg = true;
        }
        check(noArg && oneArg, "public ctors are exactly () and (String)");
        check(new InaccessibleObjectException() instanceof java.io.Serializable, "Serializable");
        InaccessibleObjectException e = new InaccessibleObjectException("denied");
        check(e.toString().equals("java.lang.reflect.InaccessibleObjectException: denied"),
              "toString exact");
        check(e.getSuppressed().length == 0, "no suppressed initially");
        RuntimeException sup = new RuntimeException("s");
        e.addSuppressed(sup);
        check(e.getSuppressed().length == 1 && e.getSuppressed()[0] == sup, "suppressed wired");
        RuntimeException cause = new RuntimeException("c");
        e.initCause(cause);
        check(e.getCause() == cause, "initCause once");
        try {
            e.initCause(new RuntimeException("c2"));
            check(false, "second initCause must fail");
        } catch (IllegalStateException ise) {
            check(true, "initCause twice -> IllegalStateException");
        }
        check(new InaccessibleObjectException("m").getSuppressed().length == 0,
              "fresh instance carries no suppressed");
    }

    static void testPublicMembersNeverRefused() throws Exception {
        // contrast: public members of unconditionally exported packages never raise the
        // exception even with setAccessible(true) -- no deep reflection needed there
        java.lang.reflect.Constructor<?> c = String.class.getDeclaredConstructor(String.class);
        c.setAccessible(true);
        check(c.isAccessible(), "public ctor setAccessible fine (java.lang exported)");
        check(c.canAccess(null), "canAccess true for public ctor with null receiver");
        c.setAccessible(false);
        check(!c.isAccessible(), "flag back off");
        Method valueOf = Integer.class.getMethod("valueOf", int.class);
        valueOf.setAccessible(true);
        check(valueOf.isAccessible(), "public static method setAccessible fine");
        valueOf.setAccessible(false);
        Field out = System.class.getField("out");
        out.setAccessible(true);
        check(out.isAccessible(), "public field setAccessible fine");
        out.setAccessible(false);
        // same members stay usable without the flag
        check(valueOf.invoke(null, 7).equals(7), "public method invoke still works");
    }

    static void testCatchHierarchy() {
        InaccessibleObjectException boom = new InaccessibleObjectException("x");
        boolean asRte = false, asExc = false;
        try { throw boom; } catch (RuntimeException e) { asRte = (e == boom); }
        try { throw boom; } catch (Exception e) { asExc = (e == boom); }
        check(asRte && asExc, "catchable as RuntimeException and Exception");
        check(!java.lang.Error.class.isAssignableFrom(InaccessibleObjectException.class),
              "not an Error");
        check(java.lang.reflect.InaccessibleObjectException.class.getSuperclass()
              == java.lang.RuntimeException.class, "direct RuntimeException subclass");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructorAndThrowable();
        testPublicMembersNeverRefused();
        testCatchHierarchy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
