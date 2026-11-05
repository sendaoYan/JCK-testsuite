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


    // ---- deep contract: the six-method surface and its error channels ----
    static void testInterfaceTable() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.JShellConsole");
        java.util.TreeSet<String> names = new java.util.TreeSet<>();
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getDeclaringClass() == c) names.add(m.getName());
        check(names.equals(new java.util.TreeSet<>(java.util.Arrays.asList(
              "charset", "flush", "readLine", "readPassword", "reader", "writer"))),
              "exact six-method surface: " + names);
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getDeclaringClass() == c)
                check(!m.isDefault() && java.lang.reflect.Modifier.isAbstract(m.getModifiers()),
                      "abstract, no defaults: " + m.getName());
        }
        // the two read methods report failures as IOError (not IOException)
        check(jdk.jshell.JShellConsole.class.getMethod("readLine", String.class)
              .getExceptionTypes()[0] == java.io.IOError.class, "readLine throws IOError");
        check(jdk.jshell.JShellConsole.class.getMethod("readPassword", String.class)
              .getExceptionTypes()[0] == java.io.IOError.class, "readPassword throws IOError");
        check(!c.isAnnotationPresent(java.lang.FunctionalInterface.class), "not a functional interface");
        // console() is how the engine surfaces it (default empty on ExecutionEnv)
        check(jdk.jshell.spi.ExecutionEnv.class.getMethod("console").getReturnType()
              == java.util.Optional.class, "ExecutionEnv.console() returns Optional<JShellConsole>");
    }

    static void testReadExhaustion() throws Exception {
        // a reader at EOF makes readLine return null; the fixture mirrors that
        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.StringReader("only\n"));
        jdk.jshell.JShellConsole console = new jdk.jshell.JShellConsole() {
            public java.io.PrintWriter writer() { return new java.io.PrintWriter(new java.io.StringWriter()); }
            public java.io.Reader reader() { return br; }
            public String readLine(String p) throws java.io.IOError {
                try { return br.readLine(); } catch (java.io.IOException e) { throw new java.io.IOError(e); }
            }
            public char[] readPassword(String p) throws java.io.IOError {
                String s = readLine(p);
                return s == null ? null : s.toCharArray();
            }
            public void flush() { }
            public java.nio.charset.Charset charset() { return java.nio.charset.StandardCharsets.US_ASCII; }
        };
        check("only".equals(console.readLine(">")), "first readLine returns the line");
        check(console.readLine(">") == null, "exhausted reader yields null");
        check(console.readPassword(">") == null, "exhausted reader yields null password");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testInterfaceTable();
        testReadExhaustion();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
