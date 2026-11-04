/*
 * @test
 * @summary JLS 3.5 (Java SE 8): white space separates tokens; a run that could
 *          be either an identifier or a keyword is reduced to the keyword; the
 *          characters x+++y are tokenized as x, ++, +, y.
 * @run main InputElementTokens
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InputElementTokens {
    static int passed;
    static void check(boolean c, String what) {
        if (c) { passed++; } else { throw new AssertionError("check failed: " + what); }
    }

    public static void main(String[] args) {
        // "staticvoid" is a single identifier, not the keywords static + void.
        int staticvoid = 1;
        check(staticvoid == 1, "staticvoid is a single identifier");

        // A run that is either an identifier or keyword is reduced to a keyword.
        List<List<String>> nested = new ArrayList<List<String>>();
        nested.add(Arrays.asList("x"));
        check(nested.get(0).get(0).equals("x"), "nested generic type parsed");

        // x+++y is tokenized as x, ++, +, y, i.e. (x++) + y.
        int x = 10;
        int y = 5;
        int r = x+++y;
        check(r == 15 && x == 11, "x+++y is (x++) + y");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
