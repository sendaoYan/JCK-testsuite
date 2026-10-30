/*
 * @test
 * @summary JLS 8.8.8: overloading of constructors is resolved at compile time by
 *          each class instance creation expression.
 * @run main ConstructorOverloading
 */

public class ConstructorOverloading {
    int kind;
    ConstructorOverloading(int i) { kind = 1; }
    ConstructorOverloading(int i, int j) { kind = 2; }
    ConstructorOverloading(String s) { kind = 3; }

    public static void main(String[] args) {
        if (new ConstructorOverloading(1).kind != 1) throw new AssertionError("one arg");
        if (new ConstructorOverloading(1, 2).kind != 2) throw new AssertionError("two args");
        if (new ConstructorOverloading("x").kind != 3) throw new AssertionError("string arg");
        System.out.println("PASS");
    }
}
