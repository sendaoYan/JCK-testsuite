/*
 * @test
 * @summary java.lang.module.ModuleDescriptor.Version conformance: parse(), compareTo(), equals(), hashCode(), toString() and rejection of invalid input.
 * @run main VersionTest
 */

import java.lang.module.ModuleDescriptor.Version;

public class VersionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectIllegalArgument(String version) {
        try {
            Version.parse(version);
            check(false, "expected IllegalArgumentException for " + version);
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    public static void main(String[] args) {
        Version v = Version.parse("1.2.3");
        check("1.2.3".equals(v.toString()), "toString()");
        check(v.equals(Version.parse("1.2.3")), "equals()");
        check(v.hashCode() == Version.parse("1.2.3").hashCode(), "hashCode()");

        check(Version.parse("1.2.3").compareTo(Version.parse("1.2.4")) < 0, "compareTo less");
        check(Version.parse("1.2.4").compareTo(Version.parse("1.2.3")) > 0, "compareTo greater");
        check(Version.parse("1.2.3").compareTo(Version.parse("1.2.3")) == 0, "compareTo equal");
        check(Version.parse("1.2.3.4").compareTo(Version.parse("1.2.3")) > 0, "longer version greater");
        check(Version.parse("1").compareTo(Version.parse("1.0")) == 0, "trailing zero equals");

        check(!v.equals("1.2.3"), "!equals String");

        expectIllegalArgument("");
        expectIllegalArgument("abc");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
