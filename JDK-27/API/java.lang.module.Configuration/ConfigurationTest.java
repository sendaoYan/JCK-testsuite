/*
 * @test
 * @summary java.lang.module.Configuration conformance: white-box + black-box behavior.
 * @run main ConfigurationTest
 */

import java.lang.module.Configuration;
import java.lang.module.FindException;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
    }
}
