/*
 * @test
 * @summary JLS 10.1: an array element type may be an abstract class; an element
 *          may hold null or any non-abstract subclass instance.
 * @run main ArrayTypeAbstractClassElement
 */

public class ArrayTypeAbstractClassElement {
    abstract static class A {}
    static class B extends A {}

    public static void main(String[] args) {
        A[] a = new A[3];
        a[0] = null;
        a[1] = new B();
        if (a[0] != null) throw new AssertionError("null element");
        if (!(a[1] instanceof A)) throw new AssertionError("abstract class element");
        System.out.println("PASS");
    }
}
