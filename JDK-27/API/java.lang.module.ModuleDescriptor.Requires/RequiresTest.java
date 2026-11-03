/*
 * @test
 * @summary java.lang.module.ModuleDescriptor.Requires conformance: white-box structure + black-box behavior.
 * @run main RequiresTest
 */

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Requires;
import java.lang.module.ModuleDescriptor.Requires.Modifier;
import java.lang.module.ModuleDescriptor.Version;
import java.lang.reflect.Method;
import java.util.Set;

public class RequiresTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Requires withVersion() {
        return ModuleDescriptor.newModule("m")
                .requires(Set.of(Modifier.TRANSITIVE, Modifier.STATIC),
                          "java.base", Version.parse("1.2.3"))
                .build().requires().iterator().next();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.module.ModuleDescriptor$Requires");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Requires is final");
        check(Comparable.class.isAssignableFrom(c), "implements Comparable");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor");
        String[] getters = {"name", "modifiers", "accessFlags", "compiledVersion",
                            "rawCompiledVersion", "compareTo", "equals", "hashCode", "toString"};
        for (String g : getters) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(g)) found = true;
            }
            check(found, "method " + g);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Requires r = withVersion();
        check("java.base".equals(r.name()), "name()");
        check(r.modifiers().equals(Set.of(Modifier.TRANSITIVE, Modifier.STATIC)), "modifiers()");
        check(r.accessFlags() != null && !r.accessFlags().isEmpty(), "accessFlags non-empty");
        check("1.2.3".equals(r.compiledVersion().orElseThrow().toString()), "compiledVersion()");
        check("1.2.3".equals(r.rawCompiledVersion().orElseThrow()), "rawCompiledVersion()");
        check(r.toString().contains("java.base"), "toString mentions name: " + r);
        check(r.toString().contains("transitive"), "toString contains modifiers");

        // plain requires without modifiers or version
        Requires plain = ModuleDescriptor.newModule("n").requires("dep").build().requires().stream()
                .filter(x -> "dep".equals(x.name())).findFirst().orElseThrow();
        check(plain.modifiers().isEmpty(), "plain modifiers empty");
        check(plain.compiledVersion().isEmpty(), "plain compiledVersion empty");
        check(plain.rawCompiledVersion().isEmpty(), "plain rawCompiledVersion empty");
        check(plain.toString().equals("dep"), "plain toString is just the name: " + plain);

        // requires with a Version object (string variant is package-private)
        Requires raw = ModuleDescriptor.newModule("n2")
                .requires(Set.of(Modifier.SYNTHETIC), "other", Version.parse("9.1")).build().requires().stream()
                .filter(x -> "other".equals(x.name())).findFirst().orElseThrow();
        check("9.1".equals(raw.rawCompiledVersion().orElseThrow()), "raw string version");
        check(raw.modifiers().equals(Set.of(Modifier.SYNTHETIC)), "synthetic modifier");

        // value semantics
        Requires r2 = withVersion();
        check(r.equals(r2), "equals()");
        check(r.hashCode() == r2.hashCode(), "hashCode()");
        check(r.compareTo(r2) == 0, "compareTo() equal");
        check(!r.equals(plain), "!equals different requires");
        check(plain.compareTo(r) < 0, "compareTo orders by module name (dep < java.base)");
        check(r.compareTo(plain) > 0, "reverse order");

        // same name, different modifiers -> not equal
        Requires sameNamePlain = ModuleDescriptor.newModule("n3")
                .requires("java.base").build().requires().stream()
                .filter(x -> "java.base".equals(x.name())).findFirst().orElseThrow();
        check(!r.equals(sameNamePlain), "modifiers participate in equals");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
