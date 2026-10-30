/*
 * @test
 * @summary JLS 4.5/4.6/4.8 (Java SE 8): a parameterized type has a single erasure
 *          shared by all its instantiations; raw types bypass generic checking.
 * @run main ParameterizedTypes
 */
import java.util.*;

public class ParameterizedTypes {
    static class Box<T> {
        T value;
        Box(T v) { value = v; }
        T get() { return value; }
    }

    public static void main(String[] args) {
        Box<String> bs = new Box<String>("hello");
        if (!bs.get().equals("hello")) throw new AssertionError("parameterized get");

        // Erasure: all instantiations of Box share one runtime class.
        Box<Integer> bi = new Box<Integer>(42);
        if (bs.getClass() != bi.getClass()) throw new AssertionError("type erasure shares one class");

        // Erasure: List<String> and List<Integer> have the same runtime class.
        List<String> ls = new ArrayList<String>();
        List<Integer> li = new ArrayList<Integer>();
        if (ls.getClass() != li.getClass()) throw new AssertionError("list erasure");

        // Raw type bypasses generic checking but still stores values.
        Box raw = new Box("raw");
        if (!raw.get().equals("raw")) throw new AssertionError("raw type");

        System.out.println("PASS");
    }
}
