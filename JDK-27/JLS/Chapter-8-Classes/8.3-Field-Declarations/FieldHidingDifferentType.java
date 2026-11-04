/*
 * @test
 * @summary JLS 8.3 / 8.3.1.1: a field declaration hides accessible superclass
 *          fields of the same name even if the types differ; the hidden field
 *          is reachable via super or a cast to the superclass type.
 * @run main FieldHidingDifferentType
 */
class Point { int x = 2; }
public class FieldHidingDifferentType extends Point {
    double x = 4.7;
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) {
        FieldHidingDifferentType t = new FieldHidingDifferentType();
        check(t.x == 4.7, "declared field hides superclass field");
        check(((Point) t).x == 2, "cast to superclass accesses hidden field");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
