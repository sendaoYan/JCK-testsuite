/*
 * @test
 * @summary java.lang.module.ResolvedModule conformance: white-box + black-box behavior.
 * @run main ResolvedModuleTest
 */

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
