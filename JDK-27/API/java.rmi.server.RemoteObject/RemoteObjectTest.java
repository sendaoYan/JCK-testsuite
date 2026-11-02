/*
 * @test
 * @summary java.rmi.server.RemoteObject conformance: abstract shape and Remote/Serializable supertypes.
 * @run main RemoteObjectTest
 */

import java.rmi.server.RemoteObject;
import java.rmi.Remote;
import java.lang.reflect.Modifier;

public class RemoteObjectTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(Modifier.isAbstract(RemoteObject.class.getModifiers()), "abstract");
        check(Remote.class.isAssignableFrom(RemoteObject.class), "implements Remote");
        check(java.io.Serializable.class.isAssignableFrom(RemoteObject.class), "serializable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
