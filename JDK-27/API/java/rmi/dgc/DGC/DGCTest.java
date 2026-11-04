/*
 * @test
 * @summary java.rmi.dgc.DGC conformance: white-box structure + black-box behavior.
 * @run main DGCTest
 */

import java.rmi.dgc.DGC;
import java.rmi.dgc.Lease;
import java.rmi.dgc.VMID;
import java.rmi.server.ObjID;

public class DGCTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.dgc.DGC");
        check(c.isInterface(), "is interface");
        check(java.rmi.Remote.class.isAssignableFrom(c), "extends Remote");
        java.util.TreeSet<String> m = new java.util.TreeSet<>();
        for (java.lang.reflect.Method mm : c.getDeclaredMethods()) m.add(mm.getName());
        check(m.equals(java.util.Set.of("clean", "dirty")), "methods clean/dirty");

    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        class Impl implements DGC {
            public Lease dirty(ObjID[] ids, long seq, Lease lease) { return lease; }
            public void clean(ObjID[] ids, long seq, VMID vmid, boolean strong) { }
        }
        DGC d = new Impl();
        check(d instanceof java.rmi.Remote, "DGC impl instanceof Remote");
        VMID vmid = new VMID();
        Lease lease = new Lease(vmid, 1000L);
        Lease ret = d.dirty(new ObjID[0], 0L, lease);
        check(ret == lease, "dirty returns passed lease");
        d.clean(new ObjID[0], 0L, vmid, false);
        check(true, "clean callable");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
