/*
 * @test
 * @summary JLS 8.1.3 (Java SE 8): an inner class may declare a static member that
 *          is a constant variable.
 * @run main InnerClassConstantVariable
 */
class Outer {
    class Inner {
        static final int x = 3;
    }
}
public class InnerClassConstantVariable {
    public static void main(String[] args) {
        if (Outer.Inner.x != 3) throw new AssertionError("inner class constant variable");
        System.out.println("PASS");
    }
}
