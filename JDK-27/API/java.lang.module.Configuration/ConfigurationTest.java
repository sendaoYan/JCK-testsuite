/*
 * @test
 * @summary java.lang.module.Configuration conformance: empty(), modules(), findModule(), parents(), resolve().
 * @run main ConfigurationTest
 */

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.module.ResolvedModule;
import java.util.List;
import java.util.Set;

public class ConfigurationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        Configuration empty = Configuration.empty();
        check(empty.modules().isEmpty(), "empty().modules() is empty");
        check(empty.parents().isEmpty(), "empty().parents() is empty");
        check(empty.toString() != null, "empty().toString() non-null");

        Configuration boot = ModuleLayer.boot().configuration();
        check(!boot.modules().isEmpty(), "boot configuration has modules");
        ResolvedModule base = boot.findModule("java.base").orElseThrow();
        check("java.base".equals(base.name()), "findModule(java.base).name()");

        // instance resolve() with empty roots and empty finders adds no modules
        Configuration c2 = boot.resolve(ModuleFinder.of(), ModuleFinder.of(), Set.of());
        check(c2.modules().isEmpty(), "resolve(empty roots) has no new modules");

        // static resolve() with the boot layer as parent resolves java.sql + deps
        Configuration c3 = Configuration.resolve(ModuleFinder.ofSystem(), List.of(boot),
                                                 ModuleFinder.of(), Set.of("java.sql"));
        check(!c3.modules().isEmpty(), "resolve(java.sql) non-empty");
        check(c3.findModule("java.sql").isPresent(), "java.sql resolved");
        check(c3.parents().size() == 1, "parents() size 1");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
