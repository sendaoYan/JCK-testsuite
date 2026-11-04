/*
 * @test
 * @summary java.rmi.server.LogStream conformance: white-box structure + black-box behavior.
 * @run main LogStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.rmi.server.LogStream;

public class LogStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() {
        Class<?> c = LogStream.class;
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(c.getSuperclass() == java.io.PrintStream.class, "extends PrintStream");
        check(c.isAnnotationPresent(Deprecated.class), "deprecated");
    }

    static void testBlackBox() {
        // level constants
        check(LogStream.SILENT == 0, "SILENT == 0");
        check(LogStream.BRIEF == 10, "BRIEF == 10");
        check(LogStream.VERBOSE == 20, "VERBOSE == 20");

        // parseLevel
        check(LogStream.parseLevel("SILENT") == LogStream.SILENT, "parse SILENT");
        check(LogStream.parseLevel("BRIEF") == LogStream.BRIEF, "parse BRIEF");
        check(LogStream.parseLevel("VERBOSE") == LogStream.VERBOSE, "parse VERBOSE");
        check(LogStream.parseLevel("7") == 7, "parse numeric");
        check(LogStream.parseLevel(null) == -1, "parse null -> -1");
        check(LogStream.parseLevel("") == -1, "parse empty -> -1");
        check(LogStream.parseLevel("NOPE") == -1, "parse unknown -> -1");

        // getDefaultStream
        check(LogStream.getDefaultStream() != null, "default stream non-null");

        // log(String) returns a named, cached LogStream
        LogStream ls1 = LogStream.log("conformance-log");
        check(ls1 != null, "log returns non-null");
        check("conformance-log".equals(ls1.toString()), "log name via toString");
        LogStream ls2 = LogStream.log("conformance-log");
        check(ls1 == ls2, "log caches same name");

        // output stream round trip (no output dependency)
        OutputStream initial = ls1.getOutputStream();
        check(initial != null, "getOutputStream non-null");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ls1.setOutputStream(baos);
        check(ls1.getOutputStream() == baos, "set/get output stream round trip");

        // write path is callable without throwing (no newline -> buffered, no external output)
        ls1.write('x');
        ls1.write(new byte[]{'a', 'b', 'c'}, 0, 3);
        ls1.flush();
        check(true, "write(int)/write(byte[],int,int)/flush callable");

        ls1.setOutputStream(initial);
        check(ls1.getOutputStream() == initial, "restore output stream");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
