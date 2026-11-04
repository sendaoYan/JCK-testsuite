/*
 * @test
 * @summary java.rmi.dgc.VMID conformance: uniqueness and isUnique.
 * @run main VMIDTest
 */

import java.rmi.dgc.*;
public class VMIDTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        VMID v1 = new VMID();
        VMID v2 = new VMID();
        check(!v1.equals(v2), "unique");
        check(v1.equals(v1), "self equals");
        check(VMID.isUnique(), "isUnique");
        check(v1.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
