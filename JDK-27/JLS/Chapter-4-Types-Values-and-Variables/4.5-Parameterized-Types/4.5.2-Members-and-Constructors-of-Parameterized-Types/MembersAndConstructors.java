/*
 * @test
 * @summary JLS 4.5.2: the type of a member or constructor m of C in the
 *          parameterized type C<T1,...,Tn> is the substitution
 *          T[A1:=T1,...,An:=Tn] of its declared type.
 * @run main MembersAndConstructors
 */

class Box<T> {
    T value;
    Box(T v) { value = v; }
    T get() { return value; }
    void set(T v) { value = v; }
}

class SubBox<T> extends Box<T> {
    SubBox(T v) { super(v); }
}

public class MembersAndConstructors {
    public static void main(String[] args) {
        // In Box<String>, the member types are String (T := String).
        Box<String> b = new Box<String>("hello");
        String s = b.get();              // get() has type String in Box<String>
        b.set("world");
        if (!s.equals("hello")) throw new AssertionError("get() type substitution");
        if (!b.value.equals("world")) throw new AssertionError("field type substitution");

        // In Box<Integer>, the same members have type Integer.
        Box<Integer> bi = new Box<Integer>(7);
        Integer i = bi.get();            // get() has type Integer in Box<Integer>
        if (i != 7) throw new AssertionError("Integer substitution");

        // A member inherited from a superclass keeps the substituted supertype.
        SubBox<Double> sb = new SubBox<Double>(1.5);
        Double d = sb.get();             // inherited get() has type Double
        if (d != 1.5) throw new AssertionError("inherited member substitution");

        System.out.println("PASS");
    }
}
