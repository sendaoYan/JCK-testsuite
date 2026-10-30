/*
 * @test
 * @summary JLS 8.1.3: an inner class may declare static members and static
 *          initializers (permitted since Java SE 16).
 * @run main InnerClassStaticMembers
 */

class Outer {
    class Inner {
        static int x = 3;
        static final int y = 4;
        static {
            if (y != 4) throw new AssertionError("static init");
        }
        static void hello() {
            if (x != 3) throw new AssertionError("static method");
        }
    }
}

public class InnerClassStaticMembers {
    public static void main(String[] args) {
        Outer.Inner.hello();
        if (Outer.Inner.x != 3 || Outer.Inner.y != 4)
            throw new AssertionError("inner class static members");
        System.out.println("PASS");
    }
}
