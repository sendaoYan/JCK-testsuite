/*
 * @test
 * @summary java.lang.module.FindException conformance: white-box + black-box behavior.
 * @run main FindExceptionTest
 */

import java.lang.module.FindException;
import java.lang.module.ModuleFinder;

public class FindExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.module.FindException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        int ctors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) ctors++;
        }
        check(ctors == 4, "four public constructors");
        c.getDeclaredConstructor();                 // ()
        c.getDeclaredConstructor(String.class);     // (String)
        c.getDeclaredConstructor(Throwable.class);  // (Throwable)
        c.getDeclaredConstructor(String.class, Throwable.class); // (String, Throwable)
        check(c.getDeclaredField("serialVersionUID").getType() == long.class,
              "serialVersionUID declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(new FindException() instanceof RuntimeException, "is a RuntimeException");
        check(new FindException() instanceof Exception, "is an Exception");
        check(new FindException() instanceof Throwable, "is a Throwable");

        // no-arg: null message, null cause
        FindException e0 = new FindException();
        check(e0.getMessage() == null, "no-arg ctor message null");
        check(e0.getCause() == null, "no-arg ctor cause null");

        // message-only
        FindException e1 = new FindException("boom");
        check("boom".equals(e1.getMessage()), "String ctor message");
        check(e1.getCause() == null, "String ctor no cause");

        // cause-only: message becomes cause.toString()
        RuntimeException cause = new RuntimeException("cause");
        FindException e2 = new FindException(cause);
        check(e2.getCause() == cause, "Throwable ctor cause (identity)");
        check(e2.getMessage().equals(cause.toString()), "Throwable ctor message is cause.toString()");

        // message + cause
        FindException e3 = new FindException("msg", cause);
        check("msg".equals(e3.getMessage()), "String+Throwable ctor message");
        check(e3.getCause() == cause, "String+Throwable ctor cause (identity)");

        // Throwable mechanics
        check(e1.toString().startsWith("java.lang.module.FindException: boom"),
              "toString format");
        Throwable suppressed = new IllegalStateException("sup");
        e1.addSuppressed(suppressed);
        check(e1.getSuppressed().length == 1 && e1.getSuppressed()[0] == suppressed,
              "addSuppressed/getSuppressed");
        check(e1.getStackTrace() != null && e1.getStackTrace().length > 0,
              "fillInStackTrace captured frames");
        e1.initCause(cause);
        check(e1.getCause() == cause, "initCause works once");
        try { e1.initCause(null); check(false, "re-initCause should fail"); }
        catch (IllegalStateException expected) { check(true, "initCause twice ISE"); }

        // thrown by the module resolution machinery for an unknown root
        try {
            ModuleFinder.of().find("no.such.module");
            check(ModuleFinder.of().find("no.such.module").isEmpty(),
                  "find(absent) yields empty Optional, no throw");
        } catch (FindException e) {
            check(false, "find(absent) must not throw");
        }
        try {
            java.lang.module.Configuration.resolve(
                ModuleFinder.of(), java.util.List.of(ModuleLayer.boot().configuration()),
                ModuleFinder.of(), java.util.Set.of("no.such.root"));
            check(false, "unknown root must throw");
        } catch (FindException e) {
            check(e.getMessage().contains("no.such.root"), "resolve raises FindException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
