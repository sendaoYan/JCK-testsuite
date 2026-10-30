/*
 * @test
 * @summary JLS 10.1: an array element type may be an interface; an element may
 *          hold null or any instance of a type that implements the interface.
 * @run main ArrayTypeInterfaceElement
 */

public class ArrayTypeInterfaceElement {
    interface I {}
    static class Impl implements I {}

    public static void main(String[] args) {
        I[] a = new I[3];
        a[0] = null;
        a[1] = new Impl();
        if (a[0] != null) throw new AssertionError("null element");
        if (!(a[1] instanceof I)) throw new AssertionError("interface element");
        System.out.println("PASS");
    }
}
