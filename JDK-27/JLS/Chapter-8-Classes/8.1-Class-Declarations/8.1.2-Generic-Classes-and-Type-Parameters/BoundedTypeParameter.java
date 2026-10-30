/*
 * @test
 * @summary JLS 8.1.2: a generic class type parameter may be bounded, and the
 *          bound restricts the type arguments that may be supplied.
 * @run main BoundedTypeParameter
 */

class Box<T extends Number> {
    T value;
    Box(T v) { value = v; }
    double d() { return value.doubleValue(); }
}

public class BoundedTypeParameter {
    public static void main(String[] args) {
        Box<Integer> b = new Box<>(42);
        if (b.d() != 42.0) throw new AssertionError("bounded type parameter");
        System.out.println("PASS");
    }
}
