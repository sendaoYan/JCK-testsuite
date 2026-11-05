/*
 * @test
 * @summary jdk.jshell.execution.impl.ConsoleImpl.ConsoleOutputStream conformance: internal class reflection.
 * @modules jdk.jshell/jdk.jshell.execution.impl:+open
 * @run main/othervm --add-opens=jdk.jshell/jdk.jshell.execution.impl=ALL-UNNAMED ConsoleOutputStreamTest
 */
public class ConsoleOutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.impl.ConsoleImpl$ConsoleOutputStream", false, ConsoleOutputStreamTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.jshell.execution.impl.ConsoleImpl$ConsoleOutputStream"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.OutputStream"), "superclass");
        {
            java.lang.Class<?>[] arr = c.getInterfaces();
            String[] n = new String[arr.length];
            for (int i = 0; i < arr.length; i++) n[i] = arr[i].getName();
            java.util.Arrays.sort(n);
            check(String.join(";", n).equals(""), "interfaces");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(m.getName());
            }
            check(String.join(";", s).equals("write"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals("sinkInput"), "fields");
        }
        testConsoleWireProtocol();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract: byte-exact wire protocol between remote agent and local console ----
    static java.util.List<String> calls = new java.util.ArrayList<>();
    static java.io.StringWriter written = new java.io.StringWriter();

    static jdk.jshell.JShellConsole recordingConsole() {
        java.io.PrintWriter pw = new java.io.PrintWriter(written, true);
        return new jdk.jshell.JShellConsole() {
            public java.io.PrintWriter writer() { return pw; }
            public java.io.Reader reader() { return new java.io.StringReader("Z"); }
            public String readLine(String prompt) { calls.add("readLine:" + prompt); return prompt + "-ans"; }
            public char[] readPassword(String prompt) { calls.add("readPassword:" + prompt); return "secret".toCharArray(); }
            public void flush() { calls.add("flush"); }
            public java.nio.charset.Charset charset() { calls.add("charset"); return java.nio.charset.StandardCharsets.ISO_8859_1; }
        };
    }

    static jdk.jshell.JShellConsole nullConsole() {
        return new jdk.jshell.JShellConsole() {
            public java.io.PrintWriter writer() { throw new UnsupportedOperationException(); }
            public java.io.Reader reader() { throw new UnsupportedOperationException(); }
            public String readLine(String prompt) { return null; }
            public char[] readPassword(String prompt) { return null; }
            public void flush() { }
            public java.nio.charset.Charset charset() { return java.nio.charset.StandardCharsets.US_ASCII; }
        };
    }

    static void wi(java.io.OutputStream o, int v) throws java.io.IOException {
        o.write((v >> 24) & 0xFF); o.write((v >> 16) & 0xFF); o.write((v >> 8) & 0xFF); o.write(v & 0xFF);
    }
    static void wc(java.io.OutputStream o, String s) throws java.io.IOException {
        for (char ch : s.toCharArray()) { o.write((ch >> 8) & 0xFF); o.write(ch & 0xFF); }
    }
    static int ri(java.io.InputStream i) throws java.io.IOException {
        return (i.read() << 24) | (i.read() << 16) | (i.read() << 8) | i.read();
    }
    static String rs(java.io.InputStream i) throws java.io.IOException {
        int len = ri(i);
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < len; k++) sb.append((char) ((i.read() << 8) | i.read()));
        return sb.toString();
    }

    @SuppressWarnings("removal")
    static Object newStream(jdk.jshell.JShellConsole console) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.impl.ConsoleImpl$ConsoleOutputStream");
        return c.getDeclaredConstructor(jdk.jshell.JShellConsole.class).newInstance(console);
    }

    static void testConsoleWireProtocol() throws Exception {
        Object cos = newStream(recordingConsole());
        java.io.InputStream sink = (java.io.InputStream)
                cos.getClass().getField("sinkInput").get(cos);
        java.io.OutputStream out = (java.io.OutputStream) cos;
        // Task 0 WRITE_CHARS: [0][len:4][chars x2] -> forwarded to console.writer(), no ack
        out.write(0); wi(out, 3); wc(out, "abc");
        check(written.toString().equals("abc"), "WRITE_CHARS forwards text to the console writer");
        // Task 1 FLUSH_OUTPUT: ack 0
        out.write(1);
        check(sink.read() == 0, "FLUSH_OUTPUT acknowledges");
        // Task 2 READ_CHARS: one char from console.reader(), answered as int
        out.write(2);
        check(ri(sink) == 'Z', "READ_CHARS answers the next console char");
        // Task 3 READ_LINE: prompt echoed in, line answered as len+chars
        out.write(3); wi(out, 2); wc(out, "p>");
        check(rs(sink).equals("p>-ans"), "READ_LINE answers the read line");
        check(calls.contains("readLine:p>"), "READ_LINE passed the prompt");
        // Task 4 READ_PASSWORD
        out.write(4); wi(out, 2); wc(out, "pw");
        check(rs(sink).equals("secret"), "READ_PASSWORD answers the password chars");
        check(calls.contains("readPassword:pw"), "READ_PASSWORD passed the prompt");
        // Task 5 FLUSH_CONSOLE
        out.write(5);
        check(sink.read() == 0, "FLUSH_CONSOLE acknowledges");
        check(calls.contains("flush"), "FLUSH_CONSOLE invoked console.flush()");
        // Task 6 CHARSET: charset name answered as chars
        out.write(6);
        check(rs(sink).equals("ISO-8859-1"), "CHARSET answers the console charset name");
        // Task 7 ENSURE_OUTPUTS_ARE_WRITTEN
        out.write(7);
        check(sink.read() == 0, "ENSURE_OUTPUTS_ARE_WRITTEN acknowledges");
        // null answers: readLine/readPassword null -> int -1
        Object cos2 = newStream(nullConsole());
        java.io.InputStream sink2 = (java.io.InputStream)
                cos2.getClass().getField("sinkInput").get(cos2);
        java.io.OutputStream out2 = (java.io.OutputStream) cos2;
        out2.write(3); wi(out2, 1); wc(out2, "?");
        check(ri(sink2) == -1, "readLine null -> -1 sentinel");
        out2.write(4); wi(out2, 1); wc(out2, "?");
        check(ri(sink2) == -1, "readPassword null -> -1 sentinel");
    }
}
