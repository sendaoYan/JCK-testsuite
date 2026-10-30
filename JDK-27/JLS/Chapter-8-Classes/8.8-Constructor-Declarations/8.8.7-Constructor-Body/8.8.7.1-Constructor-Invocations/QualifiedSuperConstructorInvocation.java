/*
 * @test
 * @summary JLS 8.8.7.1: a qualified superclass constructor invocation provides
 *          the immediately enclosing instance for an inner superclass.
 * @run main QualifiedSuperConstructorInvocation
 */

class Outer {
    int secret = 5;
    class Inner {
        int  getSecret()      { return secret; }
        void setSecret(int s) { secret = s; }
    }
}

class ChildOfInner extends Outer.Inner {
    ChildOfInner(Outer x) { x.super(); }
}

public class QualifiedSuperConstructorInvocation {
    public static void main(String[] args) {
        Outer x = new Outer();
        ChildOfInner a = new ChildOfInner(x);
        ChildOfInner b = new ChildOfInner(x);
        if (b.getSecret() != 5) throw new AssertionError("initial secret");
        a.setSecret(6);
        if (b.getSecret() != 6) throw new AssertionError("shared enclosing instance");
        System.out.println("PASS");
    }
}
