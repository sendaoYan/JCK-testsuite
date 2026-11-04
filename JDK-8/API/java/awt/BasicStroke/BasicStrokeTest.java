/*
 * @test
 * @summary java.awt.BasicStroke conformance: width/join/cap.
 * @run main BasicStrokeTest
 */


import java.awt.BasicStroke;
public class BasicStrokeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        BasicStroke s = new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
        check(s.getLineWidth() == 2.5f, "line width");
        check(s.getEndCap() == BasicStroke.CAP_ROUND, "end cap");
        check(s.getLineJoin() == BasicStroke.JOIN_BEVEL, "line join");
        check(s.getMiterLimit() == 10.0f, "default miter limit");
        check(s.getDashArray() == null, "no dash array default");
        BasicStroke def = new BasicStroke();
        check(def.getLineWidth() == 1.0f && def.getEndCap() == BasicStroke.CAP_SQUARE, "defaults");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
