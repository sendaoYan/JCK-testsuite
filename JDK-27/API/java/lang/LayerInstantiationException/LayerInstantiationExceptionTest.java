/*
 * @test
 * @summary java.lang.LayerInstantiationException conformance: white-box structure + black-box behavior.
 * @run main LayerInstantiationExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.module.ModuleFinder;

public class LayerInstantiationExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": got " + t.getClass().getName());
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.LayerInstantiationException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == RuntimeException.class, "superclass is RuntimeException");
        check(c.getDeclaredConstructors().length == 4, "4 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.LayerInstantiationException");

        LayerInstantiationException e0 = new LayerInstantiationException();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { LayerInstantiationException e = new LayerInstantiationException("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }
        { Exception cause = new Exception("cause"); LayerInstantiationException e = new LayerInstantiationException(cause); check(e.getCause() == cause, "(Throwable) ctor cause"); }
        { Exception cause = new Exception("cause"); LayerInstantiationException e = new LayerInstantiationException("boom", cause); check("boom".equals(e.getMessage()) && e.getCause() == cause, "(String,Throwable) ctor"); }

        // common Throwable contract
        LayerInstantiationException t = new LayerInstantiationException();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.LayerInstantiationException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

    }

    // ---- Part 3: contract deepening (2026-11) ----

    static LayerInstantiationException roundTrip(LayerInstantiationException e) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(e);
        }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            return (LayerInstantiationException) ois.readObject();
        }
    }

    static ModuleFinder finderOf(java.lang.module.ModuleDescriptor... descriptors) {
        java.util.Map<String, java.lang.module.ModuleReference> refs = new java.util.HashMap<>();
        for (java.lang.module.ModuleDescriptor d : descriptors) {
            refs.put(d.name(), new java.lang.module.ModuleReference(d, null) {
                @Override public java.lang.module.ModuleReader open() {
                    throw new UnsupportedOperationException();
                }
            });
        }
        return new ModuleFinder() {
            @Override public java.util.Optional<java.lang.module.ModuleReference> find(String n) {
                return java.util.Optional.ofNullable(refs.get(n));
            }
            @Override public java.util.Set<java.lang.module.ModuleReference> findAll() {
                return new java.util.HashSet<>(refs.values());
            }
        };
    }

    // serialization, suppressed/stack mechanics, exact strings
    static void testThrowableMechanics() throws Exception {
        Exception cause = new Exception("c");
        LayerInstantiationException e = new LayerInstantiationException("boom", cause);
        check("java.lang.LayerInstantiationException: boom".equals(e.toString()),
                "exact toString with message");
        LayerInstantiationException back = roundTrip(e);
        check(back.getMessage().equals("boom"), "serialization keeps message");
        check(back.getCause().getMessage().equals("c"), "serialization keeps cause");
        check(back instanceof LayerInstantiationException, "serialization keeps class");
        check(e.getSuppressed().length == 0, "default suppressed empty");
        IllegalStateException s1 = new IllegalStateException("s1");
        e.addSuppressed(s1);
        Throwable[] copy = e.getSuppressed();
        copy[0] = null;
        check(e.getSuppressed()[0] == s1, "getSuppressed defensive copy");
        StackTraceElement[] st = {new StackTraceElement("L", "m", "L.java", 7)};
        e.setStackTrace(st);
        check(e.getStackTrace()[0].getClassName().equals("L"), "setStackTrace round trip");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(bos));
        check(bos.toString("UTF-8").contains("Suppressed: java.lang.IllegalStateException: s1"),
                "printStackTrace suppressed section");
        check(new LayerInstantiationException().toString()
                      .equals("java.lang.LayerInstantiationException"), "no-arg toString");
        check(new LayerInstantiationException(cause).getMessage().equals(cause.toString()),
                "cause-ctor message is cause.toString()");
        check(new LayerInstantiationException(cause).getCause() == cause,
                "cause-ctor keeps identity");
        check(LayerInstantiationException.class.getPackageName().equals("java.lang"),
                "package java.lang");
        check(java.io.Serializable.class.isAssignableFrom(LayerInstantiationException.class),
                "Serializable");
        // constructor surface: (), (String), (Throwable), (String, Throwable)
        Class<?>[][] want = {{}, {String.class}, {Throwable.class}, {String.class, Throwable.class}};
        for (Class<?>[] sig : want) {
            try {
                LayerInstantiationException.class.getDeclaredConstructor(sig);
                check(true, "ctor " + java.util.Arrays.toString(sig) + " declared");
            } catch (NoSuchMethodException ex) {
                check(false, "missing ctor " + java.util.Arrays.toString(sig));
            }
        }
        check(e.getStackTrace().length >= 1, "stack trace present");
        check(new LayerInstantiationException(cause).toString()
                      .startsWith("java.lang.LayerInstantiationException: java.lang.Exception"),
                "cause-ctor toString embeds cause.toString()");
        LayerInstantiationException causeOnly = roundTrip(new LayerInstantiationException(cause));
        check(causeOnly.getMessage() != null && causeOnly.getMessage().contains("Exception"),
                "cause-only serialization keeps the derived message");
    }

    // real trigger: two modules in one layer share a package
    static void testRealTrigger() {
        java.lang.module.Configuration bootCfg = ModuleLayer.boot().configuration();
        java.lang.module.ModuleDescriptor m1 = java.lang.module.ModuleDescriptor
                .newModule("jck.lie.a").packages(java.util.Set.of("jck.shared"))
                .exports("jck.shared").build();
        java.lang.module.ModuleDescriptor m2 = java.lang.module.ModuleDescriptor
                .newModule("jck.lie.b").packages(java.util.Set.of("jck.shared"))
                .exports("jck.shared").build();
        java.lang.module.Configuration cfg = java.lang.module.Configuration.resolve(
                finderOf(m1, m2), java.util.List.of(bootCfg), ModuleFinder.of(),
                java.util.Set.of("jck.lie.a", "jck.lie.b"));
        try {
            ModuleLayer.boot().defineModulesWithOneLoader(cfg,
                    LayerInstantiationExceptionTest.class.getClassLoader());
            check(false, "split package must throw");
        } catch (LayerInstantiationException e) {
            check("Package jck.shared in more than one module".equals(e.getMessage()),
                    "split package exact message");
        }
        // the same configuration is fine with many loaders (no shared loader conflict)
        try {
            ModuleLayer.Controller ctl = ModuleLayer.defineModulesWithManyLoaders(cfg,
                    java.util.List.of(ModuleLayer.boot()),
                    LayerInstantiationExceptionTest.class.getClassLoader());
            check(ctl.layer().findModule("jck.lie.a").isPresent()
                          && ctl.layer().findModule("jck.lie.b").isPresent(),
                    "many-loaders sidesteps the package clash");
        } catch (LayerInstantiationException e) {
            check(false, "many-loaders should not fail here: " + e);
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
