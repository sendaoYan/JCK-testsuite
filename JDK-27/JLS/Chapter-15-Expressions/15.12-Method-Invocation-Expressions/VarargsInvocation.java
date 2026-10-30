/*
 * @test
 * @summary JLS 15.12.2.4: a variable arity (varargs) method is invoked with zero
 *          or more arguments, or with an explicit array argument.
 * @run main VarargsInvocation
 */

public class VarargsInvocation {
    static int sum(int... xs) {
        int s = 0;
        for (int x : xs) s += x;
        return s;
    }

    static String tag(String prefix, int... xs) { return prefix + xs.length; }

    public static void main(String[] args) {
        if (sum() != 0) throw new AssertionError("empty varargs");
        if (sum(1, 2, 3) != 6) throw new AssertionError("varargs sum");
        if (!"n3".equals(tag("n", 1, 2, 3))) throw new AssertionError("trailing varargs");
        if (sum(new int[]{4, 5}) != 9) throw new AssertionError("explicit array varargs");

        System.out.println("PASS");
    }
}
