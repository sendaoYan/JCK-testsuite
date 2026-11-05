/*
 * @test
 * @summary java.lang.module.ResolvedModule conformance: white-box + black-box behavior.
 * @run main ResolvedModuleTest
 */

import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.util.List;
import java.util.Set;

public class ResolvedModuleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.module.ResolvedModule");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor");
        for (String m : new String[]{"configuration", "reference", "name", "reads",
                                      "hashCode", "equals", "toString"}) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(m)) found = true;
            check(found, "declares " + m);
        }
        check(c.getDeclaredMethod("reads").getReturnType() == java.util.Set.class,
              "reads() returns Set");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Configuration boot = ModuleLayer.boot().configuration();
        ResolvedModule base = boot.findModule("java.base").orElseThrow();

        // per-module identity and accessors
        check("java.base".equals(base.name()), "name()");
        check(base.configuration() == boot, "configuration() == boot");
        check("java.base".equals(base.reference().descriptor().name()),
              "reference().descriptor().name()");
        check(base.reads().isEmpty(), "java.base reads nothing in the static graph");

        // java.sql in the boot configuration reads java.base and friends
        ResolvedModule sql = boot.findModule("java.sql").orElseThrow();
        check(sql.reads().stream().anyMatch(rm -> "java.base".equals(rm.name())),
              "java.sql reads java.base");
        check(sql.reads().stream().anyMatch(rm -> "java.xml".equals(rm.name())),
              "java.sql reads java.xml");
        check(sql.reads().stream().noneMatch(rm -> rm.name().equals("java.sql")),
              "no self-reads");
        for (ResolvedModule rm : sql.reads()) {
            check(rm.configuration() == boot, "reads() targets share the configuration");
        }

        // equals/hashCode: same config + same name -> equal; different config -> not equal
        ResolvedModule baseAgain = boot.findModule("java.base").orElseThrow();
        check(base.equals(baseAgain), "equals() same module");
        check(base.hashCode() == baseAgain.hashCode(), "hashCode consistent with equals");
        check(!base.equals(sql), "!equals different module in same config");
        check(!base.equals(null), "!equals null");
        check(!base.equals("java.base"), "!equals unrelated type");
        Configuration child = Configuration.resolve(ModuleFinder.ofSystem(), List.of(boot),
                ModuleFinder.of(), Set.of("java.sql"));
        ResolvedModule sqlChild = child.findModule("java.sql").orElseThrow();
        check(!sql.equals(sqlChild), "same name in another configuration is not equal");
        check(sql.name().equals(sqlChild.name()), "...but the names match");
        check(sqlChild.reads().stream().anyMatch(rm -> "java.base".equals(rm.name())),
              "child-graph java.sql still reads java.base");

        // toString: "<hash>/<name>" per specification (hash of the configuration)
        check(base.toString().endsWith("/java.base"), "toString format ends with /name");
        check(base.toString().equals(System.identityHashCode(boot) + "/java.base"),
              "toString is identityHashCode(configuration)/name()");
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

    // boot configuration ResolvedModules: reads graph, immutability, reference location
    static void testBootResolvedModules() {
        Configuration boot = ModuleLayer.boot().configuration();
        ResolvedModule base = boot.findModule("java.base").orElseThrow();
        check(base.reads().isEmpty(), "java.base reads nothing");
        try {
            base.reads().add(null);
            check(false, "reads() immutable");
        } catch (UnsupportedOperationException e) {
            check(true, "reads() immutable");
        }
        ResolvedModule logging = boot.findModule("java.logging").orElseThrow();
        java.util.Set<String> logReads = new java.util.HashSet<>();
        for (ResolvedModule rm : logging.reads()) logReads.add(rm.name());
        check(logReads.equals(java.util.Set.of("java.base")),
                "java.logging reads exactly java.base");
        check(logging.reference().location().isPresent()
                      && "jrt".equals(logging.reference().location().orElseThrow().getScheme()),
                "java.logging reference at a jrt URI");
        ResolvedModule sql = boot.findModule("java.sql").orElseThrow();
        java.util.Set<String> sqlReads = new java.util.HashSet<>();
        for (ResolvedModule rm : sql.reads()) sqlReads.add(rm.name());
        check(sqlReads.contains("java.base") && sqlReads.contains("java.xml")
                      && sqlReads.contains("java.logging") && sqlReads.contains("java.transaction.xa"),
                "java.sql reads base/xml/logging/xa");
        boolean allBoot = true;
        for (ResolvedModule rm : sql.reads()) {
            if (rm.configuration() != boot) allBoot = false;
        }
        check(allBoot, "all read targets share the boot configuration");
        check(base.name().equals(base.reference().descriptor().name()),
                "name() == reference().descriptor().name()");
        check(base.toString().equals(System.identityHashCode(boot) + "/java.base"),
                "toString is identityHashCode(configuration)/name");
        check(base.hashCode() == boot.findModule("java.base").orElseThrow().hashCode(),
                "hashCode consistent with equals");
        check(base.equals(boot.findModule("java.base").orElseThrow())
                      && !base.equals(sql), "equals within a configuration");
    }

    // custom resolution graph: reads edges follow requires exactly
    static void testCustomResolvedModules() {
        Configuration boot = ModuleLayer.boot().configuration();
        ModuleDescriptor a = ModuleDescriptor.newModule("jck.rm.a").exports("jck.rm.a").build();
        ModuleDescriptor b = ModuleDescriptor.newModule("jck.rm.b").requires("jck.rm.a").build();
        ModuleDescriptor c = ModuleDescriptor.newModule("jck.rm.c")
                .requires("jck.rm.b").build();
        ModuleFinder shared = finderOf(a, b, c);
        Configuration cfg = Configuration.resolve(shared, List.of(boot),
                ModuleFinder.of(), Set.of("jck.rm.c"));
        check(cfg.modules().size() == 3, "closure pulls in the whole requires chain");
        ResolvedModule rmA = cfg.findModule("jck.rm.a").orElseThrow();
        ResolvedModule rmB = cfg.findModule("jck.rm.b").orElseThrow();
        ResolvedModule rmC = cfg.findModule("jck.rm.c").orElseThrow();
        java.util.Set<String> aReads = new java.util.HashSet<>();
        java.util.Set<String> bReads = new java.util.HashSet<>();
        java.util.Set<String> cReads = new java.util.HashSet<>();
        for (ResolvedModule rm : rmA.reads()) aReads.add(rm.name());
        for (ResolvedModule rm : rmB.reads()) bReads.add(rm.name());
        for (ResolvedModule rm : rmC.reads()) cReads.add(rm.name());
        check(aReads.equals(java.util.Set.of("java.base")), "a reads exactly java.base");
        check(bReads.equals(java.util.Set.of("java.base", "jck.rm.a")),
                "b reads its direct require plus java.base");
        check(cReads.equals(java.util.Set.of("java.base", "jck.rm.b")),
                "c reads only its direct require (no transitive edges)");
        check(rmA.configuration() == cfg && rmB.configuration() == cfg && rmC.configuration() == cfg,
                "every module owns the configuration");
        check(rmA.reference().descriptor() == a, "reference is the finder's exact reference");
        check(rmA.reference().location().isEmpty(), "null location surfaces as empty Optional");
        check(!rmA.equals(rmB) && !rmB.equals(rmA), "distinct modules mutually unequal");
        check(rmA.hashCode() == cfg.findModule("jck.rm.a").orElseThrow().hashCode(),
                "hashCode consistent for repeated lookups");
        check(rmC.toString().endsWith("/jck.rm.c"), "toString ends with /name");
        // same module name resolved under two different configurations is never equal
        Configuration cfg2 = Configuration.resolve(shared, List.of(boot),
                ModuleFinder.of(), Set.of("jck.rm.a"));
        ResolvedModule rmA2 = cfg2.findModule("jck.rm.a").orElseThrow();
        check(!rmA.equals(rmA2), "same name in another configuration unequal");
        check(rmA.name().equals(rmA2.name()), "names still match");
        check(rmA.reference() == rmA2.reference(), "both wrap the identical ModuleReference");
        // after service binding the provider reads its consumer
        ModuleDescriptor svc = ModuleDescriptor.newModule("jck.rm.svc")
                .exports("jck.rm.api").uses("jck.rm.api.S").build();
        ModuleDescriptor impl = ModuleDescriptor.newModule("jck.rm.impl")
                .requires("jck.rm.svc").provides("jck.rm.api.S", List.of("jck.rm.impl.I"))
                .build();
        Configuration bound = Configuration.resolveAndBind(finderOf(svc, impl), List.of(boot),
                ModuleFinder.of(), Set.of("jck.rm.svc"));
        ResolvedModule provider = bound.findModule("jck.rm.impl").orElseThrow();
        java.util.Set<String> pReads = new java.util.HashSet<>();
        for (ResolvedModule rm : provider.reads()) pReads.add(rm.name());
        check(pReads.contains("jck.rm.svc"), "bound provider reads its consumer");
        check(provider.toString().equals(System.identityHashCode(bound) + "/jck.rm.impl"),
                "custom-config toString format");
        check(provider.reference() != null && provider.reference().descriptor() == impl,
                "provider reference is the finder's exact reference");
    }

    // equals/hashCode contract details
    static void testEqualsContract() {
        Configuration boot = ModuleLayer.boot().configuration();
        ResolvedModule base = boot.findModule("java.base").orElseThrow();
        ResolvedModule sql = boot.findModule("java.sql").orElseThrow();
        check(base.equals(base), "reflexive");
        check(!base.equals(null), "null unequal");
        check(!base.equals("java.base"), "foreign type unequal");
        check(!base.equals(sql) && !sql.equals(base), "symmetric inequality");
        check(base.hashCode() == base.hashCode(), "hashCode stable in-run");
        check(base.equals(boot.findModule("java.base").orElseThrow()), "repeat lookup equal");
        // configuration() is never null for real resolved modules
        check(base.configuration() != null && sql.configuration() != null,
                "configuration never null");
    }

    // ---- Part 4: white-box deepening ----
    static void testWhiteBoxDeepen() throws Exception {
        Class<?> c = ResolvedModule.class;
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getPackageName().equals("java.lang.module"), "package java.lang.module");
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            check(!java.lang.reflect.Modifier.isPublic(ct.getModifiers()),
                    "no public constructor");
        }
        check(c.getDeclaredMethod("reads").getReturnType() == java.util.Set.class,
                "reads() returns Set");
        check(c.getDeclaredMethod("name").getReturnType() == String.class,
                "name() returns String");
        check(c.getDeclaredMethod("reference").getReturnType() == ModuleReference.class,
                "reference() returns ModuleReference");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testWhiteBoxDeepen();
        testBlackBox();
        testBootResolvedModules();
        testCustomResolvedModules();
        testEqualsContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
