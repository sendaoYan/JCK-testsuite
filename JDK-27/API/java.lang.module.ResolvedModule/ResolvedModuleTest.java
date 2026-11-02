/*
 * @test
 * @summary java.lang.module.ResolvedModule conformance: name(), configuration(), reference(), reads() and equality.
 * @run main ResolvedModuleTest
 */

import java.lang.module.Configuration;
import java.lang.module.ResolvedModule;

public class ResolvedModuleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        Configuration boot = ModuleLayer.boot().configuration();
        ResolvedModule base = boot.findModule("java.base").orElseThrow();

        check("java.base".equals(base.name()), "name()");
        check(base.configuration() == boot, "configuration() == boot");
        check("java.base".equals(base.reference().descriptor().name()), "reference().descriptor().name()");
        check(base.reads() != null, "reads() non-null");
        check(base.equals(boot.findModule("java.base").orElseThrow()), "equals() same module");
        check(base.hashCode() == boot.findModule("java.base").orElseThrow().hashCode(), "hashCode() same module");
        check(!base.equals(boot.findModule("java.sql").orElseThrow()), "!equals different module");
        check(base.toString() != null, "toString() non-null");

        ResolvedModule sql = boot.findModule("java.sql").orElseThrow();
        check(sql.reads().stream().anyMatch(r -> "java.base".equals(r.name())),
              "java.sql reads java.base");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
