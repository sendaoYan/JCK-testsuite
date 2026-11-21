/*
 * @test
 * @summary JVMS 3.16: a module declaration compiles to a class file whose
 *          Module attribute records the module name, requires, exports, opens,
 *          uses and provides clauses (round-tripped via ModuleDescriptor).
 * @run main ModuleAttributeTest
 */

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class ModuleAttributeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("jvms316");
        Path src = dir.resolve("src");
        Path out = dir.resolve("out");
        Files.createDirectories(src.resolve("api"));
        Files.createDirectories(out);

        Files.writeString(src.resolve("module-info.java"),
            "module m.jvms.modules {\n" +
            "    requires java.base;\n" +
            "    exports api;\n" +
            "    opens api;\n" +
            "    uses java.util.spi.ToolProvider;\n" +
            "    provides java.util.spi.ToolProvider with api.TP;\n" +
            "}\n");
        Files.writeString(src.resolve("api").resolve("TP.java"),
            "package api;\n" +
            "import java.io.PrintWriter;\n" +
            "public class TP implements java.util.spi.ToolProvider {\n" +
            "    public String name() { return \"tp\"; }\n" +
            "    public int run(PrintWriter out, PrintWriter err, String... args) { return 0; }\n" +
            "}\n");

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new AssertionError("no system Java compiler available (java.compiler not resolved)");
        }
        int rc = compiler.run(null, null, null,
            "-d", out.toString(),
            src.resolve("module-info.java").toString(),
            src.resolve("api").resolve("TP.java").toString());
        check(rc == 0, "module compiles (rc=" + rc + ")");

        // JVMS 3.16: the Module attribute is the class-file encoding of the
        // module declaration. Read it back and verify every clause survived.
        ModuleFinder finder = ModuleFinder.of(out);
        ModuleReference ref = finder.find("m.jvms.modules")
            .orElseThrow(() -> new AssertionError("compiled module-info.class not found"));
        ModuleDescriptor md = ref.descriptor();

        check(md.name().equals("m.jvms.modules"), "module name recorded");
        check(md.requires().stream().anyMatch(r -> r.name().equals("java.base")), "requires java.base");
        check(md.exports().stream().anyMatch(e -> e.source().equals("api")), "exports api");
        check(md.opens().stream().anyMatch(o -> o.source().equals("api")), "opens api");
        check(md.uses().contains("java.util.spi.ToolProvider"), "uses ToolProvider");
        check(md.provides().stream().anyMatch(p -> p.service().equals("java.util.spi.ToolProvider")
                && p.providers().contains("api.TP")), "provides ToolProvider with api.TP");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
