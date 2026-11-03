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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBoot();
        testEmpty();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
