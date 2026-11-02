/*
 * @test
 * @summary java.rmi.dgc.Lease conformance: vmid and value.
 * @run main LeaseTest
 */

import java.rmi.dgc.*;

public class LeaseTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        VMID vmid = new VMID();
        Lease lease = new Lease(vmid, 60000L);
        check(lease.getVMID().equals(vmid), "vmid");
        check(lease.getValue() == 60000L, "value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
