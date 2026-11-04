/*
 * @test
 * @summary java.awt.Polygon conformance: points and contains.
 * @run main PolygonTest
 */


import java.awt.Polygon;
public class PolygonTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Polygon p = new Polygon();
        check(p.npoints == 0, "empty initially");
        p.addPoint(0, 0);
        p.addPoint(10, 0);
        p.addPoint(0, 10);
        check(p.npoints == 3, "three points");
        check(p.xpoints[1] == 10 && p.ypoints[2] == 10, "points stored");
        check(p.contains(2, 2), "contains interior");
        check(!p.contains(9, 9), "does not contain exterior");
        java.awt.Rectangle b = p.getBounds();
        check(b.x == 0 && b.y == 0 && b.width == 10 && b.height == 10, "bounds");
        check(p.getBoundingBox().equals(b), "getBoundingBox");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static java.awt.Polygon serPoly(java.awt.Polygon p) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(p);
        return (java.awt.Polygon) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void testContract() throws Exception {
        // constructor copies input arrays (no aliasing)
        int[] xs = {1, 2, 3}; int[] ys = {1, 2, 3};
        java.awt.Polygon p = new java.awt.Polygon(xs, ys, 3);
        xs[0] = 100; ys[0] = 100;
        check(p.xpoints[0] == 1 && p.ypoints[0] == 1, "ctor copies coordinate arrays");
        // array length binding: xpoints.length >= npoints
        java.awt.Polygon g = new java.awt.Polygon();
        for (int i = 0; i < 20; i++) g.addPoint(i, 0);
        check(g.npoints == 20, "npoints counts added points");
        check(g.xpoints.length >= g.npoints && g.ypoints.length >= g.npoints, "array capacity >= npoints");
        check(g.xpoints.length == g.ypoints.length, "x and y arrays same capacity");
        // mismatched array lengths vs n -> IndexOutOfBoundsException
        boolean threw = false;
        try { new java.awt.Polygon(new int[]{1}, new int[]{1, 2}, 3); } catch (java.lang.IndexOutOfBoundsException e) { threw = true; }
        check(threw, "n exceeds arrays -> IndexOutOfBoundsException");
        threw = false;
        try { new java.awt.Polygon(new int[]{1}, new int[]{1}, -1); } catch (java.lang.NegativeArraySizeException e) { threw = true; }
        check(threw, "negative n -> NegativeArraySizeException");
        // n smaller than arrays uses first n points only
        java.awt.Polygon part = new java.awt.Polygon(new int[]{0, 10, 99}, new int[]{0, 10, 99}, 2);
        check(part.npoints == 2, "n limits used points");
        check(part.getBounds().equals(new java.awt.Rectangle(0, 0, 10, 10)), "unused point not in bounds");
        // contains: interior true; observed edge behavior locked (javadoc: undefined on edges)
        java.awt.Polygon sq = new java.awt.Polygon();
        sq.addPoint(0, 0); sq.addPoint(10, 0); sq.addPoint(10, 10); sq.addPoint(0, 10);
        check(sq.contains(5, 5), "interior contains");
        check(!sq.contains(-1, 5), "outside left");
        check(!sq.contains(11, 5), "outside right");
        check(!sq.contains(5, -1), "outside above");
        check(!sq.contains(5, 11), "outside below");
        check(sq.contains(5, 0), "top edge observed true");
        check(sq.contains(0, 5), "left edge observed true");
        check(sq.contains(0, 0), "top-left vertex observed true");
        check(!sq.contains(10, 10), "far vertex observed false");
        check(sq.contains(5.0, 5.0), "contains(double) interior");
        check(!sq.contains(10.5, 5.0), "contains(double) outside");
        check(sq.contains(new java.awt.Point(1, 1)), "contains(Point)");
        check(sq.contains(2.0, 2.0, 3.0, 3.0), "contains inner rect");
        check(!sq.contains(5.0, 5.0, 10.0, 10.0), "contains rect sticking out");
        check(sq.intersects(5.0, 5.0, 10.0, 10.0), "intersects overlapping");
        check(!sq.intersects(20.0, 20.0, 5.0, 5.0), "intersects disjoint");
        // empty polygon contains nothing
        check(!new java.awt.Polygon().contains(0, 0), "empty polygon contains nothing");
        check(new java.awt.Polygon().getBounds().isEmpty(), "empty polygon empty bounds");
        // bounds are cached until invalidate: direct array mutation needs invalidate
        java.awt.Polygon cache = new java.awt.Polygon();
        cache.addPoint(0, 0); cache.addPoint(5, 0); cache.addPoint(0, 5);
        check(cache.getBounds().equals(new java.awt.Rectangle(0, 0, 5, 5)), "bounds computed");
        cache.xpoints[0] = -4; cache.invalidate();
        check(cache.getBounds().x == -4, "invalidate forces bounds recompute");
        // translate
        cache.translate(10, 20);
        check(cache.getBounds().x == 6 && cache.getBounds().y == 20, "translate shifts points and bounds");
        check(cache.xpoints[0] == 6, "translate updated point data");
        // reset
        cache.reset();
        check(cache.npoints == 0, "reset clears npoints");
        check(cache.getBounds().width == 0 && cache.getBounds().height == 0, "reset empty bounds");
        // getBounds2D returns double view
        java.awt.geom.Rectangle2D b2 = sq.getBounds2D();
        check(b2.getX() == 0.0 && b2.getY() == 0.0 && b2.getWidth() == 10.0 && b2.getHeight() == 10.0,
              "getBounds2D exact");
        // serialization round-trip
        java.awt.Polygon rt = serPoly(sq);
        check(rt.npoints == 4 && rt.contains(5, 5), "serialization round-trip usable");
        check(rt.getBounds().equals(sq.getBounds()), "serialized bounds equal");
        // deprecated getBoundingBox alias
        check(sq.getBoundingBox().equals(sq.getBounds()), "getBoundingBox same as getBounds");
        // Polygon implements Shape
        check(java.awt.Shape.class.isAssignableFrom(java.awt.Polygon.class), "implements Shape");
        check(!sq.getPathIterator(null).isDone(), "path iterator has segments");
    }

}
