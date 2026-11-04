/*
 * @test
 * @summary JLS 6.3 (Java SE 8): the scope of a type parameter of a class is the
 *          type parameter section and the rest of the declaration, including
 *          non-static member declarations; it is usable in field and method
 *          signatures and in method bodies.
 * @run main TypeParameterScope
 */

public class TypeParameterScope {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    static class Box<T> {
        T value;                        // type parameter usable in instance field
        Box(T v) { this.value = v; }
        T get() { return value; }
        <U> U pass(U u) { return u; }   // method type parameter in scope
    }

    public static void main(String[] args) {
        Box<String> b = new Box<String>("hi");
        check(b.get().equals("hi"), "class type parameter usable in field/method");
        check(b.pass(42) == 42, "method type parameter usable in body");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
