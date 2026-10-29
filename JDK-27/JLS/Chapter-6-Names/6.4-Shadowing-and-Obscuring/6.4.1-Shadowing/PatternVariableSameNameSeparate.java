/*
 * @test
 * @summary JLS 6.4 Example 6.4-1 Test4: repeated patterns in disjoint scopes may
 *          reuse the same pattern variable name.
 * @run main PatternVariableSameNameSeparate
 */

class Point { int x, y; Point(int x, int y) { this.x = x; this.y = y; } }

public class PatternVariableSameNameSeparate {
    static int sum(Object a, Object b, Object c) {
        int total = 0;
        if (a instanceof Point p) total += p.x + p.y;
        if (b instanceof Point p) total += p.x + p.y;
        else if (c instanceof Point p) total += p.x + p.y;
        return total;
    }
    public static void main(String[] args) {
        int r = sum(new Point(1, 2), new Point(3, 4), new Point(5, 6));
        if (r != 10) throw new AssertionError("r " + r);

        System.out.println("PASS");
    }
}
