/*
 * @test
 * @summary JLS 8.1.7: a class body may be empty; an empty class still gets an
 *          implicitly declared default constructor.
 * @run main EmptyClassBody
 */
class Empty { }
public class EmptyClassBody {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) {
        Empty e = new Empty();
        check(e != null, "empty class body instantiates via default constructor");
        check(e.getClass().getDeclaredConstructors().length == 1, "default constructor declared");
        check(e.getClass().getDeclaredFields().length == 0, "no members declared");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
