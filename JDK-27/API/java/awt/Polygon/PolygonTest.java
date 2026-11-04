/*
 * @test
 * @summary java.awt.Polygon conformance: white-box structure + black-box behavior.
 * @run main PolygonTest
 */

import java.lang.reflect.*;

public class PolygonTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Polygon", false, PolygonTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Polygon");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(!Modifier.isAbstract(m), "concrete class");
        check(!c.isInterface(), "not interface");
        check(c.getEnumConstants() == null, "non-enum");
        check(java.awt.Shape.class.isAssignableFrom(c), "implements java.awt.Shape");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");

        check(c.getField("npoints").getType() == int.class, "public int npoints");
        check(c.getField("xpoints").getType() == int[].class, "public int[] xpoints");
        check(c.getField("ypoints").getType() == int[].class, "public int[] ypoints");

        check(c.getConstructor() != null, "ctor ()");
        check(c.getConstructor(int[].class, int[].class, int.class) != null, "ctor (int[],int[],int)");
        for (String name : new String[] {"reset", "invalidate", "translate", "addPoint",
                "getBounds", "getBoundingBox", "contains", "inside", "getBounds2D",
                "intersects", "getPathIterator"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(mm -> mm.getName().equals(name)), "method present: " + name);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // default constructor
        java.awt.Polygon p0 = new java.awt.Polygon();
        check(p0.npoints == 0, "default npoints == 0");
        check(p0.xpoints != null && p0.xpoints.length == 4, "default xpoints length 4");
        check(p0.ypoints != null && p0.ypoints.length == 4, "default ypoints length 4");
        check(p0.getBounds().equals(new java.awt.Rectangle(0, 0, 0, 0)), "empty polygon bounds");

        // array constructor
        int[] xs = {0, 10, 10, 0};
        int[] ys = {0, 0, 10, 10};
        java.awt.Polygon sq = new java.awt.Polygon(xs, ys, 4);
        check(sq.npoints == 4, "array ctor npoints");
        check(sq.xpoints[0] == 0 && sq.xpoints[2] == 10, "array ctor copies x");
        check(sq.ypoints[1] == 0 && sq.ypoints[2] == 10, "array ctor copies y");
        check(sq.xpoints.length == 4, "array ctor truncates to npoints length");
        // constructor copies (not alias) the input arrays
        xs[0] = 99;
        check(sq.xpoints[0] == 0, "array ctor copies not aliases");

        // constructor validation
        boolean ioobe = false;
        try { new java.awt.Polygon(new int[] {0, 1}, new int[] {0, 1}, 3); }
        catch (IndexOutOfBoundsException e) { ioobe = true; }
        check(ioobe, "npoints > length throws IndexOutOfBoundsException");
        boolean nase = false;
        try { new java.awt.Polygon(new int[] {0}, new int[] {0}, -1); }
        catch (NegativeArraySizeException e) { nase = true; }
        check(nase, "negative npoints throws NegativeArraySizeException");

        // getBounds / getBoundingBox / getBounds2D
        check(sq.getBounds().equals(new java.awt.Rectangle(0, 0, 10, 10)), "square bounds");
        check(sq.getBoundingBox().equals(new java.awt.Rectangle(0, 0, 10, 10)), "getBoundingBox");
        check(sq.getBounds2D().getX() == 0.0 && sq.getBounds2D().getWidth() == 10.0,
              "getBounds2D Rectangle2D");

        // contains (point)
        check(sq.contains(5, 5), "contains interior point");
        check(sq.contains(new java.awt.Point(5, 5)), "contains(Point)");
        check(sq.inside(5, 5), "inside(int,int) delegates");
        check(!sq.contains(-1, 5), "outside left");
        check(!sq.contains(15, 15), "outside corner");

        // contains (double / Point2D / rect)
        check(sq.contains(5.0, 5.0), "contains(double,double)");
        check(sq.contains(new java.awt.geom.Point2D.Double(5.0, 5.0)), "contains(Point2D)");
        check(sq.contains(4, 4, 2, 2), "contains inner rect (int form)");
        check(sq.contains(new java.awt.geom.Rectangle2D.Double(4, 4, 2, 2)), "contains(Rectangle2D)");
        check(!sq.contains(4, 4, 20, 2), "does not contain wide rect");

        // intersects
        check(sq.intersects(5, 5, 10, 10), "intersects overlapping (int form)");
        check(sq.intersects(new java.awt.geom.Rectangle2D.Double(5, 5, 10, 10)),
              "intersects(Rectangle2D)");
        check(!sq.intersects(20, 20, 1, 1), "no intersect far rect");

        // translate
        java.awt.Polygon moved = new java.awt.Polygon(new int[] {0, 10, 10, 0},
                                                      new int[] {0, 0, 10, 10}, 4);
        moved.translate(5, 7);
        check(moved.xpoints[0] == 5 && moved.ypoints[0] == 7, "translate shifts first vertex");
        check(moved.xpoints[2] == 15 && moved.ypoints[2] == 17, "translate shifts all vertices");
        check(moved.getBounds().equals(new java.awt.Rectangle(5, 7, 10, 10)), "translate moves bounds");

        // addPoint + growth
        java.awt.Polygon grow = new java.awt.Polygon();
        grow.addPoint(0, 0);
        grow.addPoint(10, 0);
        grow.addPoint(10, 10);
        grow.addPoint(0, 10);
        check(grow.npoints == 4, "addPoint increments npoints");
        check(grow.xpoints.length == 4 && grow.ypoints.length == 4, "arrays grow to MIN_LENGTH");
        grow.addPoint(20, 20);
        check(grow.npoints == 5, "addPoint past capacity increments");
        check(grow.xpoints.length >= 5 && grow.ypoints.length >= 5, "arrays grow past capacity");
        check(grow.xpoints[4] == 20 && grow.ypoints[4] == 20, "added vertex stored");
        check(grow.getBounds().equals(new java.awt.Rectangle(0, 0, 20, 20)), "bounds after addPoint");

        // reset / invalidate
        grow.reset();
        check(grow.npoints == 0, "reset clears npoints");
        check(grow.getBounds().equals(new java.awt.Rectangle(0, 0, 0, 0)), "reset empties bounds");
        java.awt.Polygon inv = new java.awt.Polygon(new int[] {0, 10}, new int[] {0, 10}, 2);
        java.awt.Rectangle before = inv.getBounds();
        inv.invalidate();
        check(inv.getBounds().equals(before), "invalidate recomputes same bounds");

        // path iterator
        java.awt.Polygon tri = new java.awt.Polygon(new int[] {0, 10, 0}, new int[] {0, 10, 10}, 3);
        java.awt.geom.PathIterator it = tri.getPathIterator(null);
        check(it.getWindingRule() == java.awt.geom.PathIterator.WIND_EVEN_ODD,
              "winding rule even-odd");
        float[] coords = new float[2];
        int seg = it.currentSegment(coords);
        check(seg == java.awt.geom.PathIterator.SEG_MOVETO && coords[0] == 0f && coords[1] == 0f,
              "first segment MOVETO at origin");
        it.next();
        check(it.currentSegment(coords) == java.awt.geom.PathIterator.SEG_LINETO,
              "subsequent segment LINETO");
        int count = 0;
        java.awt.geom.PathIterator it2 = tri.getPathIterator(null);
        while (!it2.isDone()) { it2.currentSegment(coords); count++; it2.next(); }
        check(count == 4, "triangle iterator has MOVETO+2*LINETO+CLOSE == 4 segments: " + count);

        // path iterator with transform
        java.awt.geom.AffineTransform tx = java.awt.geom.AffineTransform.getTranslateInstance(2.0, 3.0);
        java.awt.geom.PathIterator it3 = tri.getPathIterator(tx);
        float[] c3 = new float[2];
        it3.currentSegment(c3);
        check(c3[0] == 2f && c3[1] == 3f, "transform applied to path iterator");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
