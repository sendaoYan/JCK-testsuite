/*
 * @test
 * @summary java.rmi.server.UnicastRemoteObject conformance: static export/unexport surface (reflection).
 * @run main UnicastRemoteObjectTest
 */

import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.RemoteServer;
import java.lang.reflect.Modifier;

public class UnicastRemoteObjectTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(RemoteServer.class.isAssignableFrom(UnicastRemoteObject.class), "extends RemoteServer");
        check(!Modifier.isAbstract(UnicastRemoteObject.class.getModifiers()), "concrete");
        check(UnicastRemoteObject.class.getMethod("exportObject", java.rmi.Remote.class) != null, "exportObject");
        check(UnicastRemoteObject.class.getMethod("unexportObject", java.rmi.Remote.class, boolean.class) != null, "unexportObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
