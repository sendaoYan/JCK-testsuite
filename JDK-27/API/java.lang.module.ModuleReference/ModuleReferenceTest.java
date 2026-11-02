/*
 * @test
 * @summary java.lang.module.ModuleReference conformance: descriptor(), location() and open().
 * @run main ModuleReferenceTest
 */

import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;

public class ModuleReferenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        ModuleReference ref = ModuleFinder.ofSystem().find("java.base").orElseThrow();

        check("java.base".equals(ref.descriptor().name()), "descriptor().name() == java.base");
        check(ref.location().isPresent(), "location() present");
        check(ref.location().get().toString().contains("java.base"), "location() identifies java.base");

        try (ModuleReader reader = ref.open()) {
            check(reader != null, "open() returns ModuleReader");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
