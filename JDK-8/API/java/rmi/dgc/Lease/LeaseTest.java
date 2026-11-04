/*
 * @test
 * @summary java.rmi.dgc.Lease conformance: VMID + value.
 * @run main LeaseTest
 */

import java.rmi.dgc.*;
public class LeaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        VMID v = new VMID();
        Lease l = new Lease(v, 1000L);
        check(l.getVMID().equals(v), "VMID");
        check(l.getValue() == 1000L, "value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
