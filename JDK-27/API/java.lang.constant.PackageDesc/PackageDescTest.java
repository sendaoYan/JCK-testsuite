/*
 * @test
 * @summary java.lang.constant.PackageDesc conformance: of(), ofInternalName(),
 *          internalName(), name(), equals().
 * @run main PackageDescTest
 */

import java.lang.constant.PackageDesc;

public class PackageDescTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(PackageDesc.class.isInterface(), "PackageDesc is an interface");

        PackageDesc lang = PackageDesc.of("java.lang");
        check("java.lang".equals(lang.name()), "name()");
        check("java/lang".equals(lang.internalName()), "internalName()");

        PackageDesc internal = PackageDesc.ofInternalName("java/util");
        check("java.util".equals(internal.name()), "ofInternalName name()");
        check("java/util".equals(internal.internalName()), "ofInternalName internalName()");

        check(lang.equals(PackageDesc.of("java.lang")), "equals()");
        check(!lang.equals(PackageDesc.of("java.util")), "!equals different package");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
