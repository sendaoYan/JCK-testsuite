/*
 * @test
 * @summary java.lang.Readable conformance: white-box structure + black-box behavior
 *          (read(CharBuffer) copies and returns count, -1 at EOF, NPE for null).
 * @run main ReadableTest
 */

import java.io.StringReader;
import java.nio.CharBuffer;

public class ReadableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Readable");
        check(c.isInterface(), "Readable is an interface");
        java.lang.reflect.Method read = c.getMethod("read", CharBuffer.class);
        check(read.getReturnType() == int.class, "read(CharBuffer) returns int");
        boolean throwsIO = false;
        for (Class<?> t : read.getExceptionTypes()) {
            if (t == java.io.IOException.class) throwsIO = true;
        }
        check(throwsIO, "read(CharBuffer) declares IOException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testRead() throws Exception {
        Readable r = new StringReader("hello");
        CharBuffer buf = CharBuffer.allocate(3);
        int n = r.read(buf);
        check(n == 3, "read returns chars copied");
        check(buf.flip().toString().equals("hel"), "buffer contents");
        CharBuffer rest = CharBuffer.allocate(10);
        check(r.read(rest) == 2, "read remaining 2");
        check(rest.flip().toString().equals("lo"), "remaining contents");
        check(r.read(CharBuffer.allocate(10)) == -1, "read at EOF returns -1");
    }

    static void testNull() throws Exception {
        Readable r = new StringReader("x");
        try {
            r.read(null);
            throw new AssertionError("read(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    
    static void testBlackBox() throws Exception {
        testRead();
        testNull();
    }
public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
