/*
 * @test
 * @summary JLS 9.9 (Java SE 8): a function type is the type of a functional
 *          interface, used by lambda expressions and method references.
 * @run main FunctionType
 */

public class FunctionType {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface F<T, R> { R apply(T t); }

    public static void main(String[] args) {
        F<String, Integer> len = s -> s.length();
        check(len.apply("hello") == 5, "lambda function type");

        F<Integer, Integer> inc = x -> x + 1;
        check(inc.apply(1) == 2, "second lambda function type");

        F<String, String> id = FunctionType::identity;
        check(id.apply("abc").equals("abc"), "method reference function type");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static String identity(String s) { return s; }
}
