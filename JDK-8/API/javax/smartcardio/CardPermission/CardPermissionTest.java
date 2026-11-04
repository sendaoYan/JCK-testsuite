/*
 * @test
 * @summary javax.smartcardio.CardPermission conformance: card permission.
 * @run main CardPermissionTest
 */

public class CardPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.smartcardio.CardPermission p = new javax.smartcardio.CardPermission("*", "connect");
        check("connect".equals(p.getActions()), "getActions");
        check("*".equals(p.getName()), "getName");
        check(p.implies(new javax.smartcardio.CardPermission("*", "connect")), "implies connect");
        check(!p.implies(new javax.smartcardio.CardPermission("*", "reset")), "not implies reset");
        javax.smartcardio.CardPermission p2 = new javax.smartcardio.CardPermission("*", "connect");
        check(p.equals(p2), "equals");
        check(p.hashCode() == p2.hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
