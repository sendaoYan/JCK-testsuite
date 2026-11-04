/*
 * @test
 * @summary JLS 8.5 (Java SE 8): a member class may be static or inner, and a member
 *          interface is declared directly in the body of another class or interface.
 * @run main MemberClassAndInterface
 */

public class MemberClassAndInterface {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Outer {
        static class StaticMember {
            int v() { return 1; }
        }
        class Inner {
            int v() { return 2; }
        }
        interface MemberInterface {
            int v();
        }
    }

    public static void main(String[] args) {
        Outer.StaticMember sm = new Outer.StaticMember();
        Outer o = new Outer();
        Outer.Inner in = o.new Inner();
        Outer.MemberInterface mi = new Outer.MemberInterface() {
            public int v() { return 3; }
        };
        check(sm.v() == 1, "static member class");
        check(in.v() == 2, "inner member class");
        check(mi.v() == 3, "member interface");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
