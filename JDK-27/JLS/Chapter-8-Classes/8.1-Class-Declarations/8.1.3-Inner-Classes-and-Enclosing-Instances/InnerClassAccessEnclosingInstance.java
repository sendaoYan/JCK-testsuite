/*
 * @test
 * @summary JLS 8.1.3: an inner class that is not in a static context may refer
 *          to instance variables of its enclosing class, using the instance of
 *          the corresponding lexically enclosing object.
 * @run main InnerClassAccessEnclosingInstance
 */

class Outer {
    int i = 100;

    class Inner {
        int j = i;   // refers to the enclosing Outer instance's i
    }
}

public class InnerClassAccessEnclosingInstance {
    public static void main(String[] args) {
        Outer o = new Outer();
        Outer.Inner in = o.new Inner();
        if (in.j != 100) throw new AssertionError("enclosing instance access");
        System.out.println("PASS");
    }
}
