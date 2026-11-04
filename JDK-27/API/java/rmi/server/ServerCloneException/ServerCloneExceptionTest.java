/*
 * @test
 * @summary java.rmi.server.ServerCloneException conformance: white-box structure + black-box behavior.
 * @run main ServerCloneExceptionTest
 */

import java.lang.reflect.Modifier;
import java.rmi.server.ServerCloneException;

public class ServerCloneExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() {
        Class<?> c = ServerCloneException.class;
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(c.getSuperclass() == CloneNotSupportedException.class, "extends CloneNotSupportedException");
    }

    static void testBlackBox() {
        ServerCloneException e1 = new ServerCloneException("detail-msg");
        check("detail-msg".equals(e1.getMessage()), "single-arg message");
        check(e1.getCause() == null, "single-arg cause null");
        check(e1.detail == null, "single-arg detail null");

        Exception inner = new Exception("inner");
        ServerCloneException e2 = new ServerCloneException("detail-msg", inner);
        check(e2.getCause() == inner, "two-arg cause identity");
        check(e2.detail == inner, "two-arg detail identity");
        String m2 = e2.getMessage();
        check(m2 != null && m2.startsWith("detail-msg"), "two-arg message startsWith");
        check(m2.contains("nested exception"), "two-arg nested marker");
        check(m2.contains("inner"), "two-arg message includes cause text");

        boolean threw = false;
        try { e1.initCause(new Exception("x")); } catch (IllegalStateException expected) { threw = true; }
        check(threw, "initCause throws IllegalStateException");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
