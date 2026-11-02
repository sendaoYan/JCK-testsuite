/*
 * @test
 * @summary org.ietf.jgss.GSSManager conformance: default GSS manager mechs.
 * @run main GSSManagerTest
 */

import org.ietf.jgss.*;

public class GSSManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        GSSManager gm = GSSManager.getInstance();
        check(gm != null, "getInstance non-null");
        Oid[] mechs = gm.getMechs();
        check(mechs.length > 0, "getMechs non-empty");
        check(gm.getNamesForMech(mechs[0]).length > 0, "getNamesForMech non-empty");
        check(gm.getMechsForName(mechs[0]).length >= 0, "getMechsForName returns array");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
