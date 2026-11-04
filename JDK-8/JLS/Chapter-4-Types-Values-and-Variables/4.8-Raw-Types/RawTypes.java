/*
 * @test
 * @summary JLS 4.8 (Java SE 8): a raw type is the erasure of a parameterized
 *          type (the generic type name with no type argument list) or an array
 *          type whose element type is raw. The types of constructors, instance
 *          methods and non-static fields of a raw type are the erasures of
 *          their generic types.
 * @run main RawTypes
 */

class Cell<E> {
    E value;
    Cell(E v) { value = v; }
    E get() { return value; }
    void set(E v) { value = v; }
}

public class RawTypes {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // Raw Cell: its members have type Object (the erasure of E).
        Cell raw = new Cell<String>("abc");
        Object v = raw.value;      // raw field type is Object
        Object g = raw.get();      // raw get() returns Object
        check("abc".equals(v) && "abc".equals(g), "raw members have erasure type Object");

        // Any parameterization is assignable to the raw type.
        Cell raw2 = new Cell<Integer>(1);
        check(Integer.valueOf(1).equals(raw2.get()), "raw accepts any parameterization");

        // Raw types are reifiable (instanceof / class literal allowed).
        check(raw instanceof Cell, "raw Cell is reifiable");
        check(raw.getClass() == Cell.class, "raw class literal");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
