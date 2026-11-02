/*
 * @test
 * @summary java.lang.constant.ModuleDesc conformance: of(), name(), equals().
 *          (In JDK 27 ModuleDesc no longer extends ConstantDesc.)
 * @run main ModuleDescTest
 */

import java.lang.constant.ModuleDesc;

public class ModuleDescTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(ModuleDesc.class.isInterface(), "ModuleDesc is an interface");

        ModuleDesc base = ModuleDesc.of("java.base");
        check("java.base".equals(base.name()), "name()");
        check(base.equals(ModuleDesc.of("java.base")), "equals()");
        check(!base.equals(ModuleDesc.of("java.sql")), "!equals different name");
        check(!base.equals(null), "!equals null");
        check(!base.equals("java.base"), "!equals non-ModuleDesc");

        check("java.sql".equals(ModuleDesc.of("java.sql").name()), "of(java.sql)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
