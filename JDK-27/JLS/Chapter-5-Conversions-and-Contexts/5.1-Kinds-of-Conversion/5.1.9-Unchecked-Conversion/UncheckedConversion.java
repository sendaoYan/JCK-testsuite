/*
 * @test
 * @summary JLS 5.1.9: there is an unchecked conversion from the raw type G to
 *          any parameterized type G<T1,...,Tn>, and from a raw array type G[]k
 *          to G<T1,...,Tn>[]k.
 * @run main UncheckedConversion
 */

import java.util.ArrayList;
import java.util.List;

public class UncheckedConversion {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        // Unchecked conversion from raw List to List<String>.
        List raw = new ArrayList();
        raw.add("a");
        List<String> ls = raw;          // unchecked conversion (raw -> List<String>)
        if (!ls.get(0).equals("a")) throw new AssertionError("raw -> List<String>");

        // Unchecked conversion from raw array type List[] to List<String>[].
        List[] rawArr = new List[1];
        rawArr[0] = new ArrayList<String>();
        List<String>[] lsArr = rawArr;  // unchecked conversion (raw array)
        if (lsArr.length != 1) throw new AssertionError("raw array -> List<String>[]");

        System.out.println("PASS");
    }
}
