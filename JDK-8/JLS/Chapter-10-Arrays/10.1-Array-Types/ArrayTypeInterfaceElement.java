/*
 * @test
 * @summary JLS 10.1 (Java SE 8): an array element type may be an interface; an
 *          element may hold null or any instance of a type that implements it.
 * @run main ArrayTypeInterfaceElement
 */

public class ArrayTypeInterfaceElement {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface I {}
    static class Impl implements I {}

    public static void main(String[] args) {
        I[] a = new I[3];
        a[0] = null;
        a[1] = new Impl();
        check(a[0] == null, "null element");
        check(a[1] instanceof I, "interface element");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
