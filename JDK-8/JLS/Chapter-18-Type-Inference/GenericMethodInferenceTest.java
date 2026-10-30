/*
 * @test
 * @summary JLS 18 (Java SE 8): type inference determines the type arguments of a
 *          generic method invocation from its arguments and the target type.
 * @run main GenericMethodInferenceTest
 */
import java.util.*;

public class GenericMethodInferenceTest {
    static <T> T identity(T x) { return x; }
    static <T> List<T> singleton(T x) { List<T> l = new ArrayList<T>(); l.add(x); return l; }

    public static void main(String[] args) {
        String s = identity("hi");   // infers T = String
        if (!s.equals("hi")) throw new AssertionError("generic method inference");

        Integer i = identity(42);    // infers T = Integer (boxed)
        if (i != 42) throw new AssertionError("boxed inference");

        List<String> l = singleton("a");
        if (l.size() != 1 || !l.get(0).equals("a")) throw new AssertionError("generic method list");

        System.out.println("PASS");
    }
}
