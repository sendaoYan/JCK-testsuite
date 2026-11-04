/*
 * @test
 * @summary java.lang.module.ModuleDescriptor.Builder conformance: white-box structure + black-box behavior.
 * @run main BuilderTest
 */

import java.lang.module.ModuleDescriptor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

public class BuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.module.ModuleDescriptor$Builder");
        check(Modifier.isFinal(c.getModifiers()), "Builder is final");
        check(c.getDeclaringClass() == ModuleDescriptor.class, "nested in ModuleDescriptor");
        check(c.getDeclaredMethod("build").getReturnType() == ModuleDescriptor.class,
              "build() returns ModuleDescriptor");
        String[] fluents = {"requires", "exports", "opens", "uses", "provides",
                            "packages", "version", "mainClass"};
        for (String f : fluents) {
            boolean found = false;
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(f)) found = true;
            }
            check(found, "fluent method " + f);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // full fluent chain: every overload returns the same builder for chaining
        ModuleDescriptor.Builder b = ModuleDescriptor.newModule("b");
        ModuleDescriptor.Builder same = b
                .requires("java.logging")
                .requires(Set.of(ModuleDescriptor.Requires.Modifier.TRANSITIVE), "java.sql")
                .exports("b.api")
                .exports(Set.of(ModuleDescriptor.Exports.Modifier.SYNTHETIC), "b.syn")
                .exports("b.q", Set.of("b.client"))
                .opens("b.impl")
                .opens("b.oq", Set.of("b.framework"))
                .uses("b.api.Service")
                .provides("b.api.Service", List.of("b.spi.Impl"))
                .packages(Set.of("b.api", "b.spi"))
                .mainClass("b.Main")
                .version("3.0");
        check(same == b, "fluent methods return this");
        ModuleDescriptor d = b.build();
        check(d.name().equals("b"), "build name");
        check(d.requires().size() == 3, "three requires (implicit + 2 explicit)");
        check(d.exports().size() == 3, "three exports");
        check(d.opens().size() == 2, "two opens");
        check(d.uses().size() == 1, "one use");
        check(d.provides().size() == 1, "one provides");
        check(d.version().orElseThrow().toString().equals("3.0"), "version(String)");

        // version(Version) overload
        ModuleDescriptor d2 = ModuleDescriptor.newModule("b2")
                .version(ModuleDescriptor.Version.parse("4.5")).build();
        check(d2.version().orElseThrow().toString().equals("4.5"), "version(Version)");

        // requires(Requires) object overload round-trip
        ModuleDescriptor.Requires r = ModuleDescriptor.newModule("x")
                .requires("java.xml").build().requires().stream()
                .filter(q -> q.name().equals("java.xml")).findFirst().orElseThrow();
        ModuleDescriptor d3 = ModuleDescriptor.newModule("b3").requires(r).build();
        check(d3.requires().contains(r), "requires(Requires) object");

        // exports(Exports) / opens(Opens) object overloads
        ModuleDescriptor.Exports e = ModuleDescriptor.newModule("y").exports("p.q").build()
                .exports().iterator().next();
        ModuleDescriptor d4 = ModuleDescriptor.newModule("b4").exports(e).build();
        check(d4.exports().contains(e), "exports(Exports) object");
        ModuleDescriptor.Opens o = ModuleDescriptor.newModule("y2").opens("p.r").build()
                .opens().iterator().next();
        ModuleDescriptor d5 = ModuleDescriptor.newModule("b5").opens(o).build();
        check(d5.opens().contains(o), "opens(Opens) object");
        ModuleDescriptor.Provides p = ModuleDescriptor.newModule("y3")
                .provides("s.S", List.of("s.I")).build().provides().iterator().next();
        ModuleDescriptor d6 = ModuleDescriptor.newModule("b6").provides(p).build();
        check(d6.provides().contains(p), "provides(Provides) object");

        // duplicate declaration rejected
        try {
            ModuleDescriptor.newModule("dup").requires("java.xml").requires("java.xml").build();
            check(false, "duplicate requires rejected");
        } catch (IllegalStateException expected) {
            check(true, "duplicate requires ISE");
        }
        try {
            ModuleDescriptor.newModule("dup").exports("a.b").exports("a.b").build();
            check(false, "duplicate exports rejected");
        } catch (IllegalStateException expected) {
            check(true, "duplicate exports ISE");
        }
        // open module cannot declare opens
        try {
            ModuleDescriptor.newOpenModule("om").opens("a.b");
            check(false, "open module cannot opens");
        } catch (IllegalStateException expected) {
            check(true, "open module opens ISE");
        }
        // bad package names rejected
        try {
            ModuleDescriptor.newModule("bad").exports("not a package");
            check(false, "bad package rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "bad package IAE");
        }
        // open module builder reports via final descriptor
        check(ModuleDescriptor.newOpenModule("om2").build().isOpen(), "open builder result");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
