/*
 * @test
 * @summary java.rmi.registry.LocateRegistry conformance: getRegistry returns stubs without connecting.
 * @run main LocateRegistryTest
 */

import java.rmi.registry.*;

public class LocateRegistryTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        Registry r = LocateRegistry.getRegistry();
        check(r != null, "getRegistry()");
        Registry r2 = LocateRegistry.getRegistry(1099);
        check(r2 != null, "getRegistry(port)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
