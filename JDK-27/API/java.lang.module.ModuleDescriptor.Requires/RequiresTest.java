/*
 * @test
 * @summary java.lang.module.ModuleDescriptor.Requires conformance: name(), modifiers(), compiledVersion(), rawCompiledVersion(), equality.
 * @run main RequiresTest
 */

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Requires;
import java.lang.module.ModuleDescriptor.Requires.Modifier;
import java.lang.module.ModuleDescriptor.Version;
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

    public static void main(String[] args) {
        Requires r = withVersion();
        check("java.base".equals(r.name()), "name()");
        check(r.modifiers().equals(Set.of(Modifier.TRANSITIVE, Modifier.STATIC)), "modifiers()");
        check("1.2.3".equals(r.compiledVersion().orElseThrow().toString()), "compiledVersion()");
        check("1.2.3".equals(r.rawCompiledVersion().orElseThrow()), "rawCompiledVersion()");

        Requires plain = ModuleDescriptor.newModule("n").requires("dep").build().requires().stream()
                .filter(x -> "dep".equals(x.name())).findFirst().orElseThrow();
        check("dep".equals(plain.name()), "plain name()");
        check(plain.modifiers().isEmpty(), "plain modifiers empty");
        check(plain.compiledVersion().isEmpty(), "plain compiledVersion empty");
        check(plain.rawCompiledVersion().isEmpty(), "plain rawCompiledVersion empty");

        Requires r2 = withVersion();
        check(r.equals(r2), "equals()");
        check(r.hashCode() == r2.hashCode(), "hashCode()");
        check(r.compareTo(r2) == 0, "compareTo() equal");
        check(!r.equals(plain), "!equals different requires");
        check(r.toString() != null, "toString() non-null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
