/*
 * @test
 * @summary java.awt.geom.FlatteningPathIterator conformance: flattens curve segments.
 * @run main FlatteningPathIteratorTest
 */


import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
public class FlatteningPathIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        GeneralPath gp = new GeneralPath();
        gp.moveTo(0, 0);
        gp.lineTo(10, 10);
        FlatteningPathIterator it = new FlatteningPathIterator(gp.getPathIterator(null), 0.5);
        check(it.getFlatness() == 0.5, "flatness");
        int count = 0;
        float[] coords = new float[6];
        while (!it.isDone()) {
            it.currentSegment(coords);
            it.next();
            count++;
        }
        check(count >= 2, "iterates segments");
        check(it.isDone(), "done at end");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
