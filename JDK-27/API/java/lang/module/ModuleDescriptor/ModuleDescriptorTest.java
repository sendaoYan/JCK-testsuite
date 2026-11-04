/*
 * @test
 * @summary java.lang.module.ModuleDescriptor conformance: white-box structure + black-box behavior.
 * @run main ModuleDescriptorTest
 */

import java.io.InputStream;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.lang.reflect.Modifier;
import java.util.Set;

public class ModuleDescriptorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.module.ModuleDescriptor");
        check(Modifier.isFinal(c.getModifiers()), "ModuleDescriptor is final");
        check(Comparable.class.isAssignableFrom(c), "implements Comparable");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor: builders/read only");
        String[] factories = {"newModule", "newOpenModule", "newAutomaticModule", "read"};
        for (String f : factories) {
            boolean found = false;
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(f) && Modifier.isStatic(m.getModifiers())) found = true;
            }
            check(found, "static factory " + f + " present");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // read(InputStream) of a real module's module-info.class
        ModuleReference ref = ModuleFinder.ofSystem().find("java.base").orElseThrow();
        ModuleDescriptor base;
        try (ModuleReader reader = ref.open()) {
            try (InputStream in = reader.open("module-info.class").orElseThrow()) {
                base = ModuleDescriptor.read(in);
            }
        }
        check("java.base".equals(base.name()), "read(InputStream).name() == java.base");
        check(!base.isAutomatic(), "java.base not automatic");
        check(!base.isOpen(), "java.base not open");
        check(base.mainClass().isEmpty(), "java.base has no main class");
        check(base.requires().isEmpty(), "java.base requires nothing (it is the root)");
        check(base.exports().stream().anyMatch(e -> e.source().equals("java.lang")),
              "java.base exports java.lang");
        check(base.packages().contains("java.lang"), "java.base packages contains java.lang");
        check(base.modifiers() != null, "modifiers() non-null set");

        // build a full-featured descriptor and read every getter back
        ModuleDescriptor full = ModuleDescriptor.newModule("com.example.full")
                .requires(Set.of(ModuleDescriptor.Requires.Modifier.TRANSITIVE), "java.base")
                .requires(Set.of(ModuleDescriptor.Requires.Modifier.STATIC), "java.compiler")
                .exports("com.example.full.api", Set.of("com.example.client"))
                .exports("com.example.full.spi")
                .opens("com.example.full.impl", Set.of("com.example.framework"))
                .uses("com.example.full.api.Service")
                .provides("com.example.full.api.Service",
                          java.util.List.of("com.example.full.spi.Impl1", "com.example.full.spi.Impl2"))
                .packages(Set.of("com.example.full.api", "com.example.full.spi"))
                .mainClass("com.example.full.Main")
                .version(ModuleDescriptor.Version.parse("2.1.0"))
                .build();

        check("com.example.full".equals(full.name()), "name");
        check(!full.isOpen(), "not open");
        check(!full.isAutomatic(), "not automatic");
        check(full.requires().size() == 2, "two requires (java.base implicit overridden)");
        ModuleDescriptor.Requires req = full.requires().stream()
                .filter(r -> r.name().equals("java.compiler")).findFirst().orElseThrow();
        check(req.modifiers().equals(Set.of(ModuleDescriptor.Requires.Modifier.STATIC)),
              "STATIC modifier read back");
        check("2.1.0".equals(full.version().orElseThrow().toString()), "version read back");
        check("2.1.0".equals(full.rawVersion().orElseThrow()), "rawVersion read back");
        check("com.example.full@2.1.0".equals(full.toNameAndVersion()), "toNameAndVersion");
        check("com.example.full.Main".equals(full.mainClass().orElseThrow()), "mainClass");
        ModuleDescriptor.Exports qualified = full.exports().stream()
                .filter(e -> e.source().equals("com.example.full.api")).findFirst().orElseThrow();
        check(qualified.isQualified(), "qualified export");
        check(qualified.targets().equals(Set.of("com.example.client")), "export targets");
        ModuleDescriptor.Opens open = full.opens().iterator().next();
        check(open.source().equals("com.example.full.impl"), "opens source");
        check(open.targets().equals(Set.of("com.example.framework")), "opens targets");
        check(full.uses().equals(Set.of("com.example.full.api.Service")), "uses");
        ModuleDescriptor.Provides provides = full.provides().iterator().next();
        check(provides.service().equals("com.example.full.api.Service"), "provides service");
        check(provides.providers().equals(java.util.List.of(
                "com.example.full.spi.Impl1", "com.example.full.spi.Impl2")), "providers order");
        check(full.packages().size() == 4, "packages = declared + opens source + mainClass package");
        check(full.packages().containsAll(Set.of(
                "com.example.full", "com.example.full.api", "com.example.full.spi",
                "com.example.full.impl")),
              "packages content");

        // newModule injects an implicit java.base requirement with MANDATED
        ModuleDescriptor m = ModuleDescriptor.newModule("m").build();
        check(m.requires().size() == 1, "implicit java.base require");
        ModuleDescriptor.Requires implicit = m.requires().iterator().next();
        check("java.base".equals(implicit.name()), "implicit require is java.base");
        check(implicit.modifiers().equals(Set.of(ModuleDescriptor.Requires.Modifier.MANDATED)),
              "implicit require is MANDATED");

        // open/automatic variants
        ModuleDescriptor om = ModuleDescriptor.newOpenModule("om").build();
        check(om.isOpen(), "newOpenModule isOpen");
        check(!om.isAutomatic(), "newOpenModule not automatic");
        ModuleDescriptor am = ModuleDescriptor.newAutomaticModule("am").build();
        check(am.isAutomatic(), "newAutomaticModule isAutomatic");
        check(am.requires().equals(Set.of()) == false
              && am.requires().size() == 1
              && "java.base".equals(am.requires().iterator().next().name())
              && am.requires().iterator().next().modifiers()
                      .equals(Set.of(ModuleDescriptor.Requires.Modifier.MANDATED)),
              "automatic module only has mandated java.base");
        check(am.modifiers().equals(Set.of(ModuleDescriptor.Modifier.AUTOMATIC)),
              "automatic modifier set");
        try {
            ModuleDescriptor.newAutomaticModule("am2").requires("java.logging");
            check(false, "automatic module cannot declare requires");
        } catch (IllegalStateException expected) {
            check(true, "automatic requires throws ISE");
        }

        // version(String) overload equals version(Version)
        ModuleDescriptor vs = ModuleDescriptor.newModule("vs").version("9.9").build();
        check("9.9".equals(vs.version().orElseThrow().toString()), "version(String)");

        // equals/hashCode/compareTo are name-based
        check(m.equals(ModuleDescriptor.newModule("m").build()), "equals same name");
        check(m.hashCode() == ModuleDescriptor.newModule("m").build().hashCode(), "hashCode");
        check(m.compareTo(ModuleDescriptor.newModule("m").build()) == 0, "compareTo equal");
        check(ModuleDescriptor.newModule("a").build()
                .compareTo(ModuleDescriptor.newModule("b").build()) < 0, "compareTo by name");
        check(!m.equals(om), "different names not equal");

        // read(ByteBuffer) variant reads the same descriptor
        ModuleDescriptor fromBuf;
        try (ModuleReader reader = ref.open()) {
            java.nio.ByteBuffer buf = reader.read("module-info.class").orElseThrow();
            fromBuf = ModuleDescriptor.read(buf);
            reader.release(buf);
        }
        check("java.base".equals(fromBuf.name()), "read(ByteBuffer).name()");
        check(fromBuf.equals(base), "both read paths equal");

        // null/blank name rejected (IllegalArgumentException per Checks.requireModuleName)
        try {
            ModuleDescriptor.newModule(null);
            check(false, "null name rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "null name IAE");
        }
        try {
            ModuleDescriptor.newModule("");
            check(false, "empty name rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "empty name IAE");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
