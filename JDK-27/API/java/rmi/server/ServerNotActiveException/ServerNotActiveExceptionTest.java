/*
 * @test
 * @summary java.rmi.server.ServerNotActiveException conformance: white-box structure + black-box behavior.
 * @run main ServerNotActiveExceptionTest
 */

import java.lang.reflect.Modifier;
import java.rmi.server.ServerNotActiveException;

public class ServerNotActiveExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() {
        Class<?> c = ServerNotActiveException.class;
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(c.getSuperclass() == Exception.class, "extends Exception");
    }

    static void testBlackBox() {
        ServerNotActiveException e1 = new ServerNotActiveException();
        check(e1 != null, "default ctor non-null");
        check(e1.getMessage() == null, "default ctor message null");

        ServerNotActiveException e2 = new ServerNotActiveException("detail-msg");
        check("detail-msg".equals(e2.getMessage()), "String ctor message");
        check(e2.getCause() == null, "cause null");
        check(e2 instanceof Exception, "instanceof Exception");
        check(e2.getClass() == ServerNotActiveException.class, "exact type");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
