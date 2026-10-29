/*
 * @test
 * @summary JLS 4.5: a generic class/interface defines a set of parameterized
 *          types C<T1,...,Tn>. A parameterized type is well-formed only if the
 *          number of type arguments equals the number of type parameters and
 *          each argument is within the corresponding bound.
 * @run main ParameterizedTypes
 */

import java.util.List;
import java.util.ArrayList;

class Seq<T> {
    T head;
    Seq(T h) { head = h; }
}

class Pair<X, Y> {
    X first;
    Y second;
    Pair(X x, Y y) { first = x; second = y; }
}

public class ParameterizedTypes {
    public static void main(String[] args) {
        // Well-formed parameterized types.
        Seq<String> s = new Seq<String>("a");
        if (!s.head.equals("a")) throw new AssertionError("Seq<String>");

        Seq<Seq<String>> nested = new Seq<Seq<String>>(new Seq<String>("b"));
        if (!nested.head.head.equals("b")) throw new AssertionError("Seq<Seq<String>>");

        Pair<String, Integer> p = new Pair<String, Integer>("k", 42);
        if (!p.first.equals("k") || p.second != 42) throw new AssertionError("Pair<String,Integer>");

        // A parameterized type may be a nested member type (C.D<Object>).
        java.util.Map.Entry<String, Integer> e =
            new java.util.AbstractMap.SimpleEntry<String, Integer>("k", 1);
        if (!e.getKey().equals("k") || e.getValue() != 1) throw new AssertionError("nested");

        // The erasure of all parameterizations is the same run-time class.
        List<String> ls = new ArrayList<String>();
        List<Integer> li = new ArrayList<Integer>();
        if (ls.getClass() != li.getClass()) {
            throw new AssertionError("erased List<String> and List<Integer> share one class");
        }

        System.out.println("PASS");
    }
}
