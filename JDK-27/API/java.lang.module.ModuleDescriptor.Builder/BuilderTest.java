/*
 * @test
 * @summary java.lang.module.ModuleDescriptor.Builder conformance: requires/exports/opens/uses/provides/packages/version/mainClass and build().
 * @run main BuilderTest
 */

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Builder;
import java.lang.module.ModuleDescriptor.Exports;
import java.lang.module.ModuleDescriptor.Opens;
import java.lang.module.ModuleDescriptor.Requires;
import java.lang.module.ModuleDescriptor.Version;
import java.util.List;
import java.util.Set;

public class BuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        Builder b = ModuleDescriptor.newModule("m");
        check(b != null, "newModule returns builder");
        check("m".equals(b.build().name()), "build().name()");

        ModuleDescriptor d = ModuleDescriptor.newModule("m")
                .requires(Set.of(Requires.Modifier.TRANSITIVE, Requires.Modifier.STATIC),
                          "java.base", Version.parse("1.2.3"))
                .exports(Set.of(Exports.Modifier.MANDATED, Exports.Modifier.SYNTHETIC),
                         "p", Set.of("target"))
                .opens(Set.of(Opens.Modifier.MANDATED), "q", Set.of("target2"))
                .uses("svc.Service")
                .provides("svc.Service", List.of("impl.A", "impl.B"))
                .packages(Set.of("p", "q", "internal"))
                .version("2.0")
                .mainClass("p.Main")
                .build();
        check("m".equals(d.name()), "build name");
        check(d.requires().size() == 1, "requires count");
        check(d.exports().size() == 1, "exports count");
        check(d.opens().size() == 1, "opens count");
        check(d.uses().equals(Set.of("svc.Service")), "uses()");
        check(d.provides().size() == 1, "provides count");
        check("2.0".equals(d.version().orElseThrow().toString()), "version()");
        check("p.Main".equals(d.mainClass().orElseThrow()), "mainClass()");
        check(d.packages().containsAll(Set.of("p", "q", "internal")), "packages()");
        check("m@2.0".equals(d.toNameAndVersion()), "toNameAndVersion()");

        // object round-trip: requires(Requires), exports(Exports), opens(Opens)
        ModuleDescriptor src = ModuleDescriptor.newModule("src").requires("dep").build();
        Requires req = src.requires().stream().filter(x -> "dep".equals(x.name())).findFirst().orElseThrow();
        ModuleDescriptor viaReq = ModuleDescriptor.newModule("via").requires(req).build();
        check(viaReq.requires().stream().anyMatch(x -> "dep".equals(x.name())), "requires(Requires) round-trip");

        ModuleDescriptor srcEx = ModuleDescriptor.newModule("srcEx").exports("pkg").build();
        Exports ex = srcEx.exports().iterator().next();
        ModuleDescriptor viaEx = ModuleDescriptor.newModule("viaEx").exports(ex).build();
        check("pkg".equals(viaEx.exports().iterator().next().source()), "exports(Exports) round-trip");

        ModuleDescriptor srcOp = ModuleDescriptor.newModule("srcOp").opens("opkg").build();
        Opens op = srcOp.opens().iterator().next();
        ModuleDescriptor viaOp = ModuleDescriptor.newModule("viaOp").opens(op).build();
        check("opkg".equals(viaOp.opens().iterator().next().source()), "opens(Opens) round-trip");

        check(ModuleDescriptor.newOpenModule("om").build().isOpen(), "newOpenModule build isOpen");
        check(ModuleDescriptor.newAutomaticModule("am").build().isAutomatic(), "newAutomaticModule build isAutomatic");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
