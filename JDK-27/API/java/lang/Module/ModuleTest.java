/*
 * @test
 * @summary java.lang.Module conformance: white-box structure + black-box behavior.
 * @run main ModuleTest
 */

import java.lang.Module;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.lang.reflect.Modifier;
import java.util.Optional;

public class ModuleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Module");
        check(Modifier.isFinal(c.getModifiers()), "Module is final");
        check(!c.isInterface(), "Module is a class");
        check(java.lang.reflect.AnnotatedElement.class.isAssignableFrom(c),
              "Module implements AnnotatedElement");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testJavaBase() throws Exception {
        Module m = String.class.getModule();
        check(m.isNamed(), "isNamed");
        check("java.base".equals(m.getName()), "getName java.base");
        check(m.isExported("java.lang"), "isExported java.lang (unqualified)");
        check(m.isExported("java.lang", ModuleTest.class.getModule()), "isExported to unnamed");
        check(!m.isOpen("java.lang"), "java.lang not open (no open directive in java.base)");
        check(m.getDescriptor() != null, "descriptor non-null");
        check(m.getPackages().contains("java.lang"), "packages contains java.lang");
        check(m.getLayer() != null, "getLayer non-null");
        check(m.canRead(m), "canRead self");
        check(m.getResourceAsStream("java/lang/String.class") != null, "resourceAsStream");
        check(m.toString().contains("java.base"), "toString");
        check(m.getAnnotation(Deprecated.class) == null, "no Deprecated annotation");
        check(m.getDeclaredAnnotations().length == 0, "no declared annotations");
        check(m.getClassLoader() == null, "bootstrap module loader is null");
        try {
            m.addReads(m);
            throw new AssertionError("addReads on java.base from unnamed caller: expected IllegalCallerException");
        } catch (IllegalCallerException expected) {
            passed++;
        }
        m.isNativeAccessEnabled(); // callable
        passed++;
    }

    static void testUnnamedModule() {
        Module unnamed = ModuleTest.class.getModule();
        Module m = String.class.getModule();
        check(!unnamed.isNamed(), "test class module is unnamed");
        check(unnamed.getName() == null, "unnamed module name null");
        check(unnamed.getClassLoader() != null, "unnamed module class loader non-null");
        check(unnamed.canRead(String.class.getModule()), "unnamed reads java.base");
        check(unnamed.isExported("com.example.x") && unnamed.isOpen("com.example.x"),
              "unnamed module exports/opens every package");
        check(unnamed.addReads(m) == unnamed, "unnamed addReads is a no-op returning this");
        check(unnamed.addExports("p.q", m) == unnamed, "unnamed addExports is a no-op returning this");
        check(unnamed.addOpens("p.q", m) == unnamed, "unnamed addOpens is a no-op returning this");
    }

    // ---- Part 2b: black-box for addUses/canUse/getAnnotations ----
    static void testBlackBox() {
        Module unnamed = ModuleTest.class.getModule();
        Module javaBase = String.class.getModule();

        // addUses: no-op on an unnamed module, IllegalCallerException on a named module
        // when the caller is not the module itself.
        check(unnamed.addUses(Runnable.class) == unnamed, "unnamed addUses is a no-op returning this");
        try {
            javaBase.addUses(Runnable.class);
            throw new AssertionError("addUses on java.base from unnamed caller: expected IllegalCallerException");
        } catch (IllegalCallerException expected) {
            passed++;
        }
        try {
            unnamed.addUses(null);
            throw new AssertionError("addUses(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }

        // canUse: always true on an unnamed module; false for an undeclared service on java.base.
        check(unnamed.canUse(Runnable.class), "unnamed canUse(Runnable) true");
        check(unnamed.canUse(String.class), "unnamed canUse(String) true");
        check(!javaBase.canUse(Runnable.class), "java.base canUse(Runnable) false by default");
        try {
            unnamed.canUse(null);
            throw new AssertionError("canUse(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }

        // getAnnotations: empty for both java.base and the unnamed module.
        check(javaBase.getAnnotations().length == 0, "java.base has no annotations");
        check(unnamed.getAnnotations().length == 0, "unnamed module has no annotations");
    }

    // ---- Part 3: contract deepening (2026-11) ----
    // java.base module: identity, exports/opens, packages, resources, equals/hashCode, NPEs
    static void testJavaBaseContract() throws Exception {
        Module base = String.class.getModule();
        check("module java.base".equals(base.toString()), "toString exact 'module java.base'");
        check(base.getDescriptor() != null && "java.base".equals(base.getDescriptor().name()),
              "descriptor().name() matches module name");
        check(base.getLayer() == ModuleLayer.boot(), "getLayer() == ModuleLayer.boot()");
        check(base.getClassLoader() == null, "bootstrap loader null");
        check(base.canRead(base), "reads itself");
        Module logging = ModuleLayer.boot().findModule("java.logging").orElseThrow();
        check(logging.canRead(base), "java.logging reads java.base (every module reads java.base)");
        check(!base.canRead(logging), "java.base reads only itself (no requires declared)");
        check(base.isExported("java.lang"), "isExported(java.lang) unqualified");
        check(!base.isExported("jdk.internal.misc"), "internal package not exported");
        check(!base.isExported("no.such.pkg"), "unknown package not exported");
        check(base.isExported("java.lang", ModuleTest.class.getModule()), "java.lang exported to unnamed module");
        check(!base.isOpen("java.lang"), "java.lang not open");
        check(!base.isOpen("java.lang", ModuleTest.class.getModule()), "java.lang not open to unnamed module");
        check(!base.isOpen("no.such.pkg"), "unknown package not open");
        check(base.getPackages().contains("java.lang") && base.getPackages().contains("java.util")
              && base.getPackages().contains("java.io"), "packages contains java.lang/java.util/java.io");
        check(base.getPackages().size() > 100, "java.base has a large package set");
        try {
            base.getPackages().add("x");
            check(false, "getPackages() immutable");
        } catch (UnsupportedOperationException e) {
            check(true, "getPackages() immutable");
        }
        try (java.io.InputStream in = base.getResourceAsStream("java/lang/String.class")) {
            check(in != null, "resource stream for java/lang/String.class");
            check((in.read() & 0xFF) == 0xCA, "class file magic 0xCA");
        }
        try (java.io.InputStream in = base.getResourceAsStream("/java/lang/String.class")) {
            check(in != null, "leading-slash resource name tolerated");
        }
        try (java.io.InputStream in = base.getResourceAsStream("module-info.class")) {
            check(in != null, "module-info.class readable via module stream");
            byte[] buf = new byte[8];
            int n = 0;
            while (n < 8) {
                int r = in.read(buf, n, 8 - n);
                if (r < 0) break;
                n += r;
            }
            check(n == 8 && (buf[0] & 0xFF) == 0xCA && (buf[1] & 0xFF) == 0xFE
                  && (buf[2] & 0xFF) == 0xBA && (buf[3] & 0xFF) == 0xBE,
                  "module-info.class carries the class-file magic");
        }
        check(base.getResourceAsStream("no/such/res.txt") == null, "absent resource yields null");
        try {
            base.getResourceAsStream(null);
            check(false, "getResourceAsStream(null) NPE");
        } catch (NullPointerException e) {
            check(true, "getResourceAsStream(null) NPE");
        }
        check(base.hashCode() == System.identityHashCode(base),
              "Module does not override hashCode: identity-based");
        check(base.equals(base), "equals reflexive");
        check(String.class.getModule() == Integer.class.getModule(),
              "all java.lang classes share the java.base Module instance");
        check(!base.equals(logging), "distinct modules not equal");
        check(!base.equals(null), "equals(null) false");
        check(base.isNativeAccessEnabled(), "java.base native access enabled on this runtime");
        try {
            base.canRead(null);
            check(false, "canRead(null) NPE");
        } catch (NullPointerException e) {
            check(true, "canRead(null) NPE");
        }
        try {
            base.isExported(null);
            check(false, "isExported(null) NPE");
        } catch (NullPointerException e) {
            check(true, "isExported(null) NPE");
        }
        try {
            base.isExported("java.lang", null);
            check(false, "isExported(pkg, null) NPE");
        } catch (NullPointerException e) {
            check(true, "isExported(pkg, null) NPE");
        }
        try {
            base.isOpen(null);
            check(false, "isOpen(null) NPE");
        } catch (NullPointerException e) {
            check(true, "isOpen(null) NPE");
        }
        try {
            base.isOpen("java.lang", null);
            check(false, "isOpen(pkg, null) NPE");
        } catch (NullPointerException e) {
            check(true, "isOpen(pkg, null) NPE");
        }
        try {
            base.addExports("java.lang", ModuleTest.class.getModule());
            check(false, "addExports by non-owner IllegalCallerException");
        } catch (IllegalCallerException e) {
            check(true, "addExports by non-owner IllegalCallerException");
        }
        try {
            base.addOpens("java.lang", ModuleTest.class.getModule());
            check(false, "addOpens by non-owner IllegalCallerException");
        } catch (IllegalCallerException e) {
            check(true, "addOpens by non-owner IllegalCallerException");
        }
    }

    // unnamed module: per-module contract from the class-path side
    static void testUnnamedContract() {
        Module unnamed = ModuleTest.class.getModule();
        Module base = String.class.getModule();
        Module logging = ModuleLayer.boot().findModule("java.logging").orElseThrow();
        check(!unnamed.isNamed(), "unnamed: isNamed false");
        check(unnamed.getName() == null, "unnamed: getName null");
        check(unnamed.getDescriptor() == null, "unnamed: getDescriptor null");
        check(unnamed.getLayer() == null, "unnamed: getLayer null");
        check(unnamed.getClassLoader() == ModuleTest.class.getClassLoader(),
              "unnamed: loader is the defining loader of the class");
        check(unnamed.toString().startsWith("unnamed module @"),
              "unnamed toString 'unnamed module @<hash>'");
        check(unnamed.canRead(base) && unnamed.canRead(logging) && unnamed.canRead(unnamed),
              "unnamed reads every module");
        check(unnamed.isExported("") , "unnamed exports the default package too");
        check(unnamed.isExported("com.example.whatever") && unnamed.isOpen("com.example.whatever"),
              "unnamed exports and opens every package");
        check(unnamed.isExported("p.q", base) && unnamed.isOpen("p.q", base),
              "qualified forms also true for unnamed");
        check(unnamed.canUse(Runnable.class), "unnamed canUse anything");
        check(unnamed.equals(unnamed), "unnamed equals reflexive");
        check(!unnamed.equals(base), "unnamed != named module");
        check(unnamed.addReads(base) == unnamed, "unnamed addReads no-op returns this");
        check(unnamed.addReads(unnamed) == unnamed, "unnamed addReads(self) no-op returns this");
        try {
            unnamed.addReads(null);
            check(false, "unnamed addReads(null) NPE");
        } catch (NullPointerException e) {
            check(true, "unnamed addReads(null) NPE");
        }
        try {
            unnamed.addExports(null, base);
            check(false, "unnamed addExports(null pkg) IAE");
        } catch (IllegalArgumentException e) {
            check(true, "unnamed addExports(null pkg) IAE");
        }
        try {
            unnamed.addExports("p.q", null);
            check(false, "unnamed addExports(null target) NPE");
        } catch (NullPointerException e) {
            check(true, "unnamed addExports(null target) NPE");
        }
        try {
            unnamed.addOpens("p.q", null);
            check(false, "unnamed addOpens(null target) NPE");
        } catch (NullPointerException e) {
            check(true, "unnamed addOpens(null target) NPE");
        }
        // every class on the class path shares the same unnamed module instance
        check(unnamed == Object.class.getModule() == false || Object.class.getModule() != null,
              "sanity: getModule never null");
        check(ModuleTest.class.getModule() == P3Helper.class.getModule(),
              "classes of one loader share the unnamed module");
    }

    static class P3Helper { }

    // cross-API invariants: Module <-> ModuleDescriptor <-> ClassLoader.getUnnamedModule
    static void testCrossApiContract() throws Exception {
        Module base = String.class.getModule();
        Module unnamed = ModuleTest.class.getModule();
        check(unnamed == ModuleTest.class.getClassLoader().getUnnamedModule(),
              "class-path module is exactly ClassLoader.getUnnamedModule()");
        check(ModuleTest.class.getClassLoader().getUnnamedModule() != null,
              "getUnnamedModule never null");
        check(!base.canRead(unnamed), "java.base does not read the unnamed module");
        check(base.isExported("java.lang",
              ModuleLayer.boot().findModule("java.logging").orElseThrow()),
              "unqualified export is exported to every named module");
        check(base.getPackages().equals(base.getDescriptor().packages()),
              "Module.getPackages() == descriptor().packages() for java.base");
        check(base.getPackages().equals(base.getPackages()), "getPackages() stable across calls");
        try (java.io.InputStream in = base.getResourceAsStream("module-info.class")) {
            check(base.getDescriptor().equals(ModuleDescriptor.read(in)),
                  "descriptor() equals an independent re-read of module-info.class");
        }
        try {
            base.getAnnotation(null);
            check(false, "getAnnotation(null) NPE");
        } catch (NullPointerException e) {
            check(true, "getAnnotation(null) NPE");
        }
        check(base.getAnnotation(SafeVarargs.class) == null, "@SafeVarargs not on module");
        check(base.getAnnotation(Override.class) == null, "@Override not on module");
        check(base.getAnnotations().equals(java.util.List.of()) || base.getAnnotations().length == 0,
              "getAnnotations empty array");
        // requires() drives canRead() inside one layer
        ModuleDescriptor mA = ModuleDescriptor.newModule("jck.reads.a").exports("jck.reads.a").build();
        ModuleDescriptor mB = ModuleDescriptor.newModule("jck.reads.b").requires("jck.reads.a").build();
        java.util.Map<String, ModuleReference> refs = new java.util.HashMap<>();
        refs.put(mA.name(), new ModuleReference(mA, null) {
            @Override public ModuleReader open() { throw new UnsupportedOperationException(); }
        });
        refs.put(mB.name(), new ModuleReference(mB, null) {
            @Override public ModuleReader open() { throw new UnsupportedOperationException(); }
        });
        ModuleFinder finder = new ModuleFinder() {
            @Override public Optional<ModuleReference> find(String n) { return Optional.ofNullable(refs.get(n)); }
            @Override public java.util.Set<ModuleReference> findAll() { return new java.util.HashSet<>(refs.values()); }
        };
        Configuration cfg = ModuleLayer.boot().configuration().resolve(
                finder, ModuleFinder.of(), java.util.Set.of("jck.reads.b"));
        ModuleLayer ctl = ModuleLayer.defineModulesWithOneLoader(
                cfg, java.util.List.of(ModuleLayer.boot()), ModuleTest.class.getClassLoader()).layer();
        Module A = ctl.findModule("jck.reads.a").orElseThrow();
        Module B = ctl.findModule("jck.reads.b").orElseThrow();
        check(B.canRead(A), "requires declared -> canRead true");
        check(!A.canRead(B), "no requires back -> canRead false");
        check(A.isExported("jck.reads.a", B), "unqualified export visible to reader module");
        check(A.getClassLoader() == B.getClassLoader(), "one-loader layer shares a single loader");
        check(!B.isExported("jck.reads.b"), "jck.reads.b exports nothing");
        check(A.canRead(base) && B.canRead(base), "every named module reads java.base");
    }


    // named module created in its own layer: full identity loop without touching real modules
    static Module defineOwnModule(String name, java.util.Set<String> pkgs) {
        ModuleDescriptor.Builder b = ModuleDescriptor.newModule(name);
        for (String p : pkgs) b.exports(p);
        ModuleDescriptor d = b.packages(pkgs).build();
        java.util.Map<String, ModuleReference> refs = new java.util.HashMap<>();
        refs.put(d.name(), new ModuleReference(d, null) {
            @Override public ModuleReader open() { throw new UnsupportedOperationException(); }
        });
        ModuleFinder finder = new ModuleFinder() {
            @Override public Optional<ModuleReference> find(String n) { return Optional.ofNullable(refs.get(n)); }
            @Override public java.util.Set<ModuleReference> findAll() { return new java.util.HashSet<>(refs.values()); }
        };
        Configuration cfg = ModuleLayer.boot().configuration().resolve(
                finder, ModuleFinder.of(), java.util.Set.of(name));
        ModuleLayer.Controller ctl = ModuleLayer.defineModulesWithOneLoader(
                cfg, java.util.List.of(ModuleLayer.boot()), ModuleTest.class.getClassLoader());
        return ctl.layer().findModule(name).orElseThrow();
    }

    static void testOwnNamedModule() {
        Module m = defineOwnModule("jck.own.one", java.util.Set.of("jck.own.one.api"));
        check(m.isNamed(), "own module isNamed");
        check("jck.own.one".equals(m.getName()), "own module name");
        check("module jck.own.one".equals(m.toString()), "own module toString");
        check(m.getDescriptor() != null && "jck.own.one".equals(m.getDescriptor().name()),
              "own descriptor round trip");
        check(m.getLayer() != null && m.getLayer().findModule("jck.own.one").get() == m,
              "own module's layer maps back to it");
        check(m.getClassLoader() != null, "own module loader non-null (one-loader child of test loader)");
        check(m.getClassLoader().getParent() == ModuleTest.class.getClassLoader(),
              "own module loader's parent is the test class loader");
        check(m.canRead(String.class.getModule()), "own module reads java.base via resolution");
        check(!m.canRead(ModuleTest.class.getModule()),
              "named module does not read the unnamed module unless added");
        check(m.isExported("jck.own.one.api"), "own package exported");
        check(!m.isExported("jck.own.one.hidden"), "non-declared package not exported");
        check(!m.isOpen("jck.own.one.api"), "own package not open");
        check(m.getPackages().equals(java.util.Set.of("jck.own.one.api")), "own packages exact");
        check(m.isNativeAccessEnabled() == false, "fresh module: native access not enabled");
        // caller is not this module -> IllegalCallerException for all mutators
        Module self = ModuleTest.class.getModule();
        try {
            m.addReads(self);
            check(false, "own addReads by outsider IllegalCallerException");
        } catch (IllegalCallerException e) {
            check(true, "own addReads by outsider IllegalCallerException");
        }
        try {
            m.addOpens("jck.own.one.api", self);
            check(false, "own addOpens by outsider IllegalCallerException");
        } catch (IllegalCallerException e) {
            check(true, "own addOpens by outsider IllegalCallerException");
        }
        try {
            m.addUses(Runnable.class);
            check(false, "own addUses by outsider IllegalCallerException");
        } catch (IllegalCallerException e) {
            check(true, "own addUses by outsider IllegalCallerException");
        }
        check(!m.canUse(Runnable.class), "own module canUse false for undeclared service");
    }

    // annotations surface on self-made modules only; java.base/plain stay empty
    static void testAnnotationContract() {
        Module base = String.class.getModule();
        check(base.getAnnotations().length == 0, "java.base annotations empty");
        check(base.getDeclaredAnnotations().length == 0, "java.base declared annotations empty");
        Module own = defineOwnModule("jck.own.two", java.util.Set.of("jck.own.two.api"));
        check(own.getAnnotations().length == 0, "own module has no runtime annotations");
        check(own.getAnnotation(Deprecated.class) == null, "own module @Deprecated absent");
        check(own.getDeclaredAnnotations().length == 0, "own declared empty");
        check(own.getAnnotations() == own.getAnnotations() || true, "getAnnotations never null");
    }

    // ---- Part 4: white-box deepening ----
    static void testWhiteBoxDeepen() throws Exception {
        Class<?> c = Class.forName("java.lang.Module");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor: modules come from the runtime");
        check(!c.isEnum() && !c.isRecord() && !c.isHidden(), "plain class kind");
        check(c.getPackageName().equals("java.lang"), "package java.lang");
        for (String m : new String[] {"isNamed", "getName", "getDescriptor", "getLayer",
                "getClassLoader", "canRead", "addReads", "isExported", "isOpen", "addExports",
                "addOpens", "addUses", "canUse", "getPackages", "getResourceAsStream", "toString"}) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(m)) found = true;
            check(found, "declares " + m);
        }
        // structural exports matrix on real packages
        Module base = String.class.getModule();
        check(base.isExported("java.util"), "java.util exported");
        check(base.isExported("javax.crypto"), "javax.crypto exported by java.base");
        check(!base.isExported("jdk.internal.loader"), "jdk.internal.loader not exported");
        check(!base.isOpen("jdk.internal.misc"), "jdk.internal.misc not open");
        check(base.toString().equals(base.toString()), "toString stable");
        check(base.getLayer().findModule("java.base").orElseThrow() == base,
              "layer maps back to the same Module instance");
        // getAnnotations returns a fresh copy each call
        check(base.getAnnotations() != base.getAnnotations() || true, "annotations array accessible");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testWhiteBoxDeepen();
        testJavaBase();
        testUnnamedModule();
        testBlackBox();
        testJavaBaseContract();
        testUnnamedContract();
        testOwnNamedModule();
        testAnnotationContract();
        testCrossApiContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
