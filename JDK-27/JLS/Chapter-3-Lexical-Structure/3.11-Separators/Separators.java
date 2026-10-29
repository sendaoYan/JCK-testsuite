/*
 * @test
 * @summary JLS 3.11: the twelve separators ( ) { } [ ] ; , . ... @ ::
 *          are recognized and usable in their respective contexts.
 * @run main Separators
 */

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@interface Marker {}

public class Separators {
    static void varargs(String... xs) {}

    static String identity(String s) { return s; }

    public static void main(String[] args) {
        // ( ) grouping and invocation
        int x = (1 + 2) * 3;
        // { } array initializer, [ ] array indexing, , comma
        int[] arr = {1, 2, 3};
        // . member access, ; statement terminator
        String s = "abc";
        int len = s.length();
        // ... varargs
        varargs("a", "b");
        // @ annotation
        @Marker Object o = new Object();
        // :: method reference
        Function<String, String> f = Separators::identity;
        // ( ) argument list for a lambda
        Runnable r = () -> { };

        if (x != 9) throw new AssertionError("parentheses grouping");
        if (arr[1] != 2) throw new AssertionError("brackets indexing");
        if (len != 3) throw new AssertionError("dot member access");
        if (o == null) throw new AssertionError("annotation use");
        if (!f.apply("z").equals("z")) throw new AssertionError("method reference");
        r.run();

        System.out.println("PASS");
    }
}
