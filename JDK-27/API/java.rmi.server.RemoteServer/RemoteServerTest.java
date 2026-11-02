/*
 * @test
 * @summary java.rmi.server.RemoteServer conformance: abstract shape and static log.
 * @run main RemoteServerTest
 */

import java.rmi.server.RemoteServer;
import java.rmi.server.RemoteObject;
import java.lang.reflect.Modifier;

public class RemoteServerTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(Modifier.isAbstract(RemoteServer.class.getModifiers()), "abstract");
        check(RemoteObject.class.isAssignableFrom(RemoteServer.class), "extends RemoteObject");
        check(RemoteServer.getLog() == null, "log null by default");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
