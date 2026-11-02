/*
 * @test
 * @summary java.lang.module.ModuleDescriptor conformance: read(InputStream), newModule/newOpenModule/newAutomaticModule, aggregate accessors, compareTo/equals.
 * @run main ModuleDescriptorTest
 */

import java.io.InputStream;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.util.Set;

public class ModuleDescriptorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
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
        check(base.modifiers() != null, "java.base modifiers non-null");

        ModuleDescriptor m = ModuleDescriptor.newModule("m").build();
        check("m".equals(m.name()), "newModule name");
        check(!m.isOpen(), "newModule not open");
        check(!m.isAutomatic(), "newModule not automatic");
        check(m.requires().size() == 1, "newModule has single implicit requirement");
        ModuleDescriptor.Requires implicit = m.requires().iterator().next();
        check("java.base".equals(implicit.name()), "implicit requirement is java.base");
        check(implicit.modifiers().equals(Set.of(ModuleDescriptor.Requires.Modifier.MANDATED)),
              "implicit requirement is MANDATED");
        check(implicit.compiledVersion().isEmpty(), "implicit requirement has no compiled version");

        ModuleDescriptor om = ModuleDescriptor.newOpenModule("om").build();
        check(om.isOpen(), "newOpenModule isOpen");
        check(!om.isAutomatic(), "newOpenModule not automatic");

        ModuleDescriptor am = ModuleDescriptor.newAutomaticModule("am").build();
        check(am.isAutomatic(), "newAutomaticModule isAutomatic");

        ModuleDescriptor vm = ModuleDescriptor.newModule("v")
                .version("1.0")
                .mainClass("v.Main")
                .packages(Set.of("v", "v.sub"))
                .build();
        check("1.0".equals(vm.version().orElseThrow().toString()), "version()");
        check("1.0".equals(vm.rawVersion().orElseThrow()), "rawVersion()");
        check("v@1.0".equals(vm.toNameAndVersion()), "toNameAndVersion()");
        check("v.Main".equals(vm.mainClass().orElseThrow()), "mainClass()");
        check(vm.packages().containsAll(Set.of("v", "v.sub")), "packages()");

        check(m.compareTo(ModuleDescriptor.newModule("m").build()) == 0, "compareTo equal");
        check(ModuleDescriptor.newModule("a").build()
                .compareTo(ModuleDescriptor.newModule("b").build()) < 0, "compareTo less by name");
        check(m.equals(ModuleDescriptor.newModule("m").build()), "equals same name");
        check(m.hashCode() == ModuleDescriptor.newModule("m").build().hashCode(), "hashCode same name");
        check(m.toString() != null, "toString non-null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
