/*
 * @test
 * @summary java.lang.Runtime.Version conformance: white-box structure + black-box behavior.
 * @run main VersionTest
 */
import java.lang.Runtime.Version;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;

public class VersionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Version.class;
        check(Modifier.isFinal(c.getModifiers()), "Version is final");
        check(Comparable.class.isAssignableFrom(c), "implements Comparable<Version>");
        check(c.getConstructors().length == 0, "no public constructors (use parse)");
        Method parse = c.getMethod("parse", String.class);
        check(Modifier.isStatic(parse.getModifiers()) && Modifier.isPublic(parse.getModifiers()),
              "parse(String) is public static");
        boolean allPresent = true;
        for (String n : new String[]{"feature", "interim", "update", "patch",
                                     "major", "minor", "security", "version",
                                     "pre", "build", "optional", "hashCode", "toString"}) {
            boolean found = false;
            for (Method m : c.getMethods()) {
                if (m.getName().equals(n) && m.getParameterCount() == 0) found = true;
            }
            if (!found) { allPresent = false; System.err.println("missing no-arg method: " + n); }
        }
        check(allPresent, "all documented no-arg accessors present");
        check(c.getMethod("version").getReturnType() == List.class, "version() returns List<Integer>");
        check(c.getMethod("pre").getReturnType() == Optional.class, "pre() returns Optional<String>");
        check(c.getMethod("build").getReturnType() == Optional.class, "build() returns Optional<Integer>");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testNumberAccessors() {
        Version v = Version.parse("27.0.1");
        check(v.feature() == 27, "feature is the first number");
        check(v.interim() == 0, "interim is the second number");
        check(v.update() == 1, "update is the third number");
        check(v.patch() == 0, "patch defaults to 0");
        check(v.major() == v.feature(), "major() == feature() (legacy alias)");
        check(v.minor() == v.interim(), "minor() == interim() (legacy alias)");
        check(v.security() == v.update(), "security() == update() (legacy alias)");
        check(v.version().equals(List.of(27, 0, 1)), "version() is the number list");
        check(v.version().size() == 3, "version() size for 27.0.1");
        check(v.toString().equals("27.0.1"), "toString round-trips the input");
    }

    static void testOptionalParts() {
        Version v = Version.parse("27.0.1+5");
        check(v.pre().isEmpty(), "pre absent without a dash");
        check(v.build().equals(Optional.of(5)), "build is +5");
        Version ea = Version.parse("27.0.1-ea");
        check(ea.pre().equals(Optional.of("ea")), "pre is ea");
        check(ea.build().isEmpty(), "build absent without +");
        check(ea.toString().equals("27.0.1-ea"), "toString keeps the pre-release");
        check(Version.parse("27").version().equals(List.of(27)), "single-number version");
    }

    static void testParseErrors() {
        for (String bad : new String[]{"abc", "27.0.1++5", "27..1"}) {
            try {
                Version.parse(bad);
                throw new AssertionError("parse('" + bad + "'): expected IllegalArgumentException");
            } catch (IllegalArgumentException expected) { // NumberFormatException is a subclass
                passed++;
            }
        }
    }

    static void testComparison() {
        check(Version.parse("27").compareTo(Version.parse("27.0.1")) < 0, "shorter version sorts first");
        check(Version.parse("27.0.1").compareTo(Version.parse("27.0.1")) == 0, "equal compares 0");
        check(Version.parse("27.0.2").compareTo(Version.parse("27.0.1")) > 0, "higher update sorts later");
        check(Version.parse("9.0.1").compareTo(Version.parse("9.0.1-ea")) > 0,
              "release sorts after its pre-release");
        check(Version.parse("27.0.1+1").compareTo(Version.parse("27.0.1+2")) < 0,
              "build numbers compare numerically");
        check(Version.parse("27.0.1").compareTo(Version.parse("27.0.2")) < 0, "compareTo less");
        check(Version.parse("27.0.1-ea+1").compareToIgnoreOptional(Version.parse("27.0.1-ea+1")) == 0,
              "compareToIgnoreOptional equal for identical versions");
    }

    static void testEqualsHash() {
        Version a = Version.parse("27.0.1");
        Version b = Version.parse("27.0.1");
        check(a.equals(b) && b.equals(a), "equal versions are equal");
        check(a.hashCode() == b.hashCode(), "hashCode consistent with equals");
        check(!a.equals(Version.parse("27.0.2")), "different versions differ");
        check(!a.equals(null) && !a.equals("27.0.1"), "not equal to null or foreign type");
        check(a.equalsIgnoreOptional(a), "equalsIgnoreOptional reflexive");
        check(Runtime.version().feature() == Runtime.version().feature(),
              "Runtime.version() is a stable snapshot");
        check(Runtime.version().version().get(0) >= 27, "runtime is JDK 27+");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testNumberAccessors();
        testOptionalParts();
        testParseErrors();
        testComparison();
        testEqualsHash();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
