/*
 * @test
 * @summary java.rmi.server.SkeletonMismatchException conformance: white-box structure + black-box behavior.
 * @run main SkeletonMismatchExceptionTest
 */

import java.lang.reflect.Modifier;
import java.rmi.RemoteException;
import java.rmi.server.SkeletonMismatchException;

public class SkeletonMismatchExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() {
        Class<?> c = SkeletonMismatchException.class;
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(c.getSuperclass() == RemoteException.class, "extends RemoteException");
    }

    static void testBlackBox() {
        SkeletonMismatchException e = new SkeletonMismatchException("detail-msg");
        check("detail-msg".equals(e.getMessage()), "message");
        check(e.getCause() == null, "cause null");
        check(e.detail == null, "detail null");

        boolean threw = false;
        try { e.initCause(new Exception("x")); } catch (IllegalStateException expected) { threw = true; }
        check(threw, "initCause throws IllegalStateException");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
