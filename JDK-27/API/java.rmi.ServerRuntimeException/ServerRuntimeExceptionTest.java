/*
 * @test
 * @summary java.rmi.ServerRuntimeException conformance: hierarchy and constructor.
 * @run main ServerRuntimeExceptionTest
 */

import java.rmi.ServerRuntimeException;

public class ServerRuntimeExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(ServerRuntimeException.class.getSuperclass() == java.rmi.RemoteException.class, "extends RemoteException");
        ServerRuntimeException e = new ServerRuntimeException("detail-msg", new Exception("inner"));
        check(e.getMessage().startsWith("detail-msg"), "message");
        check(e.getCause() != null, "cause set");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
