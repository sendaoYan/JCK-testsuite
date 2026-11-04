/*
 * @test
 * @summary java.lang.ProcessBuilder conformance: white-box structure (final,
 *          two constructors, no startPipeline in JDK-8) + black-box behavior
 *          (command/env/directory/redirect get-set round-trips, inheritIO,
 *          redirectErrorStream, Redirect type/file/round-trip, and one trivial
 *          real process via sh -c echo).
 * @run main ProcessBuilderTest
 */

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ProcessBuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectNPE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void expectIAE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ProcessBuilder");
        check(Modifier.isFinal(c.getModifiers()), "ProcessBuilder is final");
        check(!c.isInterface(), "ProcessBuilder is a class");
        c.getConstructor(List.class);
        c.getConstructor(String[].class);
        passed += 2;
        try {
            c.getMethod("startPipeline", List.class);
            throw new AssertionError("startPipeline should be absent in JDK-8");
        } catch (NoSuchMethodException expected) {
            passed++;
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testCommand() {
        ProcessBuilder pb = new ProcessBuilder("a", "b");
        check(pb.command().equals(Arrays.asList("a", "b")), "varargs ctor sets command list");
        pb.command(Arrays.asList("x", "y"));
        check(pb.command().equals(Arrays.asList("x", "y")), "command(List) replaces");
        pb.command("m", "n");
        check(pb.command().equals(Arrays.asList("m", "n")), "command(String...) replaces");
        expectNPE(() -> pb.command((List<String>) null), "command(null List)");
    }

    static void testEnvironment() {
        ProcessBuilder pb = new ProcessBuilder("true");
        Map<String, String> env = pb.environment();
        check(env != null, "environment non-null");
        check(env.containsKey("PATH") || !env.isEmpty(), "environment has entries");
        env.put("JCK_TEST_VAR", "1");
        check("1".equals(pb.environment().get("JCK_TEST_VAR")), "environment is live");
        pb.environment().remove("JCK_TEST_VAR");
        check(!pb.environment().containsKey("JCK_TEST_VAR"), "environment removal visible");
    }

    static void testDirectory() {
        ProcessBuilder pb = new ProcessBuilder("true");
        check(pb.directory() == null, "directory defaults null (inherit)");
        File dir = new File(System.getProperty("java.io.tmpdir", "."));
        pb.directory(dir);
        check(dir.equals(pb.directory()), "directory get/set round-trip");
    }

    static void testRedirects() {
        ProcessBuilder pb = new ProcessBuilder("true");
        check(pb.redirectInput() == ProcessBuilder.Redirect.PIPE, "redirectInput default PIPE");
        check(pb.redirectOutput() == ProcessBuilder.Redirect.PIPE, "redirectOutput default PIPE");
        check(pb.redirectError() == ProcessBuilder.Redirect.PIPE, "redirectError default PIPE");

        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        check(pb.redirectInput() == ProcessBuilder.Redirect.INHERIT, "redirectInput(INHERIT)");
        check(pb.redirectOutput() == ProcessBuilder.Redirect.INHERIT, "redirectOutput(INHERIT)");
        check(pb.redirectError() == ProcessBuilder.Redirect.INHERIT, "redirectError(INHERIT)");

        File out = new File(System.getProperty("java.io.tmpdir", "."), "jck-pb-out-" + System.nanoTime());
        pb.redirectOutput(out);
        check(pb.redirectOutput().type() == ProcessBuilder.Redirect.Type.WRITE, "redirectOutput(File) type WRITE");
        check(out.equals(pb.redirectOutput().file()), "redirectOutput(File) file round-trip");
        out.delete();

        pb.inheritIO();
        check(pb.redirectInput() == ProcessBuilder.Redirect.INHERIT, "inheritIO sets input INHERIT");
        check(pb.redirectOutput() == ProcessBuilder.Redirect.INHERIT, "inheritIO sets output INHERIT");
        check(pb.redirectError() == ProcessBuilder.Redirect.INHERIT, "inheritIO sets error INHERIT");
    }

    static void testRedirectErrorStream() {
        ProcessBuilder pb = new ProcessBuilder("true");
        check(!pb.redirectErrorStream(), "redirectErrorStream defaults false");
        check(pb.redirectErrorStream(true) == pb, "redirectErrorStream(boolean) returns this");
        check(pb.redirectErrorStream(), "redirectErrorStream now true");
    }

    static void testRedirectTypes() {
        check(ProcessBuilder.Redirect.PIPE.type() == ProcessBuilder.Redirect.Type.PIPE, "PIPE type");
        check(ProcessBuilder.Redirect.INHERIT.type() == ProcessBuilder.Redirect.Type.INHERIT, "INHERIT type");
        File f = new File("jck-redirect-from");
        ProcessBuilder.Redirect from = ProcessBuilder.Redirect.from(f);
        check(from.type() == ProcessBuilder.Redirect.Type.READ, "Redirect.from type READ");
        check(f.equals(from.file()), "Redirect.from file round-trip");
        ProcessBuilder.Redirect to = ProcessBuilder.Redirect.to(f);
        check(to.type() == ProcessBuilder.Redirect.Type.WRITE, "Redirect.to type WRITE");
        check(f.equals(to.file()), "Redirect.to file round-trip");
        check(from.equals(ProcessBuilder.Redirect.from(f)), "Redirect.equals same");
        check(from.hashCode() == ProcessBuilder.Redirect.from(f).hashCode(), "Redirect.hashCode same");
        check(!from.equals(ProcessBuilder.Redirect.to(f)), "READ != WRITE");
    }

    static void testRealStart() throws Exception {
        Process p = new ProcessBuilder("sh", "-c", "echo hello").redirectErrorStream(true).start();
        check(p.waitFor() == 0, "start() produces working process exit 0");
        String out = new String(readAll(p.getInputStream()), "UTF-8").trim();
        check("hello".equals(out), "stdout captured");
    }

    static byte[] readAll(java.io.InputStream in) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[64];
        int n;
        while ((n = in.read(buf)) != -1) bos.write(buf, 0, n);
        return bos.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCommand();
        testEnvironment();
        testDirectory();
        testRedirects();
        testRedirectErrorStream();
        testRedirectTypes();
        testRealStart();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
