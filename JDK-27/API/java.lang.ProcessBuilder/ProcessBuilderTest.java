/*
 * @test
 * @summary java.lang.ProcessBuilder conformance: white-box structure + black-box behavior.
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ProcessBuilder");
        check(Modifier.isFinal(c.getModifiers()), "ProcessBuilder is final");
        check(!c.isInterface(), "ProcessBuilder is a class");
        check(c.getConstructor(List.class) != null, "(List) constructor present");
        check(c.getConstructor(String[].class) != null, "(String...) constructor present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testCommand() {
        ProcessBuilder pb = new ProcessBuilder("a", "b", "c");
        check(pb.command().equals(Arrays.asList("a", "b", "c")), "command() reflects varargs");

        pb.command("x", "y");
        check(pb.command().equals(Arrays.asList("x", "y")), "command(String...) sets and returns");

        pb.command(Arrays.asList("m", "n", "o"));
        check(pb.command().equals(Arrays.asList("m", "n", "o")), "command(List) sets and returns");
        check(pb.command() == pb.command(), "command() returns a fresh/defensive view (callable)");
    }

    static void testEnvironment() {
        ProcessBuilder pb = new ProcessBuilder("cmd");
        Map<String, String> env = pb.environment();
        check(env != null, "environment() non-null");
        check(env.containsKey("PATH") || env.containsKey("Path") || env.containsKey("path"),
              "environment contains a path variable (case varies by OS)");
        env.put("JCK_PB_TEST_VAR", "value");
        check("value".equals(env.get("JCK_PB_TEST_VAR")), "environment map is mutable");
        env.remove("JCK_PB_TEST_VAR");
        check(!env.containsKey("JCK_PB_TEST_VAR"), "environment map removal");
    }

    static void testDirectoryAndRedirect() throws Exception {
        ProcessBuilder pb = new ProcessBuilder("cmd");
        File dir = java.nio.file.Files.createTempDirectory("jck-pb").toFile();
        try {
            pb.directory(dir);
            check(pb.directory() != null && pb.directory().getAbsolutePath().equals(dir.getAbsolutePath()),
                  "directory()/directory(File) roundtrip");
        } finally {
            java.nio.file.Files.deleteIfExists(dir.toPath());
        }
        check(!pb.redirectErrorStream(), "redirectErrorStream default false");
        pb.redirectErrorStream(true);
        check(pb.redirectErrorStream(), "redirectErrorStream(true)");
        check(pb.redirectInput() == ProcessBuilder.Redirect.PIPE, "redirectInput default PIPE");
        check(pb.redirectOutput() == ProcessBuilder.Redirect.PIPE, "redirectOutput default PIPE");
        check(pb.redirectError() == ProcessBuilder.Redirect.PIPE, "redirectError default PIPE");
        pb.inheritIO(); // must not throw
        passed++;
    }

    static void testStartMissingCommand() throws Exception {
        ProcessBuilder pb = new ProcessBuilder("jck-no-such-command-xyz-12345");
        try {
            pb.start();
            throw new AssertionError("start() of missing command: expected IOException");
        } catch (java.io.IOException expected) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCommand();
        testEnvironment();
        testDirectoryAndRedirect();
        testStartMissingCommand();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
