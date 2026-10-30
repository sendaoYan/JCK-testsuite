/*
 * @test
 * @summary JLS 8.1.1.4: the static modifier on a member class means it has no
 *          immediately enclosing instance; it may be instantiated without one.
 * @run main StaticNestedClass
 */

class Outer {
    static class Nested {
        int val = 7;
    }
}

public class StaticNestedClass {
    public static void main(String[] args) {
        Outer.Nested n = new Outer.Nested();   // no Outer instance needed
        if (n.val != 7) throw new AssertionError("static nested class");
        System.out.println("PASS");
    }
}
