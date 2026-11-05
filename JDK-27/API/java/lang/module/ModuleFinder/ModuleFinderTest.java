/*
 * @test
 * @summary java.lang.module.ModuleFinder conformance: white-box + black-box behavior.
 * @run main ModuleFinderTest
 */

import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

public class ModuleFinderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Set<String> names(ModuleFinder f) {
        return f.findAll().stream().map(r -> r.descriptor().name()).collect(Collectors.toSet());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.module.ModuleFinder");
        check(c.isInterface(), "is an interface");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public interface");
        for (String m : new String[]{"find", "findAll", "ofSystem", "of", "compose"}) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(m)) found = true;
            check(found, "declares " + m);
        }
        check(java.lang.reflect.Modifier.isStatic(
                      c.getDeclaredMethod("ofSystem").getModifiers()), "ofSystem is static");
        check(c.getDeclaredMethod("find", String.class).getReturnType() == java.util.Optional.class,
              "find returns Optional<ModuleReference>");
        check(c.getDeclaredMethod("findAll").getReturnType() == java.util.Set.class,
              "findAll returns Set<ModuleReference>");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // ofSystem: finds modules of the running runtime image
        ModuleFinder system = ModuleFinder.ofSystem();
        ModuleReference base = system.find("java.base").orElseThrow();
        check("java.base".equals(base.descriptor().name()), "find(java.base).descriptor().name()");
        check(base.location().isPresent() && "jrt".equals(base.location().get().getScheme()),
              "system module location uses the jrt scheme");
        check(system.find("java.logging").isPresent(), "find(java.logging) present");
        check(system.find("no.such.module").isEmpty(), "find(absent) empty Optional");

        Set<ModuleReference> all = system.findAll();
        check(!all.isEmpty(), "findAll() non-empty");
        check(all.stream().anyMatch(r -> "java.base".equals(r.descriptor().name())),
              "findAll() contains java.base");
        // findAll agrees with find for every module it reports
        boolean consistent = true;
        for (ModuleReference r : all) {
            if (system.find(r.descriptor().name()).isEmpty()) consistent = false;
        }
        check(consistent, "findAll and find are consistent");

        // of(): the empty finder
        ModuleFinder empty = ModuleFinder.of();
        check(empty.findAll().isEmpty(), "of() findAll empty");
        check(empty.find("java.base").isEmpty(), "of() finds nothing");

        // of(path) with a non-existent directory behaves as an empty finder
        Path dne = Files.createTempDirectory("mf-empty");
        ModuleFinder dirFinder = ModuleFinder.of(dne);
        check(dirFinder.findAll().isEmpty(), "of(nonexistent dir) findAll empty");
        check(dirFinder.find("java.base").isEmpty(), "of(nonexistent dir) find empty");

        // compose: union of finders, first finder wins on conflicts
        ModuleFinder c1 = ModuleFinder.compose(ModuleFinder.ofSystem(), ModuleFinder.of());
        check(c1.find("java.base").isPresent(), "compose(ofSystem, of) finds java.base");
        check(c1.findAll().size() == system.findAll().size(),
              "compose with empty adds nothing");
        ModuleFinder c2 = ModuleFinder.compose(ModuleFinder.of(), ModuleFinder.ofSystem());
        check(c2.find("java.base").isPresent(), "compose(of, ofSystem) finds java.base");
        check(names(c2).containsAll(names(system)), "compose union covers system modules");
        ModuleFinder c3 = ModuleFinder.compose();
        check(c3.findAll().isEmpty(), "compose() with no finders is empty");
        // compose of two finders finding the same module still reports it once
        ModuleFinder c4 = ModuleFinder.compose(ModuleFinder.ofSystem(), ModuleFinder.ofSystem());
        check(c4.findAll().size() == system.findAll().size(), "compose dedupes module names");
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

    // ofSystem(): names, requires edges, locations, uniqueness, immutability
    static void testOfSystemContract() {
        ModuleFinder system = ModuleFinder.ofSystem();
        ModuleReference base = system.find("java.base").orElseThrow();
        check(base.descriptor().requires().isEmpty(), "java.base requires nothing");
        check(base.descriptor().isAutomatic() == false, "java.base not automatic");
        ModuleReference logging = system.find("java.logging").orElseThrow();
        check("java.logging".equals(logging.descriptor().name()), "java.logging name");
        boolean requiresBase = false;
        for (ModuleDescriptor.Requires r : logging.descriptor().requires()) {
            if (r.name().equals("java.base")) requiresBase = true;
        }
        check(requiresBase, "java.logging requires java.base");
        java.util.Set<ModuleReference> all = system.findAll();
        check(all.size() > 50, "system image has many modules");
        java.util.Set<String> names = new java.util.HashSet<>();
        boolean allJrt = true;
        for (ModuleReference r : all) {
            names.add(r.descriptor().name());
            if (!r.location().isPresent() || !"jrt".equals(r.location().get().getScheme())) {
                allJrt = false;
            }
        }
        check(names.size() == all.size(), "system module names unique");
        check(names.contains("java.base") && names.contains("java.logging")
              && names.contains("java.sql"), "names include base/logging/sql");
        check(allJrt, "every system module lives at a jrt: URI");
        try {
            all.add(null);
            check(false, "ofSystem().findAll() immutable");
        } catch (UnsupportedOperationException e) {
            check(true, "ofSystem().findAll() immutable");
        }
        check(system.find("no.such.module").isEmpty(), "absent name empty Optional");
        try {
            system.find(null);
            check(false, "ofSystem().find(null) NPE");
        } catch (NullPointerException e) {
            check(true, "ofSystem().find(null) NPE");
        }
        // find agrees with descriptor() of the same reference repeatedly
        check(system.find("java.base").orElseThrow().descriptor().name()
              .equals(system.find("java.base").orElseThrow().descriptor().name()),
              "find stable across calls");
    }

    // of(): the empty finder contract, null and immutability shapes
    static void testOfContract() {
        ModuleFinder empty = ModuleFinder.of();
        check(empty.find("java.base").isEmpty(), "find empty");
        check(empty.findAll().isEmpty(), "findAll empty");
        check(empty.findAll().equals(empty.findAll()), "findAll stable");
        try {
            empty.findAll().add(null);
            check(false, "of().findAll() immutable");
        } catch (UnsupportedOperationException e) {
            check(true, "of().findAll() immutable");
        }
        try {
            empty.find(null);
            check(false, "of().find(null) NPE");
        } catch (NullPointerException e) {
            check(true, "of().find(null) NPE");
        }
        check(ModuleFinder.of() != ModuleFinder.of(), "of() is not cached");
        check(empty.find("").isEmpty(), "empty name yields empty Optional");
        check(ModuleFinder.of().findAll().isEmpty(), "fresh of() still empty");
    }

    // of(Path...): nonexistent and multiple directory arguments
    static void testOfPathContract() {
        Path nowhere = Path.of("/tmp/lcK-definitely-not-here-42");
        ModuleFinder dne = ModuleFinder.of(nowhere);
        check(dne.findAll().isEmpty(), "nonexistent dir: empty finder");
        check(dne.find("java.base").isEmpty(), "nonexistent dir: find empty");
        Path tmpDir = null;
        try {
            tmpDir = Files.createTempDirectory("lcK-ofpath");
            ModuleFinder one = ModuleFinder.of(tmpDir);
            ModuleFinder two = ModuleFinder.of(tmpDir, nowhere);
            check(one.findAll().isEmpty() && two.findAll().isEmpty(),
                  "empty dir and mixed empty/nonexistent dirs resolve nothing");
            check(two.find("java.base").isEmpty(), "mixed dirs find nothing");
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        } finally {
            if (tmpDir != null) {
                try {
                    Files.delete(tmpDir);
                } catch (java.io.IOException ignore) { }
            }
        }
    }

    // compose(): precedence, dedup, and identity of the winning reference
    static void testComposeContract() {
        ModuleDescriptor a = ModuleDescriptor.newModule("jck.cmp.m").exports("a").build();
        ModuleDescriptor b = ModuleDescriptor.newModule("jck.cmp.m").exports("b").build();
        ModuleFinder fa = finderOf(a);
        ModuleFinder fb = finderOf(b);
        check(ModuleFinder.compose(fa, fb).find("jck.cmp.m").orElseThrow().descriptor() == a,
              "first finder's reference wins");
        check(ModuleFinder.compose(fb, fa).find("jck.cmp.m").orElseThrow().descriptor() == b,
              "order flip changes the winner");
        check(ModuleFinder.compose(fa).find("jck.cmp.m").orElseThrow().descriptor() == a,
              "compose of one finder delegates");
        ModuleFinder c = ModuleFinder.compose(fa, ModuleFinder.of(), fb);
        check(c.find("jck.cmp.m").orElseThrow().descriptor() == a,
              "empty finder in the middle does not shadow");
        check(c.findAll().size() == 1, "duplicate name appears once");
        ModuleDescriptor x = ModuleDescriptor.newModule("jck.cmp.x").build();
        ModuleDescriptor y = ModuleDescriptor.newModule("jck.cmp.y").build();
        ModuleFinder union = ModuleFinder.compose(finderOf(x, a), finderOf(y));
        check(union.find("jck.cmp.x").isPresent() && union.find("jck.cmp.y").isPresent()
              && union.find("jck.cmp.m").isPresent(), "union sees all modules");
        check(union.findAll().size() == 3, "union size sums distinct names");
        try {
            union.findAll().add(null);
            check(false, "compose result immutable");
        } catch (UnsupportedOperationException e) {
            check(true, "compose result immutable");
        }
    }

    // a hand-written finder honors the interface contract exactly
    static void testCustomFinderContract() {
        ModuleDescriptor d1 = ModuleDescriptor.newModule("jck.cust.a").exports("jck.cust.a").build();
        ModuleDescriptor d2 = ModuleDescriptor.newModule("jck.cust.b").requires("jck.cust.a").build();
        ModuleFinder f = finderOf(d1, d2);
        ModuleReference r1 = f.find("jck.cust.a").orElseThrow();
        check(r1.descriptor() == d1, "find returns the exact stored reference");
        check(r1.location().isEmpty(), "null location surfaces as empty Optional");
        check(f.find("jck.cust.a").orElseThrow() == r1, "find stable");
        check(f.find("jck.cust.missing").isEmpty(), "find absent empty");
        java.util.Set<ModuleReference> all = f.findAll();
        check(all.size() == 2, "findAll size");
        boolean hasA = false, hasB = false;
        for (ModuleReference r : all) {
            if (r.descriptor() == d1) hasA = true;
            if (r.descriptor() == d2) hasB = true;
        }
        check(hasA && hasB, "findAll contains both references");
        // the finder plugs directly into resolution
        Configuration cfg = ModuleLayer.boot().configuration().resolve(
                f, ModuleFinder.of(), Set.of("jck.cust.b"));
        check(cfg.findModule("jck.cust.a").isPresent() && cfg.findModule("jck.cust.b").isPresent(),
              "custom finder drives Configuration.resolve");
        check(cfg.findModule("jck.cust.b").orElseThrow().reference() == f.find("jck.cust.b").orElseThrow(),
              "resolved reference is the finder's reference");
        check(f.find(null).isEmpty(),
              "custom find(null) is implementation-defined; ours yields empty Optional");
    }

    // locations, descriptor content, and shadowing across composed finders
    static void testFinderExtras() {
        ModuleFinder system = ModuleFinder.ofSystem();
        ModuleReference base = system.find("java.base").orElseThrow();
        check("jrt:/java.base".equals(base.location().orElseThrow().toString()),
              "java.base location exact jrt URI");
        check("jrt:/java.logging".equals(
                  system.find("java.logging").orElseThrow().location().orElseThrow().toString()),
              "java.logging location exact jrt URI");
        check(base.descriptor().packages().contains("java.lang"), "base packages contains java.lang");
        check(system.find("java.logging").orElseThrow().descriptor()
                      .packages().contains("java.util.logging"),
              "java.logging packages contains java.util.logging");
        check(base.descriptor().isOpen() == false, "java.base descriptor not open");
        // composing over ofSystem: custom modules are visible, system modules not shadowed
        ModuleDescriptor custom = ModuleDescriptor.newModule("jck.extra.m").exports("jck.extra").build();
        ModuleFinder composed = ModuleFinder.compose(system, finderOf(custom));
        check(composed.find("jck.extra.m").orElseThrow().descriptor() == custom,
              "custom module reachable through composition");
        check(composed.find("java.base").orElseThrow().location()
                      .orElseThrow().toString().equals("jrt:/java.base"),
              "system module unaffected by extra finder");
        // a custom finder cannot shadow a system module name: system comes first
        ModuleDescriptor fakeBase = ModuleDescriptor.newModule("java.base").build();
        ModuleFinder shadow = ModuleFinder.compose(system, finderOf(fakeBase));
        check(shadow.find("java.base").orElseThrow() == system.find("java.base").orElseThrow(),
              "earlier finder keeps the canonical java.base reference");
        check(shadow.findAll().size() == system.findAll().size(),
              "shadowing attempt adds no extra entry");
        // names() helper: the view agrees with findAll for the custom finder
        ModuleFinder f = finderOf(ModuleDescriptor.newModule("jck.names.a").build(),
                ModuleDescriptor.newModule("jck.names.b").build());
        check(names(f).equals(Set.of("jck.names.a", "jck.names.b")), "names helper round trip");
        check(f.findAll().equals(f.findAll()), "custom findAll stable across calls");
        // every system ModuleReference can serve up its module-info.class
        try (ModuleReader reader = base.open()) {
            java.util.Optional<java.nio.ByteBuffer> bb = reader.read("module-info.class");
            check(bb.isPresent() && bb.get().remaining() > 1000,
                  "module-info.class readable through finder-supplied reference");
            reader.release(bb.get());
        } catch (java.io.IOException e) {
            throw new AssertionError("unexpected IOException", e);
        }
    }

    // white-box deepening: interface shape and static factories
    static void testWhiteBoxDeepen() throws Exception {
        Class<?> c = ModuleFinder.class;
        check(java.lang.reflect.Modifier.isAbstract(
                  c.getDeclaredMethod("find", String.class).getModifiers()), "find is abstract");
        check(java.lang.reflect.Modifier.isAbstract(
                  c.getDeclaredMethod("findAll").getModifiers()), "findAll is abstract");
        for (String m : new String[] {"of", "ofSystem", "compose"}) {
            check(isStatic(c, m), m + " has a static overload");
        }
        check(ModuleFinder.ofSystem() != null && !ModuleFinder.ofSystem().findAll().isEmpty(),
              "ofSystem usable");
        check(ModuleFinder.ofSystem().findAll().size()
              == ModuleFinder.ofSystem().findAll().size(), "ofSystem size stable");
        boolean seenOfVarargs = false;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("of") && m.getParameterCount() == 1
                    && m.getParameterTypes()[0] == java.nio.file.Path[].class) {
                seenOfVarargs = true;
            }
        }
        check(seenOfVarargs, "of(Path...) varargs factory");
        boolean seenComposeVarargs = false;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("compose") && m.getParameterCount() == 1
                    && m.getParameterTypes()[0] == ModuleFinder[].class) {
                seenComposeVarargs = true;
            }
        }
        check(seenComposeVarargs, "compose(ModuleFinder...) varargs");
        check(java.io.Closeable.class.isAssignableFrom(ModuleReference.class) == false,
              "ModuleReference itself is not Closeable (its reader is)");
    }

    static boolean isStatic(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name) && java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testOfSystemContract();
        testOfContract();
        testOfPathContract();
        testComposeContract();
        testCustomFinderContract();
        testFinderExtras();
        testWhiteBoxDeepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
