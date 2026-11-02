/*
 * @test
 * @summary java.lang.module.ModuleFinder conformance: ofSystem(), find(), findAll(), of(), compose().
 * @run main ModuleFinderTest
 */

import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.util.Set;

public class ModuleFinderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        ModuleFinder system = ModuleFinder.ofSystem();

        ModuleReference base = system.find("java.base").orElseThrow();
        check("java.base".equals(base.descriptor().name()), "find(java.base).descriptor().name()");
        check(system.find("no.such.module").isEmpty(), "find(absent) empty");

        Set<ModuleReference> all = system.findAll();
        check(!all.isEmpty(), "findAll() non-empty");
        check(all.stream().anyMatch(r -> "java.base".equals(r.descriptor().name())),
              "findAll() contains java.base");

        check(ModuleFinder.of().findAll().isEmpty(), "of() findAll empty");
        check(ModuleFinder.of().find("x").isEmpty(), "of() find empty");

        ModuleFinder composed = ModuleFinder.compose(ModuleFinder.ofSystem());
        check(composed.find("java.base").isPresent(), "compose(ofSystem) find java.base");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
