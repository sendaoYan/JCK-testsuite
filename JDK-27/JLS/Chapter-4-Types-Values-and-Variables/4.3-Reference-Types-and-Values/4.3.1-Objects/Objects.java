/*
 * @test
 * @summary JLS 4.3.1: an object is a class instance or an array. Reference
 *          values are pointers to these objects, plus the special null
 *          reference. Two variables holding the same reference observe the same
 *          state; primitive variables are independent (Example 4.3.1-2).
 * @run main Objects
 */

class Value {
    int val;
}

public class Objects {
    public static void main(String[] args) {
        // Primitive values are independent copies.
        int i1 = 3;
        int i2 = i1;
        i2 = 4;
        if (i1 != 3) throw new AssertionError("i1 must remain 3");
        if (i2 != 4) throw new AssertionError("i2 must be 4");

        // Reference values point to the same object; state is shared.
        Value v1 = new Value();
        v1.val = 5;
        Value v2 = v1;
        v2.val = 6;
        if (v1.val != 6) throw new AssertionError("v1.val must be 6 (shared state)");
        if (v2.val != 6) throw new AssertionError("v2.val must be 6");

        // An object is a class instance or an array.
        Object o1 = new Value();
        Object o2 = new int[3];
        if (!(o1 instanceof Value)) throw new AssertionError("class instance");
        if (!(o2 instanceof int[])) throw new AssertionError("array instance");

        // The special null reference refers to no object.
        Value v3 = null;
        if (v3 != null) throw new AssertionError("null reference");

        // Reference equality operators == and != compare the pointer values.
        if (v1 != v2) throw new AssertionError("== must be true for same object");
        if (v1 == new Value()) throw new AssertionError("== must be false for distinct objects");

        System.out.println("PASS");
    }
}
