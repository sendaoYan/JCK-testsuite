/*
 * @test
 * @summary java.rmi.ServerError conformance: hierarchy and constructor.
 * @run main ServerErrorTest
 */

import java.rmi.ServerError;

public class ServerErrorTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(ServerError.class.getSuperclass() == java.rmi.RemoteException.class, "extends RemoteException");
        ServerError e = new ServerError("detail-msg", new Error("inner"));
        check(e.getMessage().startsWith("detail-msg"), "message");
        check(e.getCause() != null, "cause set");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
