/*
 * @test
 * @summary JLS 6.3 (Java SE 27): the scope of a member declared in a class or
 *          interface is the entire body of the class or interface, so a member
 *          declared later in the body is reachable by a simple name earlier in
 *          the body.
 * @run main MemberScopeThroughClassBody
 */

public class MemberScopeThroughClassBody {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    int f() { return g() + laterField; }   // uses g and laterField declared below

    int g() { return 10; }
    int laterField = 5;

    public static void main(String[] args) {
        MemberScopeThroughClassBody t = new MemberScopeThroughClassBody();
        check(t.f() == 15, "member declared later is in scope");
        check(t.laterField == 5, "later field reachable by simple name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
