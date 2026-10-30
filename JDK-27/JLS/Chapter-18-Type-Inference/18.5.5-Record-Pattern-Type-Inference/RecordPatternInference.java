/*
 * @test
 * @summary JLS 18.5.5: a record pattern's binding variables take their types from
 *          the record component types; nested record patterns infer the types of
 *          their var bindings.
 * @run main RecordPatternInference
 */

public class RecordPatternInference {
    record Point(int x, int y) {}
    record Labeled(Point p, String label) {}

    public static void main(String[] args) {
        Object o = new Point(3, 4);
        if (o instanceof Point(int x, int y)) {
            if (x != 3 || y != 4) throw new AssertionError("record pattern x,y");
        } else {
            throw new AssertionError("no Point match");
        }

        // nested record pattern; var bindings infer int for the components.
        Object l = new Labeled(new Point(5, 6), "origin");
        if (l instanceof Labeled(Point(var a, var b), String s)) {
            if (a != 5 || b != 6 || !"origin".equals(s))
                throw new AssertionError("nested record pattern a,b,s");
        } else {
            throw new AssertionError("no Labeled match");
        }

        System.out.println("PASS");
    }
}
