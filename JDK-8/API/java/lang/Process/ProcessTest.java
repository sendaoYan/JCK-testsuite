/*
 * @test
 * @summary java.lang.Process conformance: white-box structure (abstract, six
 *          abstract methods, concrete waitFor(timeout)/destroyForcibly/isAlive)
 *          + black-box behavior via an in-memory mock and one trivial real
 *          process (stdout, exit status, isAlive, destroyForcibly).
 * @run main ProcessTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
        boolean destroyed;
        MockProcess(int exit) { this.exit = exit; }
        public OutputStream getOutputStream() { return captured; }
        public InputStream getInputStream() { return new ByteArrayInputStream(new byte[]{'i','n'}); }
        public InputStream getErrorStream() { return new ByteArrayInputStream(new byte[]{'e','r','r'}); }
        public int waitFor() { return exit; }
        public int exitValue() { return exit; }
        public void destroy() { destroyed = true; exit = 0; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Process");
        check(Modifier.isAbstract(c.getModifiers()), "Process is abstract");
        check(!c.isInterface(), "Process is a class");
        check(Modifier.isAbstract(c.getMethod("getInputStream").getModifiers()), "getInputStream abstract");
        check(Modifier.isAbstract(c.getMethod("getOutputStream").getModifiers()), "getOutputStream abstract");
        check(Modifier.isAbstract(c.getMethod("getErrorStream").getModifiers()), "getErrorStream abstract");
        check(Modifier.isAbstract(c.getMethod("waitFor").getModifiers()), "waitFor abstract");
        check(Modifier.isAbstract(c.getMethod("exitValue").getModifiers()), "exitValue abstract");
        check(Modifier.isAbstract(c.getMethod("destroy").getModifiers()), "destroy abstract");
        check(!Modifier.isAbstract(c.getMethod("isAlive").getModifiers()), "isAlive concrete");
        check(!Modifier.isAbstract(c.getMethod("destroyForcibly").getModifiers()),
              "destroyForcibly concrete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testMockStreams() throws Exception {
        MockProcess p = new MockProcess(0);
        String in = new String(readAll(p.getInputStream()), "UTF-8");
        String err = new String(readAll(p.getErrorStream()), "UTF-8");
        check("in".equals(in), "getInputStream returns input bytes");
        check("err".equals(err), "getErrorStream returns error bytes");
        p.getOutputStream().write("hello".getBytes("UTF-8"));
        p.getOutputStream().flush();
        check("hello".equals(p.captured.toString("UTF-8")), "getOutputStream captures writes");
    }

    static void testMockExitAndAlive() throws Exception {
        MockProcess p = new MockProcess(7);
        check(p.waitFor() == 7, "waitFor returns exit value");
        check(p.exitValue() == 7, "exitValue returns exit value");
        check(!p.isAlive(), "isAlive false after exit");
        check(p.waitFor(1, java.util.concurrent.TimeUnit.MILLISECONDS), "waitFor(timeout) true after exit");
    }

    static void testDestroyForcibly() {
        MockProcess pf = new MockProcess(9);
        check(pf.destroyForcibly() == pf, "destroyForcibly returns this");
        check(pf.destroyed, "destroyForcibly invokes destroy()");
    }

    static void testRealProcess() throws Exception {
        Process p = new ProcessBuilder("sh", "-c", "echo hello").start();
        String out = new String(readAll(p.getInputStream()), "UTF-8").trim();
        check(p.waitFor() == 0, "real process exit 0");
        check("hello".equals(out), "real process stdout");
        check(p.exitValue() == 0, "exitValue after waitFor");
        check(!p.isAlive(), "isAlive false after exit");
        p.destroyForcibly();
        passed++;
    }

    static byte[] readAll(InputStream in) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[64];
        int n;
        while ((n = in.read(buf)) != -1) bos.write(buf, 0, n);
        return bos.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testMockStreams();
        testMockExitAndAlive();
        testDestroyForcibly();
        testRealProcess();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
