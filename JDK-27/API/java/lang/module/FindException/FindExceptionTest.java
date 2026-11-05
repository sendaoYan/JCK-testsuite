/*
 * @test
 * @summary java.lang.module.FindException conformance: white-box + black-box behavior.
 * @run main FindExceptionTest
 */

import java.lang.module.Configuration;
import java.lang.module.FindException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;

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

    // ---- Part 3: contract deepening (2026-11) ----

    static FindException roundTrip(FindException e) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(e);
        }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            return (FindException) ois.readObject();
        }
    }

    // serialization, suppressed copies, stack trace manipulation
    static void testThrowableMechanics() throws Exception {
        RuntimeException cause = new RuntimeException("c");
        FindException e = new FindException("ser", cause);
        FindException back = roundTrip(e);
        check(back.getMessage().equals("ser"), "serialization keeps the message");
        check(back.getCause().getMessage().equals("c"), "serialization keeps the cause");
        check(back instanceof FindException, "serialization keeps the class");
        check("java.lang.module.FindException: ser".equals(e.toString()),
                "exact toString with message");
        check(e.getSuppressed().length == 0, "fresh suppressed empty");
        IllegalStateException s1 = new IllegalStateException("s1");
        IllegalStateException s2 = new IllegalStateException("s2");
        e.addSuppressed(s1);
        e.addSuppressed(s2);
        check(e.getSuppressed().length == 2 && e.getSuppressed()[0] == s1
                      && e.getSuppressed()[1] == s2, "suppressed order preserved");
        Throwable[] copy = e.getSuppressed();
        copy[0] = null;
        check(e.getSuppressed()[0] == s1, "getSuppressed returns a defensive copy");
        StackTraceElement[] st = {new StackTraceElement("A", "m", "A.java", 1),
                new StackTraceElement("B", "n", "B.java", 2)};
        e.setStackTrace(st);
        check(e.getStackTrace().length == 2 && e.getStackTrace()[0].getClassName().equals("A"),
                "setStackTrace round trip");
        e.getStackTrace()[0] = null;
        check(e.getStackTrace()[0] != null, "getStackTrace returns a defensive copy");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintStream(bos));
        String printed = bos.toString("UTF-8");
        check(printed.contains("java.lang.module.FindException: ser"), "printStackTrace message");
        check(printed.contains("Suppressed: java.lang.IllegalStateException: s1"),
                "printStackTrace suppressed section");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(new FindException().toString().equals("java.lang.module.FindException"),
                "no-arg toString has no colon");
        check(FindException.class.getPackageName().equals("java.lang.module"), "package");
        check(java.io.Serializable.class.isAssignableFrom(FindException.class), "Serializable");
    }

    // real triggers with exact messages
    static void testRealTriggers() {
        // missing root module
        try {
            Configuration.resolve(ModuleFinder.of(),
                    java.util.List.of(ModuleLayer.boot().configuration()),
                    ModuleFinder.of(), java.util.Set.of("no.such.root"));
            check(false, "missing root must throw");
        } catch (FindException e) {
            check("Module no.such.root not found".equals(e.getMessage()),
                    "missing root exact message");
            check(e.getCause() == null, "missing root carries no cause");
        }
        // missing transitive dependency names both sides
        ModuleDescriptor user = ModuleDescriptor.newModule("jck.fe.user")
                .requires("jck.fe.absent").build();
        ModuleFinder finder = new ModuleFinder() {
            @Override public java.util.Optional<ModuleReference> find(String name) {
                return "jck.fe.user".equals(name)
                        ? java.util.Optional.of(new ModuleReference(user, null) {
                            @Override public ModuleReader open() {
                                throw new UnsupportedOperationException();
                            }})
                        : java.util.Optional.empty();
            }
            @Override public java.util.Set<ModuleReference> findAll() {
                return java.util.Set.of();
            }
        };
        try {
            Configuration.resolve(finder, java.util.List.of(ModuleLayer.boot().configuration()),
                    ModuleFinder.of(), java.util.Set.of("jck.fe.user"));
            check(false, "missing dependency must throw");
        } catch (FindException e) {
            check("Module jck.fe.absent not found, required by jck.fe.user"
                    .equals(e.getMessage()), "missing dependency exact message");
        }
        // a finder may itself throw FindException to report an unreadable module
        ModuleFinder throwing = new ModuleFinder() {
            @Override public java.util.Optional<ModuleReference> find(String name) {
                throw new FindException("cannot hash " + name);
            }
            @Override public java.util.Set<ModuleReference> findAll() {
                return java.util.Set.of();
            }
        };
        try {
            throwing.find("anything");
            check(false, "finder-thrown FindException propagates");
        } catch (FindException e) {
            check("cannot hash anything".equals(e.getMessage()), "finder-thrown message");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testThrowableMechanics();
        testRealTriggers();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
