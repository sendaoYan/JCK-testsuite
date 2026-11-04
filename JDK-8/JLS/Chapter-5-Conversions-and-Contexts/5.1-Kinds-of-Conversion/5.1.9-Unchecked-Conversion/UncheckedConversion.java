/*
 * @test
 * @summary JLS 5.1.9 (Java SE 8): there is an unchecked conversion from the raw
 *          type G to any parameterized type G<T1,...,Tn>, and from the raw
 *          array type G[]k to G<T1,...,Tn>[]k. It is used where an unchecked
 *          conversion is permitted (assignment/invocation after erasure).
 * @run main UncheckedConversion
 */

import java.util.ArrayList;
import java.util.List;

public class UncheckedConversion {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        // Raw List -> List<String>: unchecked conversion.
        List raw = new ArrayList();
        raw.add("a");
        List<String> ls = raw;
        check("a".equals(ls.get(0)), "raw -> List<String> unchecked conversion");

        // Raw array List[] -> List<String>[]: unchecked conversion.
        List[] rawArr = new List[1];
        rawArr[0] = new ArrayList<String>();
        List<String>[] lsArr = rawArr;
        check(lsArr.length == 1, "raw array -> List<String>[] unchecked conversion");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
