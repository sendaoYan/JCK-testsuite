/*
 * @test
 * @summary java.rmi.server.SocketSecurityException conformance: white-box structure + black-box behavior.
 * @run main SocketSecurityExceptionTest
 */

import java.lang.reflect.Modifier;
import java.rmi.server.ExportException;
import java.rmi.server.SocketSecurityException;

public class SocketSecurityExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() {
        Class<?> c = SocketSecurityException.class;
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(c.getSuperclass() == ExportException.class, "extends ExportException");
        check(java.io.IOException.class.isAssignableFrom(c), "is IOException");
    }

    static void testBlackBox() {
        SocketSecurityException e1 = new SocketSecurityException("detail-msg");
        check("detail-msg".equals(e1.getMessage()), "single-arg message");
        check(e1.getCause() == null, "single-arg cause null");

        Exception inner = new Exception("inner");
        SocketSecurityException e2 = new SocketSecurityException("detail-msg", inner);
        check(e2.getCause() == inner, "two-arg cause identity");
        String m2 = e2.getMessage();
        check(m2 != null && m2.startsWith("detail-msg"), "two-arg message startsWith");
        check(m2.contains("nested exception"), "two-arg nested marker");
        check(m2.contains("inner"), "two-arg message includes cause text");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
