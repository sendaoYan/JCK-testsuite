/*
 * @test
 * @summary JLS 10.1 (Java SE 8): an array element type may be an abstract class;
 *          an element may hold null or any non-abstract subclass instance.
 * @run main ArrayTypeAbstractClassElement
 */

public class ArrayTypeAbstractClassElement {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    abstract static class A {}
    static class B extends A {}

    public static void main(String[] args) {
        A[] a = new A[3];
        a[0] = null;
        a[1] = new B();
        check(a[0] == null, "null element");
        check(a[1] instanceof A, "abstract class element");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
