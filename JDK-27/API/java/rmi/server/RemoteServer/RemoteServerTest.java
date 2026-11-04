/*
 * @test
 * @summary java.rmi.server.RemoteServer conformance: white-box structure + black-box behavior.
 * @run main RemoteServerTest
 */

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;

public class RemoteServerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() {
        Class<?> c = RemoteServer.class;
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()), "public abstract");
        check(c.getSuperclass() == RemoteObject.class, "extends RemoteObject");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "abstract class");
    }

    static void testBlackBox() throws Exception {
        // default log is null
        check(RemoteServer.getLog() == null, "getLog null by default");

        // setLog/getLog round trip (restored to null afterwards)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RemoteServer.setLog(baos);
        check(RemoteServer.getLog() != null, "getLog non-null after setLog");
        RemoteServer.setLog(null);
        check(RemoteServer.getLog() == null, "getLog restored null");

        // getClientHost throws ServerNotActiveException outside a remote call
        boolean threw = false;
        try { RemoteServer.getClientHost(); } catch (ServerNotActiveException expected) { threw = true; }
        check(threw, "getClientHost throws ServerNotActiveException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
