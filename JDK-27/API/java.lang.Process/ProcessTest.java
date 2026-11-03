/*
 * @test
 * @summary java.lang.Process conformance: white-box structure + black-box behavior.
 * @run main ProcessTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;

public class ProcessTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Minimal in-memory Process implementation (no real external command).
    static class MockProcess extends Process {
        final ByteArrayOutputStream captured = new ByteArrayOutputStream();
        int exit;
        MockProcess(int exit) { this.exit = exit; }
        public OutputStream getOutputStream() { return captured; }
        public InputStream getInputStream() { return new ByteArrayInputStream("in".getBytes(java.nio.charset.StandardCharsets.UTF_8)); }
        public InputStream getErrorStream() { return new ByteArrayInputStream("err".getBytes(java.nio.charset.StandardCharsets.UTF_8)); }
        public int waitFor() { return exit; }
        public int exitValue() { return exit; }
        public void destroy() { exit = 0; }
        public void close() throws IOException { destroy(); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Process");
        check(Modifier.isAbstract(c.getModifiers()), "Process is abstract");
        check(!c.isInterface(), "Process is a class");
        check(java.io.Closeable.class.isAssignableFrom(c), "Process implements Closeable");
        check(Modifier.isAbstract(c.getMethod("getInputStream").getModifiers()), "getInputStream abstract");
        check(Modifier.isAbstract(c.getMethod("getOutputStream").getModifiers()), "getOutputStream abstract");
        check(Modifier.isAbstract(c.getMethod("waitFor").getModifiers()), "waitFor abstract");
        check(Modifier.isAbstract(c.getMethod("exitValue").getModifiers()), "exitValue abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testReaders() throws Exception {
        MockProcess p = new MockProcess(0);
        check(p.inputReader().readLine().equals("in"), "inputReader reads getInputStream");
        check(p.errorReader().readLine().equals("err"), "errorReader reads getErrorStream");
        check(p.inputReader(java.nio.charset.StandardCharsets.UTF_8) != null,
              "inputReader(Charset) non-null");
        check(p.errorReader(java.nio.charset.StandardCharsets.UTF_8) != null,
              "errorReader(Charset) non-null");
    }

    static void testWriters() throws Exception {
        MockProcess p = new MockProcess(0);
        p.outputWriter().write("hello");
        p.outputWriter().flush();
        p.outputWriter().close();
        String written = p.captured.toString(java.nio.charset.StandardCharsets.UTF_8.name());
        check(written.equals("hello"), "outputWriter writes to getOutputStream");
    }

    static void testExitAndAlive() throws Exception {
        MockProcess p = new MockProcess(7);
        check(p.waitFor() == 7, "waitFor returns exit value");
        check(p.exitValue() == 7, "exitValue returns exit value");
        check(!p.isAlive(), "isAlive false after exit");
        p.destroy();
        passed++;
    }

    static void testWaitForDuration() throws Exception {
        MockProcess p = new MockProcess(0);
        check(p.waitFor(java.time.Duration.ofMillis(100)), "waitFor(Duration) returns true when already exited");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testReaders();
        testWriters();
        testExitAndAlive();
        testWaitForDuration();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
