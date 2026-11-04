/*
 * @test
 * @summary java.lang.module.ModuleFinder conformance: white-box + black-box behavior.
 * @run main ModuleFinderTest
 */

import java.lang.module.ModuleFinder;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
