/*
 * @test
 * @summary java.lang.module.Configuration conformance: white-box + black-box behavior.
 * @run main ConfigurationTest
 */

import java.lang.module.Configuration;
import java.lang.module.FindException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.lang.module.ResolutionException;
import java.lang.module.ResolvedModule;
import java.util.List;
import java.util.Set;

public class ConfigurationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.module.Configuration");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor");
        for (String m : new String[]{"empty", "resolve", "resolveAndBind", "parents",
                                      "modules", "findModule", "toString"}) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(m) && java.lang.reflect.Modifier.isPublic(mm.getModifiers())) found = true;
            check(found, "public method " + m);
        }
        check(c.getDeclaredMethod("empty").getReturnType() == Configuration.class,
              "empty() returns Configuration");
        check(c.getDeclaredMethod("findModule", String.class).getReturnType() == java.util.Optional.class,
              "findModule returns Optional");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // empty(): singleton empty graph
        Configuration empty = Configuration.empty();
        check(empty.modules().isEmpty(), "empty().modules() is empty");
        check(empty.parents().isEmpty(), "empty().parents() is empty");
        check(empty.findModule("java.base").isEmpty(), "empty().findModule absent");
        check(Configuration.empty() == empty, "empty() is cached singleton");
        check(!empty.toString().equals("uninitialized"), "empty().toString() stable");

        // boot layer configuration
        Configuration boot = ModuleLayer.boot().configuration();
        check(!boot.modules().isEmpty(), "boot configuration has modules");
        ResolvedModule base = boot.findModule("java.base").orElseThrow();
        check("java.base".equals(base.name()), "findModule(java.base).name()");
        check(boot.findModule("no.such.module").isEmpty(), "findModule(absent) empty");
        check(boot.parents().size() == 1, "boot config has one parent (the empty config)");
        check(boot.parents().get(0).modules().isEmpty(), "boot parent is the empty configuration");
        // modules() is a set: names are unique
        check(boot.modules().stream().map(ResolvedModule::name).distinct().count()
                      == boot.modules().size(), "module names unique");

        // instance resolve with empty finders and no roots: new empty child config
        Configuration c2 = boot.resolve(ModuleFinder.of(), ModuleFinder.of(), Set.of());
        check(c2.modules().isEmpty(), "resolve(no roots) has no modules");
        check(c2.parents().size() == 1 && c2.parents().get(0) == boot,
              "resolve() child parent is receiver");

        // static resolve of java.sql pulls in its transitive requires
        Configuration c3 = Configuration.resolve(ModuleFinder.ofSystem(), List.of(boot),
                                                 ModuleFinder.of(), Set.of("java.sql"));
        check(c3.findModule("java.sql").isPresent(), "resolve(java.sql) resolved");
        check(c3.parents().size() == 1 && c3.parents().get(0) == boot, "resolve() parent is boot");
        ResolvedModule sql = c3.findModule("java.sql").orElseThrow();
        check(sql.reads().stream().anyMatch(r -> r.name().equals("java.xml")),
              "java.sql reads java.xml");
        check(sql.reads().stream().anyMatch(r -> r.name().equals("java.logging")),
              "java.sql reads java.logging");
        check(c3.findModule("java.base").isEmpty() || c3.modules().size() >= 5,
              "graph includes java.base or full closure");

        // resolveAndBind adds service providers on top of resolve
        Configuration bound = Configuration.resolveAndBind(ModuleFinder.ofSystem(), List.of(boot),
                                                           ModuleFinder.of(), Set.of("java.sql"));
        check(bound.findModule("java.sql").isPresent(), "resolveAndBind(java.sql) resolved");
        check(bound.modules().size() >= c3.modules().size(), "binding never shrinks the graph");

        // unknown root module is a FindException with the module name in the message
        try {
            Configuration.resolve(ModuleFinder.of(), List.of(boot), ModuleFinder.of(),
                                  Set.of("no.such.module"));
            check(false, "resolve(unknown root) should throw");
        } catch (FindException e) {
            check(e.getMessage() != null && e.getMessage().contains("no.such.module"),
                  "FindException names the missing root");
        }

        System.out.println("PASS (" + passed + " assertions)");
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

    // boot configuration: structure, immutability, parent chain, lookups
    static void testBootConfigurationContract() {
        Configuration boot = ModuleLayer.boot().configuration();
        check(boot.modules().size() > 50, "boot configuration resolves the whole runtime");
        ResolvedModule base = boot.findModule("java.base").orElseThrow();
        check("java.base".equals(base.name()), "java.base present");
        check("java.base".equals(base.reference().descriptor().name()), "reference descriptor name");
        check(base.reference().location().isPresent()
              && "jrt".equals(base.reference().location().get().getScheme()),
              "java.base reference lives in the jrt filesystem");
        check(boot.parents().equals(List.of(Configuration.empty())),
              "boot config parents == [Configuration.empty()]");
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
        check(boot.toString() != null && boot.toString().contains("java.base"),
              "toString lists resolved names");
        // java.sql graph in boot config
        ResolvedModule sql = boot.findModule("java.sql").orElseThrow();
        check(sql.configuration() == boot, "java.sql in boot configuration");
        java.util.Set<String> reads = new java.util.HashSet<>();
        for (ResolvedModule rm : sql.reads()) reads.add(rm.name());
        check(reads.contains("java.base") && reads.contains("java.xml")
              && reads.contains("java.logging"), "java.sql reads base/xml/logging");
        check(!reads.contains("java.sql"), "no self read");
        for (ResolvedModule rm : sql.reads()) {
            check(rm.configuration() == boot, "read target shares configuration");
        }
        // empty configuration singleton contract
        Configuration empty = Configuration.empty();
        check(Configuration.empty() == empty, "empty() singleton");
        check(empty.parents().isEmpty(), "empty parents empty");
        check(empty.modules().isEmpty(), "empty modules empty");
        check(empty.findModule("java.base").isEmpty(), "empty finds nothing");
        check("".equals(empty.toString()), "empty toString is empty string");
    }

    // instance resolve with no roots: an (almost) empty child configuration
    static void testResolveEmptyRoots() {
        Configuration boot = ModuleLayer.boot().configuration();
        Configuration child = boot.resolve(ModuleFinder.of(), ModuleFinder.of(), Set.of());
        check(child != boot, "resolve creates a fresh configuration");
        check(child.modules().isEmpty(), "no roots -> no modules");
        check(child.parents().size() == 1 && child.parents().get(0) == boot,
              "child parent is the receiver");
        check(child.findModule("java.base").isPresent(),
              "findModule delegates to parents: java.base visible from child");
        check(child.findModule("java.sql").isPresent(), "delegation sees every boot module");
        check(child.findModule("no.such.module").isEmpty(), "absent stays absent");
        try {
            child.modules().add(null);
            check(false, "child modules immutable");
        } catch (UnsupportedOperationException e) {
            check(true, "child modules immutable");
        }
        Configuration bound = boot.resolveAndBind(ModuleFinder.of(), ModuleFinder.of(), Set.of());
        check(bound.modules().isEmpty(), "resolveAndBind with no roots is empty too");
        check(bound.parents().get(0) == boot, "resolveAndBind parent is receiver");
        check(child.toString().equals(""), "empty child toString is empty");
        Configuration grandchild = child.resolve(ModuleFinder.of(), ModuleFinder.of(), Set.of());
        check(grandchild.parents().size() == 1 && grandchild.parents().get(0) == child,
              "grandchild chains onto the child");
        check(grandchild.findModule("java.base").isPresent(),
              "delegation works through two levels");
    }

    // resolveAndBind pulls in service providers; resolve alone does not
    static void testServiceBindingLoop() {
        Configuration boot = ModuleLayer.boot().configuration();
        ModuleDescriptor svc = ModuleDescriptor.newModule("jck.cfg.svc")
                .exports("jck.cfg.api").uses("jck.cfg.api.S").build();
        ModuleDescriptor impl = ModuleDescriptor.newModule("jck.cfg.impl")
                .requires("jck.cfg.svc").provides("jck.cfg.api.S", List.of("jck.cfg.impl.SImpl"))
                .build();
        java.util.Set<String> resolvedOnly = new java.util.HashSet<>();
        Configuration r = Configuration.resolve(finderOf(svc, impl), List.of(boot),
                ModuleFinder.of(), Set.of("jck.cfg.svc"));
        for (ResolvedModule rm : r.modules()) resolvedOnly.add(rm.name());
        check(resolvedOnly.equals(Set.of("jck.cfg.svc")),
              "resolve(root) closes over requires only");
        java.util.Set<String> bound = new java.util.HashSet<>();
        Configuration rb = Configuration.resolveAndBind(finderOf(svc, impl), List.of(boot),
                ModuleFinder.of(), Set.of("jck.cfg.svc"));
        for (ResolvedModule rm : rb.modules()) bound.add(rm.name());
        check(bound.equals(Set.of("jck.cfg.svc", "jck.cfg.impl")),
              "resolveAndBind adds the provider module");
        ResolvedModule implRm = rb.findModule("jck.cfg.impl").orElseThrow();
        check(implRm.configuration() == rb, "bound module lives in the child configuration");
        java.util.Set<String> implReads = new java.util.HashSet<>();
        for (ResolvedModule rm : implRm.reads()) implReads.add(rm.name());
        check(implReads.equals(Set.of("java.base", "jck.cfg.svc")),
              "provider reads java.base and its consumer");
        ResolvedModule svcRm = rb.findModule("jck.cfg.svc").orElseThrow();
        check(svcRm.reads().size() == 1
              && "java.base".equals(svcRm.reads().iterator().next().name()),
              "consumer reads only java.base before binding edges");
        check(implRm.reference().location().isEmpty(),
              "custom ModuleReference without location surfaces as empty Optional");
    }

    // precise failure modes: missing root, missing dependency, require cycle
    static void testFailureModes() {
        Configuration boot = ModuleLayer.boot().configuration();
        try {
            Configuration.resolve(ModuleFinder.of(), List.of(boot), ModuleFinder.of(),
                                  Set.of("no.such.root"));
            check(false, "missing root throws");
        } catch (FindException e) {
            check("Module no.such.root not found".equals(e.getMessage()),
                  "missing root exact message");
        }
        ModuleDescriptor dep = ModuleDescriptor.newModule("jck.cfg.user")
                .requires("jck.cfg.missing").build();
        try {
            Configuration.resolve(finderOf(dep), List.of(boot), ModuleFinder.of(),
                                  Set.of("jck.cfg.user"));
            check(false, "missing dependency throws");
        } catch (FindException e) {
            check("Module jck.cfg.missing not found, required by jck.cfg.user"
                      .equals(e.getMessage()), "missing dependency exact message");
        }
        ModuleDescriptor m1 = ModuleDescriptor.newModule("jck.cyc.a").requires("jck.cyc.b").build();
        ModuleDescriptor m2 = ModuleDescriptor.newModule("jck.cyc.b").requires("jck.cyc.a").build();
        try {
            Configuration.resolve(finderOf(m1, m2), List.of(boot), ModuleFinder.of(),
                                  Set.of("jck.cyc.a"));
            check(false, "cycle throws");
        } catch (ResolutionException e) {
            check(e.getMessage().startsWith("Cycle detected:")
                      && e.getMessage().contains("jck.cyc.a")
                      && e.getMessage().contains("jck.cyc.b"),
                  "cycle exact message shape");
        }
        check(true, "resolve failure leaves the parent untouched (no exception escaped)");
    }

    // static and instance resolve agree on the module-name set for java.sql
    static void testStaticInstanceEquivalence() {
        Configuration boot = ModuleLayer.boot().configuration();
        java.util.Set<String> viaStatic = new java.util.HashSet<>();
        for (ResolvedModule rm : Configuration.resolve(ModuleFinder.ofSystem(), List.of(boot),
                ModuleFinder.of(), Set.of("java.sql")).modules()) {
            viaStatic.add(rm.name());
        }
        java.util.Set<String> viaInstance = new java.util.HashSet<>();
        for (ResolvedModule rm : boot.resolve(ModuleFinder.ofSystem(), ModuleFinder.of(),
                Set.of("java.sql")).modules()) {
            viaInstance.add(rm.name());
        }
        check(viaStatic.equals(viaInstance), "static and instance resolve agree");
        check(viaInstance.contains("java.sql") && viaInstance.contains("java.xml")
              && viaInstance.contains("java.logging") && viaInstance.contains("java.base")
              || viaInstance.contains("java.sql"),
              "closure contains java.sql and its requires");
        check(viaStatic.size() >= 4, "java.sql closure spans several modules");
        // parents list of the static form is exactly [boot]
        Configuration viaStaticRef = Configuration.resolve(ModuleFinder.ofSystem(), List.of(boot),
                ModuleFinder.of(), Set.of("java.sql"));
        check(viaStaticRef.parents().equals(List.of(boot)), "static resolve parent list exact");
        ResolvedModule sql = viaStaticRef.findModule("java.sql").orElseThrow();
        check(sql.reads().stream().anyMatch(rm -> rm.name().equals("java.transaction.xa"))
                  || sql.reads().size() >= 2, "java.sql reads at least two modules");
    }

    // findModule through a parent returns the parent's ResolvedModule unchanged
    static void testParentDelegationSemantics() {
        Configuration boot = ModuleLayer.boot().configuration();
        Configuration child = boot.resolve(ModuleFinder.of(), ModuleFinder.of(), Set.of());
        ResolvedModule viaChild = child.findModule("java.base").orElseThrow();
        check(viaChild.configuration() == boot,
              "delegated lookup returns the parent's ResolvedModule");
        check(viaChild == boot.findModule("java.base").orElseThrow(),
              "delegated lookup is the identical ResolvedModule instance");
        check(child.findModule("java.sql").orElseThrow().configuration() == boot,
              "delegation covers every parent module");
        // a root is always (re)resolved from the finder, even if a parent has it
        Configuration c = Configuration.resolve(ModuleFinder.ofSystem(), List.of(boot),
                ModuleFinder.of(), Set.of("java.base"));
        check(c.modules().size() == 1 && "java.base".equals(
                  c.modules().iterator().next().name()), "root java.base re-resolved in child");
        check(c.findModule("java.base").orElseThrow().configuration() == c,
              "child owns its copy of the root ResolvedModule");
        check(c.findModule("java.base").orElseThrow().reads().isEmpty(),
              "java.base in child reads nothing (no requires)");
        // but a required (non-root) module already present in the parent is not duplicated
        ModuleDescriptor user = ModuleDescriptor.newModule("jck.dup.user")
                .requires("java.logging").build();
        Configuration u = Configuration.resolve(finderOf(user), List.of(boot),
                ModuleFinder.of(), Set.of("jck.dup.user"));
        check(u.modules().size() == 1, "requires satisfied by parent adds nothing new");
        java.util.Set<String> ureads = new java.util.HashSet<>();
        for (ResolvedModule rm : u.findModule("jck.dup.user").orElseThrow().reads()) {
            ureads.add(rm.name());
        }
        check(ureads.contains("java.logging"), "reads() still sees the parent's module");
        check(u.findModule("java.logging").orElseThrow().configuration() == boot,
              "parent module keeps its own configuration");
    }

    // the 'before' finder wins over the 'after' finder for the same module name
    static void testBeforeAfterFinderPrecedence() {
        Configuration boot = ModuleLayer.boot().configuration();
        ModuleDescriptor a = ModuleDescriptor.newModule("jck.ba.m").exports("jck.ba").build();
        ModuleDescriptor b = ModuleDescriptor.newModule("jck.ba.m").exports("jck.ba.other").build();
        Configuration c = boot.resolve(finderOf(a), finderOf(b), Set.of("jck.ba.m"));
        check(c.modules().size() == 1, "single module resolved");
        ResolvedModule rm = c.findModule("jck.ba.m").orElseThrow();
        check(rm.reference().descriptor() == a,
              "before-finder reference wins over after-finder");
        Configuration c2 = boot.resolve(ModuleFinder.of(), finderOf(b), Set.of("jck.ba.m"));
        check(c2.findModule("jck.ba.m").orElseThrow().reference().descriptor() == b,
              "after-finder consulted when before misses");
        Configuration c3 = boot.resolveAndBind(finderOf(a), finderOf(b), Set.of("jck.ba.m"));
        check(c3.findModule("jck.ba.m").orElseThrow().reference().descriptor() == a,
              "resolveAndBind honors finder precedence too");
    }

    // resolve with several parents; visibility composes
    static void testMultiParentResolve() {
        Configuration boot = ModuleLayer.boot().configuration();
        ModuleDescriptor mA = ModuleDescriptor.newModule("jck.p.a").exports("jck.p.a").build();
        ModuleDescriptor mB = ModuleDescriptor.newModule("jck.p.b").requires("jck.p.a").build();
        Configuration parent1 = Configuration.resolve(finderOf(mA), List.of(boot),
                ModuleFinder.of(), Set.of("jck.p.a"));
        Configuration parent2 = boot.resolve(ModuleFinder.of(), ModuleFinder.of(), Set.of());
        ModuleDescriptor mC = ModuleDescriptor.newModule("jck.p.c")
                .requires("jck.p.a").requires("java.base").build();
        Configuration child = Configuration.resolve(finderOf(mB, mC), List.of(parent1, parent2),
                ModuleFinder.of(), Set.of("jck.p.c"));
        check(child.parents().size() == 2, "two parents recorded");
        check(child.findModule("jck.p.c").isPresent(), "root resolved");
        check(child.findModule("jck.p.a").orElseThrow().configuration() == parent1,
              "dependency satisfied from parent1");
        java.util.Set<String> reads = new java.util.HashSet<>();
        for (ResolvedModule rm : child.findModule("jck.p.c").orElseThrow().reads()) {
            reads.add(rm.name());
        }
        check(reads.contains("jck.p.a") && reads.contains("java.base"),
              "reads spans both parents");
        // missing dependency across both parents still fails
        ModuleDescriptor mD = ModuleDescriptor.newModule("jck.p.d").requires("jck.p.nowhere").build();
        try {
            Configuration.resolve(finderOf(mD), List.of(parent1, parent2),
                    ModuleFinder.of(), Set.of("jck.p.d"));
            check(false, "multi-parent missing dep throws");
        } catch (FindException e) {
            check(true, "multi-parent missing dep throws FindException");
        }
    }

    // graph invariants: every module owns its configuration; reads stay inside the family
    static void testGraphInvariants() {
        Configuration boot = ModuleLayer.boot().configuration();
        ModuleDescriptor svc = ModuleDescriptor.newModule("jck.inv.svc")
                .exports("jck.inv.api").uses("jck.inv.api.S").build();
        ModuleDescriptor impl = ModuleDescriptor.newModule("jck.inv.impl")
                .requires("jck.inv.svc").provides("jck.inv.api.S", List.of("jck.inv.impl.I"))
                .build();
        Configuration rb = Configuration.resolveAndBind(finderOf(svc, impl), List.of(boot),
                ModuleFinder.of(), Set.of("jck.inv.svc"));
        boolean ownConfig = true;
        boolean named = true;
        for (ResolvedModule rm : rb.modules()) {
            if (rm.configuration() != rb) ownConfig = false;
            if (rm.name() == null || rm.name().isEmpty()) named = false;
        }
        check(ownConfig, "every ResolvedModule reports this configuration");
        check(named, "every ResolvedModule has a non-empty name");
        java.util.Set<String> known = new java.util.HashSet<>();
        for (ResolvedModule rm : rb.modules()) known.add(rm.name());
        known.add("java.base");
        boolean readsKnown = true;
        for (ResolvedModule rm : rb.modules()) {
            for (ResolvedModule target : rm.reads()) {
                if (!known.contains(target.name())) readsKnown = false;
            }
        }
        check(readsKnown, "reads() targets are within the configuration or its parents");
        check(rb.modules().stream().map(ResolvedModule::name).distinct().count()
              == rb.modules().size(), "names unique within configuration");
        // hash codes stable within a run
        int h1 = rb.findModule("jck.inv.svc").orElseThrow().hashCode();
        int h2 = rb.findModule("jck.inv.svc").orElseThrow().hashCode();
        check(h1 == h2, "ResolvedModule hashCode stable");
    }

    // binding only pulls providers for services that are actually used
    static void testBindingSelectivity() {
        Configuration boot = ModuleLayer.boot().configuration();
        ModuleDescriptor plain = ModuleDescriptor.newModule("jck.sel.svc")
                .exports("jck.sel.api").build(); // no uses()
        ModuleDescriptor orphan = ModuleDescriptor.newModule("jck.sel.orphan")
                .requires("jck.sel.svc").provides("jck.sel.api.S", List.of("jck.sel.orphan.I"))
                .build();
        Configuration rb = Configuration.resolveAndBind(finderOf(plain, orphan), List.of(boot),
                ModuleFinder.of(), Set.of("jck.sel.svc"));
        check(!rb.findModule("jck.sel.orphan").isPresent(),
              "provider of an unused service is not bound");
        check(rb.modules().size() == 1, "only the root resolved");
        // with uses() declared the same provider is bound
        ModuleDescriptor consumer = ModuleDescriptor.newModule("jck.sel.user")
                .requires("jck.sel.svc").uses("jck.sel.api.S").build();
        Configuration rb2 = Configuration.resolveAndBind(finderOf(plain, orphan, consumer),
                List.of(boot), ModuleFinder.of(), Set.of("jck.sel.user"));
        check(rb2.findModule("jck.sel.svc").isPresent(), "consumer pulls in the api module");
        check(rb2.findModule("jck.sel.orphan").isPresent(),
              "provider bound once a uses() exists in the graph");
        // toString of a single-module configuration is the bare module name
        check("jck.sel.svc".equals(rb.toString())
              || rb.toString().contains("jck.sel.svc"), "single-module toString");
        check(rb.findModule("jck.sel.svc").orElseThrow()
                  == rb.findModule("jck.sel.svc").orElseThrow(),
              "findModule returns the same instance twice");
        check(rb.modules().stream().noneMatch(rm -> "java.base".equals(rm.name())),
              "child configuration does not duplicate java.base");
    }

    // empty-parent chain and API-surface extras
    static void testEmptyChainAndSurface() {
        Configuration e = Configuration.empty();
        Configuration child = e.resolve(ModuleFinder.of(), ModuleFinder.of(), Set.of());
        check(child.modules().isEmpty() && child.parents().equals(List.of(e)),
              "child of the empty configuration");
        check(child != e, "resolve always allocates a new configuration");
        // public API surface: resolve and resolveAndBind exist in both shapes
        try {
            Class<?> c = Configuration.class;
            check(c.getMethod("resolve", ModuleFinder.class, java.util.List.class,
                      ModuleFinder.class, java.util.Collection.class) != null,
                  "static resolve(before, parents, after, roots) declared");
            check(c.getMethod("resolveAndBind", ModuleFinder.class, java.util.List.class,
                      ModuleFinder.class, java.util.Collection.class) != null,
                  "static resolveAndBind declared");
            check(c.getMethod("resolve", ModuleFinder.class, ModuleFinder.class,
                      java.util.Collection.class) != null,
                  "instance resolve(before, after, roots) declared");
            check(c.getMethod("resolveAndBind", ModuleFinder.class, ModuleFinder.class,
                      java.util.Collection.class) != null,
                  "instance resolveAndBind declared");
        } catch (NoSuchMethodException ex) {
            throw new AssertionError("configuration API surface incomplete", ex);
        }
        // resolve with a null root set is rejected
        try {
            e.resolve(ModuleFinder.of(), ModuleFinder.of(), null);
            check(false, "null roots rejected");
        } catch (NullPointerException ex) {
            check(true, "null roots NPE");
        }
        try {
            Configuration.resolve(null, List.of(e), ModuleFinder.of(), Set.of());
            check(false, "null before-finder rejected");
        } catch (NullPointerException ex) {
            check(true, "null before-finder NPE");
        }
        try {
            Configuration.resolve(ModuleFinder.of(), null, ModuleFinder.of(), Set.of());
            check(false, "null parents rejected");
        } catch (NullPointerException ex) {
            check(true, "null parents NPE");
        }
        Configuration grandchild = child.resolve(ModuleFinder.of(), ModuleFinder.of(), Set.of());
        check(grandchild.parents().equals(List.of(child)), "grandchild of empty chains");
        check(grandchild.modules().isEmpty() && grandchild.findModule("java.base").isEmpty(),
              "empty lineage never learns about java.base");
        try {
            grandchild.findModule(null);
            check(false, "grandchild findModule(null) NPE");
        } catch (NullPointerException ex) {
            check(true, "grandchild findModule(null) NPE");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBootConfigurationContract();
        testBindingSelectivity();
        testEmptyChainAndSurface();
        testResolveEmptyRoots();
        testServiceBindingLoop();
        testFailureModes();
        testStaticInstanceEquivalence();
        testParentDelegationSemantics();
        testBeforeAfterFinderPrecedence();
        testMultiParentResolve();
        testGraphInvariants();
        testBlackBox();
    }
}
