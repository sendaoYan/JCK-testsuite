/*
 * @test
 * @summary java.lang.IO conformance: white-box structure + black-box behavior.
 * @run main IOTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.IO;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

public class IOTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.IO");
        check(Modifier.isFinal(c.getModifiers()), "IO is final");
        check(c.getConstructors().length == 0, "IO has no public constructors");
        check(c.getMethod("println", Object.class) != null, "println(Object) present");
        check(c.getMethod("println") != null, "println() present");
        check(c.getMethod("print", Object.class) != null, "print(Object) present");
        check(c.getMethod("readln") != null, "readln() present");
        check(c.getMethod("readln", String.class) != null, "readln(String) present");
        // the JDK-27 method surface is exactly these five statics: no console(),
        // isTerminal(), printf/format or write overloads exist on IO (JEP 495
        // deliberately excludes printf; measured with javap on JDK 27)
        for (Method m : c.getMethods())
            if (m.getDeclaringClass() == c)
                check(Modifier.isStatic(m.getModifiers()),
                      "IO method " + m.getName() + " is static");
        for (String absent : new String[]{"console", "isTerminal", "printf", "format", "write"})
            try {
                c.getMethod(absent);
                check(false, "IO must not declare " + absent);
            } catch (NoSuchMethodException expected) {
                check(true, "IO does not declare " + absent);
            }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testOutput() {
        // These write to System.out; they must not throw.
        IO.println("jck-io-println");
        IO.println();
        IO.print("jck-io-print");
        passed += 3;
    }


    static void testBlackBox() throws Exception {
        testOutput();
        testOutputCaptureMatrix();
        testReadln();
    }

    // ---- Part 2b: IO.* write through System.out (capture matrix) ----
    static void testOutputCaptureMatrix() throws Exception {
        String sep = System.lineSeparator();
        PrintStream oldOut = System.out;
        try {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            System.setOut(new PrintStream(buf, true, StandardCharsets.UTF_8));

            IO.println("X");                 // String via Object overload
            check(buf.toString(StandardCharsets.UTF_8).equals("X" + sep),
                  "println(\"X\") writes value + line separator");
            buf.reset();

            IO.println();                    // bare separator
            check(buf.toString(StandardCharsets.UTF_8).equals(sep),
                  "println() writes only the line separator");
            buf.reset();

            IO.print("n");
            check(buf.toString(StandardCharsets.UTF_8).equals("n"),
                  "print(Object) adds no separator");
            buf.reset();

            IO.println((Object) null);       // null is legal for print/println(Object)
            check(buf.toString(StandardCharsets.UTF_8).equals("null" + sep),
                  "println((Object)null) writes \"null\" + separator");
            buf.reset();

            IO.println(42);                  // int autoboxes through println(Object)
            check(buf.toString(StandardCharsets.UTF_8).equals("42" + sep),
                  "println(42) writes \"42\" + separator");
            buf.reset();

            IO.print(7);
            check(buf.toString(StandardCharsets.UTF_8).equals("7"),
                  "print(7) writes \"7\"");
            buf.reset();

            IO.println(123456789012345L);
            check(buf.toString(StandardCharsets.UTF_8).equals("123456789012345" + sep),
                  "println(long) writes its decimal form");
            buf.reset();

            IO.println('A');
            check(buf.toString(StandardCharsets.UTF_8).equals("A" + sep),
                  "println(char autoboxed) writes the character");
            buf.reset();

            IO.print((Object) new int[]{1, 2});
            String arrForm = buf.toString(StandardCharsets.UTF_8);
            check(arrForm.startsWith("[I@"),
                  "println(Object) of an int[] uses Object toString ([I@...), unlike PrintStream.println(int[])");
            buf.reset();
        } finally {
            System.setOut(oldOut);           // restore before anything else prints
        }
    }

    // ---- Part 2c: IO.readln reads System.in (cached reader, EOF -> null) ----
    static void testReadln() {
        java.io.InputStream oldIn = System.in;
        PrintStream oldOut = System.out;
        try {
            // IO caches one BufferedReader over the System.in instance seen at the
            // first readln call, so all three reads must share this one stream.
            System.setIn(new ByteArrayInputStream(
                    "abc\nrest".getBytes(StandardCharsets.UTF_8)));
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            System.setOut(new PrintStream(buf, true, StandardCharsets.UTF_8));

            String l1 = IO.readln("p1: ");
            check("abc".equals(l1), "readln(prompt) returns the first line");
            check(buf.toString(StandardCharsets.UTF_8).equals("p1: "),
                  "readln(prompt) writes the prompt via print");

            check("rest".equals(IO.readln()),
                  "readln() returns a final line without trailing newline");

            check(IO.readln() == null,
                  "readln() at EOF returns null (not empty string, not an exception)");
        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
    }

public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
