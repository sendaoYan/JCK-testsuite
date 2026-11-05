/*
 * @test
 * @summary java.lang.ModuleLayer conformance: white-box structure + black-box behavior.
 * @run main ModuleLayerTest
 */

import java.lang.ModuleLayer;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ModuleLayerTest {
    static int passed;

    static ModuleFinder finderOf(ModuleDescriptor... descriptors) {
        Map<String, ModuleReference> namesToReference = new HashMap<>();
        for (ModuleDescriptor d : descriptors) {
            ModuleReference mref = new ModuleReference(d, URI.create("module:/" + d.name())) {
                @Override public ModuleReader open() { throw new UnsupportedOperationException(); }
            };
            namesToReference.put(d.name(), mref);
        }
        return new ModuleFinder() {
            @Override public Optional<ModuleReference> find(String name) {
                return Optional.ofNullable(namesToReference.get(name));
            }
            @Override public Set<ModuleReference> findAll() { return new HashSet<>(namesToReference.values()); }
        };
    }

    static Configuration resolveAuto(ModuleLayer parent, String moduleName) {
        ModuleDescriptor d = ModuleDescriptor.newAutomaticModule(moduleName).build();
        return parent.configuration().resolve(finderOf(d), ModuleFinder.of(), Set.of(moduleName));
    }

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ModuleLayer");
        check(Modifier.isFinal(c.getModifiers()), "ModuleLayer is final");
        check(!c.isInterface(), "ModuleLayer is a class");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBoot() {
        ModuleLayer boot = ModuleLayer.boot();
        check(boot != null, "boot non-null");
        check(boot.findModule("java.base").isPresent(), "boot has java.base");
        check(boot.modules().size() > 0, "boot modules non-empty");
        check(boot.configuration().modules().size() > 0, "boot configuration non-empty");
        check(boot.findLoader("java.base") == null, "findLoader java.base bootstrap (null)");
        check(boot.parents().size() == 1, "boot layer has exactly one parent (the empty layer)");
        check(boot.parents().get(0).modules().isEmpty(), "boot layer's parent is the empty layer");
        check(boot.toString() != null && !boot.toString().isEmpty(), "toString non-empty");
    }

    static void testEmpty() {
        ModuleLayer empty = ModuleLayer.empty();
        check(empty.modules().isEmpty(), "empty layer has no modules");
        check(empty.configuration().modules().isEmpty(), "empty configuration");
        check(!empty.findModule("java.base").isPresent(), "empty layer lacks java.base");
    }

    // ---- Part 2b: black-box for the defineModules* factory methods ----
    static void testBlackBox() {
        ModuleLayer boot = ModuleLayer.boot();

        ModuleLayer oneLoader = boot.defineModulesWithOneLoader(
            resolveAuto(boot, "jck.m1"), ClassLoader.getSystemClassLoader());
        check(oneLoader != null && oneLoader.findModule("jck.m1").isPresent(),
              "defineModulesWithOneLoader(Configuration,ClassLoader) defines jck.m1");

        ModuleLayer manyLoaders = boot.defineModulesWithManyLoaders(
            resolveAuto(boot, "jck.m2"), ClassLoader.getSystemClassLoader());
        check(manyLoaders != null && manyLoaders.findModule("jck.m2").isPresent(),
              "defineModulesWithManyLoaders(Configuration,ClassLoader) defines jck.m2");

        ModuleLayer fnLayer = boot.defineModules(
            resolveAuto(boot, "jck.m3"), name -> ClassLoader.getSystemClassLoader());
        check(fnLayer != null && fnLayer.findModule("jck.m3").isPresent(),
              "defineModules(Configuration,Function) defines jck.m3");

        ModuleLayer.Controller ctlOne = ModuleLayer.defineModulesWithOneLoader(
            resolveAuto(boot, "jck.m4"), List.of(boot), ClassLoader.getSystemClassLoader());
        check(ctlOne != null && ctlOne.layer() != null && ctlOne.layer().findModule("jck.m4").isPresent(),
              "static defineModulesWithOneLoader(Configuration,List,ClassLoader) defines jck.m4");

        ModuleLayer.Controller ctlMany = ModuleLayer.defineModulesWithManyLoaders(
            resolveAuto(boot, "jck.m5"), List.of(boot), ClassLoader.getSystemClassLoader());
        check(ctlMany != null && ctlMany.layer() != null && ctlMany.layer().findModule("jck.m5").isPresent(),
              "static defineModulesWithManyLoaders(Configuration,List,ClassLoader) defines jck.m5");

        ModuleLayer.Controller ctlFn = ModuleLayer.defineModules(
            resolveAuto(boot, "jck.m6"), List.of(boot), name -> ClassLoader.getSystemClassLoader());
        check(ctlFn != null && ctlFn.layer() != null && ctlFn.layer().findModule("jck.m6").isPresent(),
              "static defineModules(Configuration,List,Function) defines jck.m6");
    }

    // ---- Part 3: contract deepening (2026-11) ----
    static ModuleDescriptor autoModule(String name) {
        return ModuleDescriptor.newAutomaticModule(name).build();
    }

    // boot layer: exact parents, identity of java.base module, loaders, immutability
    static void testBootContract() {
        ModuleLayer boot = ModuleLayer.boot();
        ModuleLayer empty = ModuleLayer.empty();
        check(ModuleLayer.empty() == empty, "empty() is a cached singleton");
        check(boot.parents().size() == 1 && boot.parents().get(0) == empty,
              "boot parent is exactly the shared empty layer");
        check(empty.configuration() == Configuration.empty(),
              "empty layer's configuration is Configuration.empty()");
        Module base = boot.findModule("java.base").orElseThrow();
        check(base == String.class.getModule(), "boot java.base Module is String's module");
        check(boot.modules().contains(base), "modules() contains the java.base Module");
        check(boot.findModule("java.base").isPresent()
              && "java.base".equals(boot.findModule("java.base").get().getName()),
              "findModule name round trip");
        check(boot.findModule("no.such.module").isEmpty(), "findModule absent empty");
        check(boot.configuration().findModule("java.base").isPresent(),
              "configuration().findModule(java.base) present");
        check(boot.configuration() == ModuleLayer.boot().configuration(),
              "configuration() stable across boot() calls");
        check(boot.parents().equals(List.of(empty)), "parents() equals [empty]");
        try {
            boot.modules().add(null);
            check(false, "modules() immutable");
        } catch (UnsupportedOperationException e) {
            check(true, "modules() immutable");
        }
        try {
            boot.parents().add(null);
            check(false, "parents() immutable");
        } catch (UnsupportedOperationException e) {
            check(true, "parents() immutable");
        }
        try {
            boot.findModule(null);
            check(false, "findModule(null) NPE");
        } catch (NullPointerException e) {
            check(true, "findModule(null) NPE");
        }
        check(boot.findLoader("java.base") == null, "java.base defined to the bootstrap loader");
        check(boot.findLoader("java.logging") == null
              && boot.findLoader("java.logging") == java.util.logging.Logger.class.getClassLoader(),
              "findLoader(java.logging) matches the class's loader");
        try {
            boot.findLoader("no.such.mod");
            check(false, "findLoader(absent) IAE");
        } catch (IllegalArgumentException e) {
            check("Module no.such.mod not known to this layer".equals(e.getMessage()),
                  "findLoader(absent) exact message");
        }
        check(boot.toString() != null && boot.toString().contains("java.base"),
              "toString lists module names");
    }

    static void testEmptyContract() {
        ModuleLayer empty = ModuleLayer.empty();
        check(empty.modules().isEmpty(), "empty modules empty");
        try {
            empty.modules().add(null);
            check(false, "empty modules immutable");
        } catch (UnsupportedOperationException e) {
            check(true, "empty modules immutable");
        }
        check(empty.parents().isEmpty(), "empty parents empty list");
        try {
            empty.parents().add(null);
            check(false, "empty parents immutable");
        } catch (UnsupportedOperationException e) {
            check(true, "empty parents immutable");
        }
        check(empty.configuration().modules().isEmpty(), "empty configuration has no modules");
        check(empty.findModule("java.base").isEmpty(), "empty layer cannot see java.base");
        try {
            empty.findLoader("java.base");
            check(false, "empty findLoader IAE");
        } catch (IllegalArgumentException e) {
            check(true, "empty findLoader IAE");
        }
        check("".equals(empty.toString()), "empty layer toString is the empty string");
    }

    // real defineModulesWithOneLoader closed loop over an empty-root resolution
    static void testDefineEmptyLayerLoop() {
        Configuration bootCfg = ModuleLayer.boot().configuration();
        Configuration emptyChild = bootCfg.resolve(ModuleFinder.of(), ModuleFinder.of(), Set.of());
        ModuleLayer.Controller ctl = ModuleLayer.defineModulesWithOneLoader(
                emptyChild, List.of(ModuleLayer.boot()), ModuleLayerTest.class.getClassLoader());
        ModuleLayer layer = ctl.layer();
        check(layer != null, "layer created");
        check(layer.modules().isEmpty(), "no roots -> layer has no modules of its own");
        check(layer.parents().size() == 1 && layer.parents().get(0) == ModuleLayer.boot(),
              "layer parent is exactly boot");
        check(layer.configuration() == emptyChild, "layer configuration is the resolved one");
        check(layer.findModule("java.base").isPresent(),
              "findModule consults parent layers (java.base visible)");
        check(layer.findLoader("java.base") == null,
              "findLoader delegates to the parent (bootstrap loader)");
        check(layer.findModule("no.such.module").isEmpty(), "absent still absent through parents");
        // defining the same configuration twice yields independent layers
        ModuleLayer second = ModuleLayer.defineModulesWithOneLoader(
                emptyChild, List.of(ModuleLayer.boot()), ModuleLayerTest.class.getClassLoader()).layer();
        check(second != layer && second.modules().isEmpty(), "same config can back two layers");
    }

    // one-loader vs many-loaders vs function mapping: loader topology contract
    static void testLoaderTopology() {
        Configuration bootCfg = ModuleLayer.boot().configuration();
        Configuration cfgA = bootCfg.resolve(
                finderOf(autoModule("jck.top.a"), autoModule("jck.top.b")),
                ModuleFinder.of(), Set.of("jck.top.a", "jck.top.b"));
        ClassLoader parent = ModuleLayerTest.class.getClassLoader();

        ModuleLayer one = ModuleLayer.defineModulesWithOneLoader(cfgA, List.of(ModuleLayer.boot()), parent).layer();
        ClassLoader la = one.findLoader("jck.top.a");
        ClassLoader lb = one.findLoader("jck.top.b");
        check(la == lb, "one-loader: both modules share the loader");
        check(la.getParent() == parent, "one-loader: parent chain to the given loader");
        check(one.findModule("jck.top.a").orElseThrow().getClassLoader() == la,
              "Module.getClassLoader() agrees with findLoader");
        check(!la.equals(parent) && la != parent, "one-loader creates a child loader, not reuse");

        Configuration cfgB = bootCfg.resolve(
                finderOf(autoModule("jck.top2.a"), autoModule("jck.top2.b")),
                ModuleFinder.of(), Set.of("jck.top2.a", "jck.top2.b"));
        ModuleLayer many = ModuleLayer.defineModulesWithManyLoaders(cfgB, List.of(ModuleLayer.boot()), parent).layer();
        check(many.findLoader("jck.top2.a") != many.findLoader("jck.top2.b"),
              "many-loaders: distinct loader per module");
        check(many.findLoader("jck.top2.a").getParent() == parent,
              "many-loaders: parents still the given loader");

        java.util.Set<String> seen = new HashSet<>();
        Configuration cfgC = bootCfg.resolve(
                finderOf(autoModule("jck.top3.a"), autoModule("jck.top3.b")),
                ModuleFinder.of(), Set.of("jck.top3.a", "jck.top3.b"));
        ModuleLayer mapped = ModuleLayer.defineModules(
                cfgC, List.of(ModuleLayer.boot()), name -> { seen.add(name); return parent; }).layer();
        check(seen.contains("jck.top3.a") && seen.contains("jck.top3.b"),
              "map function invoked once per module");
        check(mapped.findLoader("jck.top3.a") == parent && mapped.findLoader("jck.top3.b") == parent,
              "map function's loader assignment honored");
        check(mapped.findModule("jck.top3.a").orElseThrow().canRead(String.class.getModule()),
              "layer modules read java.base");
        check(mapped.findModule("jck.top3.a").orElseThrow().getLayer() == mapped,
              "module reports its defining layer");
    }

    // Controller mutation contract on self-made modules only
    static void testControllerContract() {
        ModuleDescriptor d = ModuleDescriptor.newModule("jck.ctl.m")
                .packages(Set.of("jck.ctl.pkg")).exports("jck.ctl.pkg").build();
        Configuration cfg = ModuleLayer.boot().configuration().resolve(
                finderOf(d), ModuleFinder.of(), Set.of("jck.ctl.m"));
        ModuleLayer.Controller ctl = ModuleLayer.defineModulesWithOneLoader(
                cfg, List.of(ModuleLayer.boot()), ModuleLayerTest.class.getClassLoader());
        ModuleLayer layer = ctl.layer();
        Module mod = layer.findModule("jck.ctl.m").orElseThrow();
        Module unnamed = ModuleLayerTest.class.getModule();
        check(mod.isExported("jck.ctl.pkg", unnamed), "unqualified export visible pre-mutation");
        check(!mod.isOpen("jck.ctl.pkg", unnamed), "not open pre-mutation");
        ModuleLayer.Controller ret = ctl.addExports(mod, "jck.ctl.pkg", unnamed);
        check(ret == ctl, "addExports returns this controller");
        check(mod.isExported("jck.ctl.pkg", unnamed), "still exported (idempotent)");
        ctl.addOpens(mod, "jck.ctl.pkg", unnamed);
        check(mod.isOpen("jck.ctl.pkg", unnamed), "addOpens opened the package to the unnamed module");
        ctl.addReads(mod, unnamed);
        check(mod.canRead(unnamed), "addReads granted readability of the unnamed module");
        try {
            ctl.addExports(mod, "no.such.pkg", unnamed);
            check(false, "addExports unknown package IAE");
        } catch (IllegalArgumentException e) {
            check("package no.such.pkg not in contents".equals(e.getMessage()),
                  "addExports unknown package exact message");
        }
        // controller cannot mutate a module of another layer
        Module javaBase = String.class.getModule();
        try {
            ctl.addExports(javaBase, "java.lang", unnamed);
            check(false, "addExports module not in layer IAE");
        } catch (IllegalArgumentException e) {
            check(true, "addExports module not in layer IAE");
        }
        try {
            ctl.enableNativeAccess(javaBase);
            check(false, "enableNativeAccess module not in layer IAE");
        } catch (IllegalArgumentException e) {
            check("module java.base not in layer".equals(e.getMessage()),
                  "enableNativeAccess exact message");
        }
        ctl.enableNativeAccess(mod);
        check(mod.isNativeAccessEnabled(), "enableNativeAccess flips the flag");
        check(layer.findLoader("jck.ctl.m") == mod.getClassLoader(),
              "controller layer stays consistent");
    }

    // real LayerInstantiationException: same package in two modules of one layer
    static void testLayerInstantiationTrigger() {
        Configuration bootCfg = ModuleLayer.boot().configuration();
        ModuleDescriptor s1 = ModuleDescriptor.newModule("jck.sp.m1").packages(Set.of("jck.sp")).exports("jck.sp").build();
        ModuleDescriptor s2 = ModuleDescriptor.newModule("jck.sp.m2").packages(Set.of("jck.sp")).exports("jck.sp").build();
        Configuration cfg = bootCfg.resolve(finderOf(s1, s2), ModuleFinder.of(),
                Set.of("jck.sp.m1", "jck.sp.m2"));
        try {
            ModuleLayer.boot().defineModulesWithOneLoader(cfg, ModuleLayerTest.class.getClassLoader());
            check(false, "split package must fail layer creation");
        } catch (LayerInstantiationException e) {
            check("Package jck.sp in more than one module".equals(e.getMessage()),
                  "split package exact message");
        }
    }

    // ---- Part 4: white-box deepening ----
    static void testWhiteBoxDeepen() throws Exception {
        Class<?> c = Class.forName("java.lang.ModuleLayer");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor");
        for (String m : new String[] {"boot", "empty", "defineModules", "defineModulesWithOneLoader",
                "defineModulesWithManyLoaders", "modules", "parents", "findModule", "findLoader",
                "configuration", "toString"}) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(m)) found = true;
            check(found, "declares " + m);
        }
        Class<?> ctl = Class.forName("java.lang.ModuleLayer$Controller");
        check(Modifier.isPublic(ctl.getModifiers()) && Modifier.isFinal(ctl.getModifiers()),
              "Controller is a public final nested class");
        int ctlCtorCount = 0;
        for (java.lang.reflect.Constructor<?> ct : ctl.getDeclaredConstructors()) ctlCtorCount++;
        check(ctlCtorCount >= 1, "Controller has a (non-public) constructor");
        check(ModuleLayer.boot().defineModulesWithOneLoader(
                      ModuleLayer.boot().configuration().resolve(
                          ModuleFinder.of(), ModuleFinder.of(), Set.of()),
                      List.of(ModuleLayer.boot()),
                      ModuleLayerTest.class.getClassLoader()).layer().modules().isEmpty(),
              "instance defineModulesWithOneLoader(cfg, loader) with empty cfg works too");
    }

    // boot layer observability: names, sizes, layers of every module, config parent chain
    static void testBootGraphContract() {
        ModuleLayer boot = ModuleLayer.boot();
        java.util.Set<String> names = new HashSet<>();
        for (Module m : boot.modules()) {
            names.add(m.getName());
        }
        check(names.size() == boot.modules().size(), "module names unique");
        check(names.contains("java.base"), "names contains java.base");
        check(names.contains("java.logging"), "names contains java.logging");
        check(names.contains("java.sql"), "names contains java.sql");
        check(boot.modules().size() == boot.configuration().modules().size(),
              "layer modules and configuration modules agree in size");
        boolean allInBoot = true;
        for (Module m : boot.modules()) {
            if (m.getLayer() != boot) allInBoot = false;
        }
        check(allInBoot, "every boot module reports boot as its layer");
        check(boot.configuration().parents().equals(List.of(Configuration.empty())),
              "boot configuration parent is [Configuration.empty()]");
        check(boot.findModule("java.sql").orElseThrow().getDescriptor().name().equals("java.sql"),
              "module descriptor name agrees with findModule key");
        // resolution through a custom child layer
        Configuration cfg = boot.configuration().resolve(
                finderOf(autoModule("jck.child.m")), ModuleFinder.of(), Set.of("jck.child.m"));
        ModuleLayer child = ModuleLayer.defineModulesWithOneLoader(
                cfg, List.of(boot), ModuleLayerTest.class.getClassLoader()).layer();
        check(child.parents().equals(List.of(boot)), "child layer parents [boot]");
        check(child.findModule("jck.child.m").isPresent(), "child sees own module");
        check(child.findModule("java.base").isPresent(), "child sees parent modules");
        check(!boot.findModule("jck.child.m").isPresent(), "boot does not see child modules");
        Module cm = child.findModule("jck.child.m").orElseThrow();
        check(cm.getLayer() == child, "child module owned by child layer");
        check(cm.getClassLoader() != null, "child module loader non-null");
        check("module jck.child.m".equals(cm.toString()), "child module toString form");
        try {
            child.modules().add(null);
            check(false, "child modules() immutable");
        } catch (UnsupportedOperationException e) {
            check(true, "child modules() immutable");
        }
        boolean allNamed = true;
        for (Module m : boot.modules()) {
            if (!m.isNamed()) allNamed = false;
        }
        check(allNamed, "every boot-layer module is named");
        check(child.configuration().modules().size() == 1
              && "jck.child.m".equals(child.configuration().modules().iterator().next().name()),
              "child configuration carries exactly the resolved module");
    }

    // multiple parents and two-level loader delegation
    static void testMultiParentDelegation() {
        ModuleLayer boot = ModuleLayer.boot();
        Configuration cfg1 = Configuration.resolve(
                finderOf(autoModule("jck.mp.a")),
                List.of(boot.configuration(), Configuration.empty()),
                ModuleFinder.of(), Set.of("jck.mp.a"));
        ModuleLayer l1 = ModuleLayer.defineModulesWithOneLoader(
                cfg1, List.of(boot, ModuleLayer.empty()), ModuleLayerTest.class.getClassLoader()).layer();
        check(l1.parents().size() == 2 && l1.parents().get(0) == boot
              && l1.parents().get(1) == ModuleLayer.empty(),
              "defineModules keeps the parent list order");
        check(cfg1.parents().equals(List.of(boot.configuration(), Configuration.empty())),
              "configuration parents mirror the layer parents");
        Configuration cfg2 = cfg1.resolve(finderOf(autoModule("jck.mp.b")), ModuleFinder.of(),
                Set.of("jck.mp.b"));
        ModuleLayer l2 = ModuleLayer.defineModulesWithOneLoader(
                cfg2, List.of(l1), ModuleLayerTest.class.getClassLoader()).layer();
        check(l2.parents().equals(List.of(l1)), "grandchild parent chain");
        check(l2.findLoader("jck.mp.b") != null, "own module loader found");
        check(l2.findLoader("jck.mp.a") == l1.findLoader("jck.mp.a"),
              "findLoader delegates through parent layers");
        check(l2.findLoader("java.base") == null, "delegation reaches boot's bootstrap loader");
        check(l2.findModule("jck.mp.a").isPresent() && l2.findModule("jck.mp.b").isPresent()
              && l2.findModule("java.base").isPresent(),
              "module visibility composes across three layers");
        check(l2.modules().size() == 1, "own modules only");
    }

    // addOpens/addReads negative shapes on a controller
    static void testControllerNegatives() {
        ModuleDescriptor d = ModuleDescriptor.newModule("jck.neg.m")
                .packages(Set.of("jck.neg.pkg")).exports("jck.neg.pkg").build();
        Configuration cfg = ModuleLayer.boot().configuration().resolve(
                finderOf(d), ModuleFinder.of(), Set.of("jck.neg.m"));
        ModuleLayer.Controller ctl = ModuleLayer.defineModulesWithOneLoader(
                cfg, List.of(ModuleLayer.boot()), ModuleLayerTest.class.getClassLoader());
        Module mod = ctl.layer().findModule("jck.neg.m").orElseThrow();
        Module unnamed = ModuleLayerTest.class.getModule();
        try {
            ctl.addOpens(mod, "no.such.pkg", unnamed);
            check(false, "addOpens unknown package IAE");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("no.such.pkg"),
                  "addOpens unknown package names the package");
        }
        try {
            ctl.addReads(String.class.getModule(), mod);
            check(false, "addReads module not in layer IAE");
        } catch (IllegalArgumentException e) {
            check(true, "addReads module not in layer IAE");
        }
        try {
            ctl.addOpens(mod, "jck.neg.pkg", String.class.getModule());
            check(true, "addOpens to a named target module allowed");
        } catch (RuntimeException e) {
            check(false, "addOpens to named target should not throw: " + e);
        }
        check(mod.isOpen("jck.neg.pkg", String.class.getModule()),
              "addOpens target visibility (java.base can now reflect jck.neg.pkg)");
        ctl.addReads(mod, String.class.getModule());
        check(mod.canRead(String.class.getModule()), "addReads to java.base");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testWhiteBoxDeepen();
        testBoot();
        testBootContract();
        testBootGraphContract();
        testEmpty();
        testEmptyContract();
        testBlackBox();
        testDefineEmptyLayerLoop();
        testLoaderTopology();
        testMultiParentDelegation();
        testControllerContract();
        testControllerNegatives();
        testLayerInstantiationTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
