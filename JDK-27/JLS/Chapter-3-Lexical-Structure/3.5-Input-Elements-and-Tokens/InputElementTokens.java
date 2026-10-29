/*
 * @test
 * @summary JLS 3.5: white space separates tokens; a run that could be either
 *          an identifier or a keyword is reduced to the keyword; '>' is reduced
 *          individually in a type context even when adjacent '>' could form a
 *          shift operator.
 * @run main InputElementTokens
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InputElementTokens {
    public static void main(String[] args) {
        // "staticvoid" (eleven characters) is a single identifier, not the two
        // keywords "static" and "void" (JLS 3.5).
        int staticvoid = 1;
        if (staticvoid != 1) {
            throw new AssertionError("staticvoid was not a single identifier");
        }

        // The '>' characters in a type context are each reduced to the numeric
        // comparison operator '>', so no whitespace is needed (JLS 3.5).
        List<List<String>> nested = new ArrayList<>();
        nested.add(Arrays.asList("x"));
        if (!nested.get(0).get(0).equals("x")) {
            throw new AssertionError("nested generic type failed to parse");
        }

        // The three-character run x+++y is tokenized as x, ++, +, y (longest
        // possible translation), i.e. (x++) + y (JLS 3.2, 3.5).
        int x = 10;
        int y = 5;
        int r = x+++y;          // (x++) + y
        if (r != 15 || x != 11) {
            throw new AssertionError("x+++y should be (x++) + y, got r=" + r + ", x=" + x);
        }

        System.out.println("PASS");
    }
}
