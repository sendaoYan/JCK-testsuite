/*
 * @test
 * @summary JLS 15.11.2: super.x accesses the superclass member that is hidden by
 *          a field of the same name in the subclass; a primary field access uses
 *          the type of the primary.
 * @run main FieldAccessSuper
 */

public class FieldAccessSuper {
    static class Base { int x = 1; }
    static class Sub extends Base { int x = 2; int getSuperX() { return super.x; } }

    public static void main(String[] args) {
        Sub s = new Sub();
        if (s.x != 2) throw new AssertionError("Sub.x hides Base.x");
        if (s.getSuperX() != 1) throw new AssertionError("super.x");
        if (((Base) s).x != 1) throw new AssertionError("field access via primary type");

        System.out.println("PASS");
    }
}
