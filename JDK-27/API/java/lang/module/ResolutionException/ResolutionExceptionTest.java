/*
 * @test
 * @summary java.lang.module.ResolutionException conformance: white-box + black-box behavior.
 * @run main ResolutionExceptionTest
 */

import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.lang.module.ResolutionException;

public class ResolutionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.module.ResolutionException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        int ctors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) ctors++;
        }
        check(ctors == 4, "four public constructors");
        c.getDeclaredConstructor();
        c.getDeclaredConstructor(String.class);
        c.getDeclaredConstructor(Throwable.class);
        c.getDeclaredConstructor(String.class, Throwable.class);
        check(c.getDeclaredField("serialVersionUID").getType() == long.class,
              "serialVersionUID declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(new ResolutionException() instanceof RuntimeException, "is a RuntimeException");

        ResolutionException e0 = new ResolutionException();
        check(e0.getMessage() == null, "no-arg message null");
        check(e0.getCause() == null, "no-arg cause null");

        ResolutionException e1 = new ResolutionException("cycle detected");
        check("cycle detected".equals(e1.getMessage()), "String ctor message");
        check(e1.getCause() == null, "String ctor no cause");

        RuntimeException cause = new RuntimeException("cause");
        ResolutionException e2 = new ResolutionException(cause);
        check(e2.getCause() == cause, "Throwable ctor cause identity");
        check(e2.getMessage().equals(cause.toString()), "Throwable ctor message = cause.toString()");

        ResolutionException e3 = new ResolutionException("msg", cause);
        check("msg".equals(e3.getMessage()), "String+Throwable message");
        check(e3.getCause() == cause, "String+Throwable cause identity");

        check(e1.toString().startsWith("java.lang.module.ResolutionException: cycle detected"),
              "toString format");
        e1.addSuppressed(new IllegalStateException("s"));
        check(e1.getSuppressed().length == 1, "suppressed supported");

        // unchecked: can be thrown from methods with no throws clause
        try {
            throw new ResolutionException("read cyclic requires");
        } catch (RuntimeException re) {
            check(re instanceof ResolutionException, "caught as RuntimeException");
        }
    }

    // ---- Part 3: contract deepening (2026-11) ----

    static ModuleFinder finderOf(ModuleDescriptor... descriptors) {
        java.util.Map<String, ModuleReference> refs = new java.util.HashMap<>();
        for (ModuleDescriptor d : descriptors) {
            refs.put(d.name(), new ModuleReference(d, null) {
                @Override public ModuleReader open() {
                    throw new UnsupportedOperationException();
                }
            });
        }
        return new ModuleFinder() {
            @Override public java.util.Optional<ModuleReference> find(String name) {
                return java.util.Optional.ofNullable(refs.get(name));
            }
            @Override public java.util.Set<ModuleReference> findAll() {
                return new java.util.HashSet<>(refs.values());
            }
        };
    }

    static ResolutionException roundTrip(ResolutionException e) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(e);
        }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            return (ResolutionException) ois.readObject();
        }
    }

    static void testThrowableMechanics() throws Exception {
        RuntimeException cause = new RuntimeException("c");
        ResolutionException e = new ResolutionException("read cycle", cause);
        check("java.lang.module.ResolutionException: read cycle".equals(e.toString()),
                "exact toString");
        ResolutionException back = roundTrip(e);
        check(back.getMessage().equals("read cycle"), "serialization keeps message");
        check(back.getCause().getMessage().equals("c"), "serialization keeps cause");
        check(back instanceof ResolutionException, "serialization keeps class");
        check(e.getSuppressed().length == 0, "default suppressed empty");
        IllegalStateException s1 = new IllegalStateException("s1");
        e.addSuppressed(s1);
        Throwable[] copy = e.getSuppressed();
        copy[0] = null;
        check(e.getSuppressed()[0] == s1, "getSuppressed defensive copy");
        StackTraceElement[] st = {new StackTraceElement("X", "m", "X.java", 9)};
        e.setStackTrace(st);
        check(e.getStackTrace().length == 1
                      && e.getStackTrace()[0].getMethodName().equals("m"),
                "setStackTrace round trip");
        ResolutionException fresh = new ResolutionException("f");
        check(fresh.initCause(cause) == fresh, "initCause returns this");
        try {
            e.initCause(new RuntimeException("x"));
            check(false, "re-initCause ISE");
        } catch (IllegalStateException ex) {
            check(true, "re-initCause ISE");
        }
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintStream(bos));
        check(bos.toString("UTF-8").contains("java.lang.module.ResolutionException"),
                "printStackTrace prefix");
        check(new ResolutionException().toString()
                      .equals("java.lang.module.ResolutionException"), "no-arg toString");
        check(ResolutionException.class.getPackageName().equals("java.lang.module"),
                "package");
        check(java.io.Serializable.class.isAssignableFrom(ResolutionException.class),
                "Serializable");
        check(e.getLocalizedMessage().equals(e.getMessage()), "getLocalizedMessage default");
    }

    // real trigger: a require cycle between two automatic-less named modules
    static void testRealTrigger() {
        ModuleDescriptor m1 = ModuleDescriptor.newModule("jck.re.a").requires("jck.re.b").build();
        ModuleDescriptor m2 = ModuleDescriptor.newModule("jck.re.b").requires("jck.re.a").build();
        try {
            Configuration.resolveAndBind(finderOf(m1, m2),
                    java.util.List.of(ModuleLayer.boot().configuration()),
                    ModuleFinder.of(), java.util.Set.of("jck.re.a"));
            check(false, "cycle must throw");
        } catch (ResolutionException e) {
            check(e.getMessage().startsWith("Cycle detected:"), "cycle message prefix");
            check(e.getMessage().contains("jck.re.a") && e.getMessage().contains("jck.re.b"),
                    "cycle message names both modules");
            check(e instanceof RuntimeException, "cycle is a RuntimeException");
        }
        // the same cycle from plain resolve, not just resolveAndBind
        try {
            Configuration.resolve(finderOf(m1, m2),
                    java.util.List.of(ModuleLayer.boot().configuration()),
                    ModuleFinder.of(), java.util.Set.of("jck.re.b"));
            check(false, "cycle from resolve must throw");
        } catch (ResolutionException e) {
            check(e.getMessage().startsWith("Cycle detected:"), "resolve cycle prefix");
        }
        // three-module cycle still reports the first edge back to the start
        ModuleDescriptor t1 = ModuleDescriptor.newModule("jck.re.t1").requires("jck.re.t2").build();
        ModuleDescriptor t2 = ModuleDescriptor.newModule("jck.re.t2").requires("jck.re.t3").build();
        ModuleDescriptor t3 = ModuleDescriptor.newModule("jck.re.t3").requires("jck.re.t1").build();
        try {
            Configuration.resolve(finderOf(t1, t2, t3),
                    java.util.List.of(ModuleLayer.boot().configuration()),
                    ModuleFinder.of(), java.util.Set.of("jck.re.t1"));
            check(false, "three-module cycle must throw");
        } catch (ResolutionException e) {
            check(e.getMessage().contains("->"), "cycle message uses arrows");
            check(e.getMessage().startsWith("Cycle detected:"), "three-module cycle prefix");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testThrowableMechanics();
        testRealTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
