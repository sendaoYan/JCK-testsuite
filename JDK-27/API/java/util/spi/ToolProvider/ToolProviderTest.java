/*
 * @test
 * @summary java.util.spi.ToolProvider conformance: white-box structure + black-box behavior.
 * @run main ToolProviderTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.spi.ToolProvider;

public class ToolProviderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static <T extends Throwable> T expectThrows(Class<T> type, Runnable r, String m) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return type.cast(t); }
            throw new AssertionError(m + ": expected " + type.getName() + " but got " + t.getClass().getName());
        }
        throw new AssertionError(m + ": expected " + type.getName() + " but nothing thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.spi.ToolProvider");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "public interface");

        check(c.getMethod("name").getReturnType() == String.class, "name() returns String");

        Method desc = c.getMethod("description");
        check(desc.getReturnType() == Optional.class && desc.isDefault(), "description() default Optional");

        Method runW = c.getMethod("run", PrintWriter.class, PrintWriter.class, String[].class);
        check(Modifier.isAbstract(runW.getModifiers()) && runW.getReturnType() == int.class,
              "run(PrintWriter...) abstract int");
        Method runS = c.getMethod("run", PrintStream.class, PrintStream.class, String[].class);
        check(runS.isDefault() && runS.getReturnType() == int.class, "run(PrintStream...) default int");

        check(c.getMethod("findFirst", String.class).getReturnType() == Optional.class,
              "findFirst static Optional");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ToolProvider tp = new ToolProvider() {
            public String name() { return "probe-tool"; }
            public int run(PrintWriter out, PrintWriter err, String... args) {
                out.print("out:");
                for (String a : args) out.print(a + ",");
                err.print("err");
                return args.length;
            }
        };

        check("probe-tool".equals(tp.name()), "name");
        check(tp.description().isEmpty(), "description default empty");

        // run(PrintWriter, PrintWriter, String...)
        StringWriter so = new StringWriter();
        StringWriter se = new StringWriter();
        int rc = tp.run(new PrintWriter(so), new PrintWriter(se), "a", "b");
        check(rc == 2, "run(PrintWriter) returns arg count");
        check("out:a,b,".equals(so.toString()), "run(PrintWriter) out content");
        check("err".equals(se.toString()), "run(PrintWriter) err content");

        StringWriter so2 = new StringWriter();
        check(tp.run(new PrintWriter(so2), new PrintWriter(new StringWriter())) == 0,
              "run(PrintWriter) zero args -> 0");
        check("out:".equals(so2.toString()), "run(PrintWriter) zero args out content");

        // default run(PrintStream, PrintStream, String...) wraps and flushes
        ByteArrayOutputStream bosOut = new ByteArrayOutputStream();
        ByteArrayOutputStream bosErr = new ByteArrayOutputStream();
        PrintStream psOut = new PrintStream(bosOut, true, StandardCharsets.UTF_8);
        PrintStream psErr = new PrintStream(bosErr, true, StandardCharsets.UTF_8);
        int rc2 = tp.run(psOut, psErr, "x");
        check(rc2 == 1, "run(PrintStream) returns arg count");
        check("out:x,".equals(new String(bosOut.toByteArray(), StandardCharsets.UTF_8)),
              "run(PrintStream) flushed out content");
        check("err".equals(new String(bosErr.toByteArray(), StandardCharsets.UTF_8)),
              "run(PrintStream) flushed err content");

        // default run(PrintStream) null-argument validation
        expectThrows(NullPointerException.class, () -> tp.run(psOut, (PrintStream) null, "x"),
                     "run(PrintStream) null err");
        expectThrows(NullPointerException.class, () -> tp.run(psOut, psErr, (String) null),
                     "run(PrintStream) null arg element");

        // static findFirst lookup
        check(ToolProvider.findFirst("definitely-not-a-tool").isEmpty(),
              "findFirst missing -> empty");
        check(ToolProvider.findFirst("javac") != null, "findFirst never returns null");
    }


    // ===================== 契约深化：findFirst javac 交互（name/run/exit code）与缺失工具 Optional.empty（JDK-27 侧锁定） =====================
    static void testContracts() throws Exception {
        java.util.Optional<ToolProvider> javac = ToolProvider.findFirst("javac");
        check(javac.isPresent(), "findFirst(javac) present");
        if (javac.isPresent()) {
            ToolProvider tp = javac.get();
            check("javac".equals(tp.name()), "name() is javac");
            java.io.StringWriter out = new java.io.StringWriter();
            int rc = tp.run(new java.io.PrintWriter(out), new java.io.PrintWriter(out), "--version");
            check(rc == 0, "--version exits 0");
            check(out.toString().contains("javac"), "--version output mentions javac: " + out.toString().trim());
            // 错误用法：未知选项 -> 非零退出码
            java.io.StringWriter err = new java.io.StringWriter();
            int badRc = tp.run(new java.io.PrintWriter(new java.io.StringWriter()),
                    new java.io.PrintWriter(err), "--jck-no-such-option");
            check(badRc != 0, "unknown option exits non-zero (" + badRc + ")");
        }
        check(!ToolProvider.findFirst("jck-no-such-tool").isPresent(), "unknown tool empty Optional");
        expectThrows(NullPointerException.class, () -> ToolProvider.findFirst(null), "findFirst(null) NPE");
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
