/*
 * @test
 * @summary JLS 4.8: a raw type is the erasure of a parameterized type (the
 *          generic type name with no type argument list). In a raw type, the
 *          types of constructors, instance methods, and non-static fields are
 *          the erasures of their generic types (Example 4.8-1).
 * @run main RawTypes
 */

class Cell<E> {
    E value;
    Cell(E v) { value = v; }
    E get() { return value; }
    void set(E v) { value = v; }
}

public class RawTypes {
    public static void main(String[] args) {
        // Raw type Cell; its members have type Object.
        Cell x = new Cell<String>("abc");
        // x.value and x.get() both have type Object.
        Object v = x.value;
        Object g = x.get();
        if (!v.equals("abc")) throw new AssertionError("raw field has type Object");
        if (!g.equals("abc")) throw new AssertionError("raw get() has type Object");

        // A value of any parameterized instance may be assigned to a raw variable.
        Cell raw = new Cell<Integer>(1);
        Object iv = raw.get();
        if (!iv.equals(1)) throw new AssertionError("raw variable accepts any parameterization");

        // Raw type in an instanceof / class literal context (reifiable).
        if (!(raw instanceof Cell)) throw new AssertionError("raw Cell is reifiable");
        if (raw.getClass() != Cell.class) throw new AssertionError("raw class literal");

        System.out.println("PASS");
    }
}
