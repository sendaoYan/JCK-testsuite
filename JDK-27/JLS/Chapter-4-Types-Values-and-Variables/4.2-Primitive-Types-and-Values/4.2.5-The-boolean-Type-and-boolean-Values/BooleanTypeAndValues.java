/*
 * @test
 * @summary JLS 4.2.5: the boolean type has exactly two values, true and false.
 *          Only boolean and Boolean expressions can be used in control-flow
 *          statements and as the first operand of the conditional operator ?:.
 *          A boolean may be cast only to boolean, Boolean, or Object.
 * @run main BooleanTypeAndValues
 */

public class BooleanTypeAndValues {
    public static void main(String[] args) {
        boolean t = true;
        boolean f = false;
        if (t == f) throw new AssertionError("true == false must be false");
        if (t != !f) throw new AssertionError("true must equal !false");

        // Logical operators &, ^, |, and logical complement !.
        if (!(t & f) == false) throw new AssertionError("true & false must be false");
        if ((t ^ f) != true) throw new AssertionError("true ^ false must be true");
        if ((t | f) != true) throw new AssertionError("true | false must be true");

        // Only boolean expressions determine control flow (if/while/do/for).
        if (!t) throw new AssertionError("if with boolean");
        int n = 0;
        while (n < 3) n++;
        if (n != 3) throw new AssertionError("while with boolean");
        do { n--; } while (n > 0);
        if (n != 0) throw new AssertionError("do-while with boolean");
        int sum = 0;
        for (int i = 0; i < 3; i++) sum += i;
        if (sum != 3) throw new AssertionError("for with boolean");

        // The conditional operator ?: uses a boolean first operand.
        int pick = t ? 1 : 2;
        if (pick != 1) throw new AssertionError("?: with boolean condition");

        // The C-style idiom: an integer/float expression x converts via x != 0.
        if (!toBool(5)) throw new AssertionError("5 != 0 must be true");
        if (toBool(0)) throw new AssertionError("0 != 0 must be false");
        if (!toBool(-3)) throw new AssertionError("-3 != 0 must be true");

        // A boolean value may be cast to boolean, Boolean, or Object only.
        Boolean boxed = (Boolean) t;
        Object obj = (Object) t;
        if (boxed != Boolean.TRUE) throw new AssertionError("cast to Boolean");
        if (obj != Boolean.TRUE) throw new AssertionError("cast to Object");
        boolean back = (boolean) obj;
        if (!back) throw new AssertionError("cast back to boolean");

        System.out.println("PASS");
    }

    // Idiom from JLS 4.2.5: "x!=0" converts a numeric expression to boolean.
    static boolean toBool(int x) {
        return x != 0;
    }
}
