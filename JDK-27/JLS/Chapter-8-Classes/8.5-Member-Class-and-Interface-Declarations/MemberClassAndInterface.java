/*
 * @test
 * @summary JLS 8.5: a member class may be static or inner, and a member interface
 *          is declared directly in the body of another class or interface.
 * @run main MemberClassAndInterface
 */

class Outer {
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

public class MemberClassAndInterface {
    public static void main(String[] args) {
        Outer.StaticMember sm = new Outer.StaticMember();
        Outer o = new Outer();
        Outer.Inner in = o.new Inner();
        Outer.MemberInterface mi = () -> 3;
        if (sm.v() != 1) throw new AssertionError("static member");
        if (in.v() != 2) throw new AssertionError("inner member");
        if (mi.v() != 3) throw new AssertionError("member interface");
        System.out.println("PASS");
    }
}
