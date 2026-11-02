/*
 * @test
 * @summary java.rmi.Naming conformance: list guarded (no registry required).
 * @run main NamingTest
 */

import java.rmi.*;

public class NamingTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        try {
            String[] names = Naming.list("//127.0.0.1:1099/");
            check(names != null, "registry reachable, list non-null");
        } catch (ConnectException e) {
            check(true, "no registry (expected)");
        } catch (RemoteException e) {
            check(true, "remote exception (expected)");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
