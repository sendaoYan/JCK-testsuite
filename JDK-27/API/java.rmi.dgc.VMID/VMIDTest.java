/*
 * @test
 * @summary java.rmi.dgc.VMID conformance: uniqueness and toString.
 * @run main VMIDTest
 */

import java.rmi.dgc.VMID;

public class VMIDTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        VMID v = new VMID();
        check(v.toString() != null && v.toString().length() > 0, "toString");
        check(v.hashCode() == v.hashCode(), "hashcode");
        check(v.equals(v), "equals self");
        check(!v.equals(new VMID()), "unique");
        check(VMID.isUnique(), "isUnique");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
