/*
 * @test
 * @summary java.lang.module.ModuleDescriptor.Version conformance: white-box + black-box behavior.
 * @run main VersionTest
 */

import java.lang.module.ModuleDescriptor.Version;
import java.lang.reflect.Method;

public class VersionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectIllegal(String version) {
        try {
            Version.parse(version);
            check(false, "expected IllegalArgumentException for " + version);
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.module.ModuleDescriptor$Version");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Version is final");
        check(Comparable.class.isAssignableFrom(c), "implements Comparable");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor");
        Method parse = c.getDeclaredMethod("parse", String.class);
        check(java.lang.reflect.Modifier.isStatic(parse.getModifiers()), "parse is static");
        check(parse.getReturnType() == Version.class, "parse returns Version");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // parse/toString round trip preserves the raw string
        for (String v : new String[]{"1.2.3", "1", "1.0.0-rc1+build.7", "27.0.1", "9-ea"}) {
            check(Version.parse(v).toString().equals(v), "round trip " + v);
        }

        // equals/hashCode by value
        Version v = Version.parse("1.2.3");
        check(v.equals(Version.parse("1.2.3")), "equals()");
        check(v.hashCode() == Version.parse("1.2.3").hashCode(), "hashCode()");
        check(!v.equals("1.2.3"), "!equals String");
        check(!v.equals(Version.parse("1.2.4")), "!equals different");

        // numeric comparison component-wise
        check(Version.parse("1.2.3").compareTo(Version.parse("1.2.4")) < 0, "1.2.3 < 1.2.4");
        check(Version.parse("1.2.4").compareTo(Version.parse("1.2.3")) > 0, "1.2.4 > 1.2.3");
        check(Version.parse("1.2.3").compareTo(Version.parse("1.2.3")) == 0, "equal");
        check(Version.parse("1.2.3.4").compareTo(Version.parse("1.2.3")) > 0, "longer greater");
        check(Version.parse("1.9").compareTo(Version.parse("1.10")) < 0, "numeric not lexical (9<10)");
        check(Version.parse("2.0.0").compareTo(Version.parse("10.0.0")) < 0, "2 < 10");
        check(Version.parse("1.2").compareTo(Version.parse("1.2.0")) == 0,
              "trailing zero equals");

        // JDK version scheme comparisons on real module versions
        check(Version.parse("27").compareTo(Version.parse("27.0.1")) < 0, "27 < 27.0.1");

        // invalid input rejected
        expectIllegal("");
        expectIllegal("abc");
        expectIllegal(null);
        expectIllegal("v1"); // must start with a number
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
