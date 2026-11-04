/*
 * @test
 * @summary JLS 3.11 (Java SE 8): the twelve separators ( ) { } [ ] ; , . ... @
 *          :: are recognized and usable in their respective contexts.
 * @run main Separators
 */

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@interface Marker {}

public class Separators {
    static int passed;
    static void check(boolean c, String what) {
        if (c) { passed++; } else { throw new AssertionError("check failed: " + what); }
    }

    static void varargs(String... xs) {}

    static String identity(String s) { return s; }

    public static void main(String[] args) {
        int x = (1 + 2) * 3;            // ( ) grouping
        int[] arr = {1, 2, 3};          // { } initializer, [ ] indexing, , comma
        String s = "abc";
        int len = s.length();           // . member access, ; terminator
        varargs("a", "b");              // ... varargs
        @Marker Object o = new Object(); // @ annotation
        Function<String, String> f = Separators::identity;  // :: method reference
        Runnable r = () -> { };         // -> (an operator, used alongside)

        check(x == 9, "parentheses grouping");
        check(arr[1] == 2, "brackets indexing");
        check(len == 3, "dot member access");
        check(o != null, "annotation use");
        check(f.apply("z").equals("z"), "method reference");
        r.run();

        System.out.println("PASS (" + passed + " assertions)");
    }
}
