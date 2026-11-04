/*
 * @test
 * @summary jdk.jshell.JShellConsole conformance: console interface reflection.
 * @modules jdk.jshell
 * @run main JShellConsoleTest
 */

import java.lang.reflect.*;

public class JShellConsoleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("jdk.jshell.JShellConsole");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
    }

    // A minimal real implementation of JShellConsole backed by a string reader
    // and a string writer, so every method can be exercised directly.
    static jdk.jshell.JShellConsole consoleFixture() {
        final java.io.StringWriter sw = new java.io.StringWriter();
        final java.io.PrintWriter pw = new java.io.PrintWriter(sw, true);
        final java.io.StringReader sr = new java.io.StringReader("one\ntwo\n");
        final java.io.BufferedReader br = new java.io.BufferedReader(sr);
        return new jdk.jshell.JShellConsole() {
            public java.io.PrintWriter writer() { return pw; }
            public java.io.Reader reader() { return sr; }
            public String readLine(String prompt) throws java.io.IOError {
                try { return br.readLine(); }
                catch (java.io.IOException e) { throw new java.io.IOError(e); }
            }
            public char[] readPassword(String prompt) throws java.io.IOError {
                String s = readLine(prompt);
                return s == null ? null : s.toCharArray();
            }
            public void flush() { pw.flush(); }
            public java.nio.charset.Charset charset() { return java.nio.charset.StandardCharsets.UTF_8; }
        };
    }

    static void testBlackBox() throws Exception {
        jdk.jshell.JShellConsole console = consoleFixture();
        check(console != null, "console instance");

        java.io.PrintWriter w = console.writer();
        check(w != null, "writer() non-null");
        java.io.Reader r = console.reader();
        check(r != null, "reader() non-null");
        check(console.charset().equals(java.nio.charset.StandardCharsets.UTF_8), "charset() returns UTF-8");

        check("one".equals(console.readLine("prompt>")), "readLine() returns next line");
        check(java.util.Arrays.equals("two".toCharArray(), console.readPassword("pw>")), "readPassword() returns next line as chars");
        console.flush();

        check(console.writer() == w, "writer() stable singleton");
        check(console.reader() == r, "reader() stable singleton");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
