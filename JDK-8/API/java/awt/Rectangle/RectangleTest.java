/*
 * @test
 * @summary java.awt.Rectangle conformance: bounds, contains, intersection.
 * @run main RectangleTest
 */


import java.awt.Rectangle;
public class RectangleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Rectangle r = new Rectangle(10, 20, 30, 40);
        check(r.x == 10 && r.y == 20 && r.width == 30 && r.height == 40, "fields");
        check(r.contains(15, 25), "contains interior");
        check(!r.contains(5, 5), "does not contain outside");
        check(r.contains(new Rectangle(11, 21, 5, 5)), "contains rect");
        Rectangle inter = r.intersection(new Rectangle(30, 30, 100, 100));
        check(inter.x == 30 && inter.y == 30 && inter.width == 10 && inter.height == 30, "intersection");
        Rectangle u = r.union(new Rectangle(100, 100, 5, 5));
        check(u.x == 10 && u.y == 20 && u.width == 95 && u.height == 85, "union");
        check(!r.isEmpty(), "not empty");
        check(new Rectangle().isEmpty(), "default empty");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static java.awt.Rectangle serRect(java.awt.Rectangle r) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(r);
        return (java.awt.Rectangle) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void testContract() throws Exception {
        // add(Point) mutates in place (void) and grows to include point
        java.awt.Rectangle r = new java.awt.Rectangle(0, 0, 10, 10);
        r.add(new java.awt.Point(20, 5));
        check(r.x == 0 && r.y == 0 && r.width == 20 && r.height == 10, "add(Point) right edge: " + r);
        r = new java.awt.Rectangle(5, 5, 10, 10);
        r.add(new java.awt.Point(0, 7));
        check(r.x == 0 && r.y == 5 && r.width == 15 && r.height == 10, "add(Point) left edge: " + r);
        r = new java.awt.Rectangle(5, 5, 10, 10);
        r.add(new java.awt.Point(7, 40));
        check(r.y == 5 && r.height == 35, "add(Point) below: " + r);
        r = new java.awt.Rectangle(5, 5, 10, 10);
        r.add(new java.awt.Point(7, 8));
        check(r.x == 5 && r.y == 5 && r.width == 10 && r.height == 10, "add(Point) inside no-op");
        // add(int,int)
        r = new java.awt.Rectangle(0, 0, 10, 10);
        r.add(-5, 15);
        check(r.x == -5 && r.y == 0 && r.width == 15 && r.height == 15, "add(int,int) grows: " + r);
        // add empty rectangle -> no change in geometry of empty
        r = new java.awt.Rectangle(0, 0, 10, 10);
        r.add(new java.awt.Rectangle(20, 20, 0, 0));
        check(r.width == 20 && r.height == 20, "add empty rect extends to point");
        // intersection with touching edge -> zero width (empty) rect, not negative
        java.awt.Rectangle t = new java.awt.Rectangle(0, 0, 10, 10).intersection(new java.awt.Rectangle(10, 0, 5, 5));
        check(t.x == 10 && t.y == 0 && t.width == 0 && t.height == 5, "touching intersection zero width: " + t);
        check(t.isEmpty(), "touching intersection empty");
        // disjoint intersection -> negative size
        java.awt.Rectangle d = new java.awt.Rectangle(0, 0, 10, 10).intersection(new java.awt.Rectangle(100, 100, 5, 5));
        check(d.width < 0 && d.height < 0, "disjoint intersection negative size: " + d);
        check(d.isEmpty(), "disjoint intersection isEmpty");
        // union of disjoint is bounding box
        java.awt.Rectangle u = new java.awt.Rectangle(0, 0, 5, 5).union(new java.awt.Rectangle(20, 20, 5, 5));
        check(u.x == 0 && u.y == 0 && u.width == 25 && u.height == 25, "union bounding: " + u);
        // union with a negatively-sized (empty) rect: observed the empty rect is ignored
        java.awt.Rectangle base = new java.awt.Rectangle(0, 0, 5, 5);
        java.awt.Rectangle u2 = base.union(new java.awt.Rectangle(10, 10, -1, -1));
        check(u2.x == 0 && u2.y == 0 && u2.width == 5 && u2.height == 5,
              "union with empty rect ignores it (observed): " + u2);
        check(u2 != base, "union returns new instance");
        // contains double overloads: right/bottom edge exclusive
        java.awt.Rectangle b = new java.awt.Rectangle(0, 0, 10, 10);
        check(!b.contains(10.0, 10.0), "contains(double) far corner exclusive");
        check(b.contains(9.999, 9.999), "contains(double) just inside");
        check(b.contains(1.0, 1.0, 5.0, 5.0), "contains(double,double,w,h) inner");
        check(!b.contains(1.0, 1.0, 9.5, 1.0), "contains(double rect) exceeding width");
        check(!b.contains(-0.001, 0.0, 5.0, 5.0), "contains(double rect) left out");
        check(b.contains(new java.awt.geom.Rectangle2D.Double(1, 1, 5, 5)), "contains(Rectangle2D)");
        check(!b.contains(new java.awt.geom.Rectangle2D.Double(1, 1, 10, 5)), "contains(Rectangle2D) too wide");
        // intersects double: touching edge is NOT intersecting
        check(!b.intersects(10.0, 0.0, 5.0, 5.0), "intersects(double) touching edge false");
        check(b.intersects(5.0, 5.0, 10.0, 10.0), "intersects(double) overlapping");
        check(b.intersects(new java.awt.geom.Rectangle2D.Double(5, 5, 10, 10)), "intersects(Rectangle2D)");
        check(!b.intersects(100.0, 100.0, 5.0, 5.0), "intersects(double) disjoint");
        // contains(int) family
        check(b.contains(0, 0), "contains(int) origin inclusive");
        check(!b.contains(10, 0), "contains(int) right edge exclusive");
        check(b.contains(2, 2, 5, 5), "contains(int,int,w,h)");
        check(!b.contains(2, 2, 9, 9), "contains(int rect) too big");
        // grow negative shrinks
        java.awt.Rectangle g = new java.awt.Rectangle(10, 10, 20, 20);
        g.grow(-5, -3);
        check(g.x == 15 && g.y == 13 && g.width == 10 && g.height == 14, "grow negative shrinks: " + g);
        // setBounds(Point,Dimension) + setSize(Dimension) + resize/setLocation aliases
        java.awt.Rectangle sb = new java.awt.Rectangle();
        java.awt.Rectangle from = new java.awt.Rectangle(1, 2, 3, 4);
        sb.setBounds(from); from.x = 99;
        check(sb.x == 1 && sb.y == 2 && sb.width == 3 && sb.height == 4, "setBounds(Rectangle) copies values");
        sb.reshape(5, 6, 7, 8);
        check(sb.x == 5 && sb.y == 6 && sb.width == 7 && sb.height == 8, "reshape alias");
        java.awt.Dimension shared = new java.awt.Dimension(30, 40);
        sb.setSize(shared); shared.width = 1;
        check(sb.width == 30, "setSize(Dimension) copies");
        sb.resize(50, 60);
        check(sb.width == 50 && sb.height == 60, "resize alias");
        sb.setLocation(new java.awt.Point(-1, -2));
        check(sb.x == -1 && sb.y == -2, "setLocation(Point)");
        sb.move(-3, -4);
        check(sb.x == -3 && sb.y == -4, "move alias");
        // outcode corner masks
        java.awt.Rectangle oc = new java.awt.Rectangle(0, 0, 10, 10);
        check(oc.outcode(-1.0, -1.0) == (java.awt.geom.Rectangle2D.OUT_LEFT | java.awt.geom.Rectangle2D.OUT_TOP),
              "outcode top-left combo");
        check(oc.outcode(11.0, 11.0) == (java.awt.geom.Rectangle2D.OUT_RIGHT | java.awt.geom.Rectangle2D.OUT_BOTTOM),
              "outcode bottom-right combo");
        check(oc.outcode(-1.0, 5.0) == java.awt.geom.Rectangle2D.OUT_LEFT, "outcode left only");
        // serialization round-trip + uid + toString
        java.awt.Rectangle rt = serRect(new java.awt.Rectangle(-1, -2, 30, 40));
        check(rt.x == -1 && rt.y == -2 && rt.width == 30 && rt.height == 40, "serialization round-trip");
        check(java.io.ObjectStreamClass.lookup(java.awt.Rectangle.class).getSerialVersionUID()
              == -4345857070255674764L, "serialVersionUID");
        check(rt.toString().equals("java.awt.Rectangle[x=-1,y=-2,width=30,height=40]"), "toString after round-trip");
        // Rectangle does not override hashCode (identity)
        boolean hasHash = false;
        try { java.awt.Rectangle.class.getDeclaredMethod("hashCode"); hasHash = true; } catch (NoSuchMethodException e) { }
        check(!hasHash, "Rectangle does not override hashCode");
        // equals vs Rectangle2D cross-implementation
        check(new java.awt.Rectangle(1, 2, 3, 4).equals(new java.awt.geom.Rectangle2D.Double(1, 2, 3, 4)),
              "Rectangle equals Rectangle2D.Double");
        check(new java.awt.geom.Rectangle2D.Double(1, 2, 3, 4).equals(new java.awt.Rectangle(1, 2, 3, 4)),
              "Rectangle2D.Double equals Rectangle (symmetric)");
        check(!new java.awt.Rectangle(1, 2, 3, 4).equals(new java.awt.geom.Rectangle2D.Float(1, 2, 3, 4.5f)),
              "different height not equal");
        // translate keeps size
        java.awt.Rectangle tr = new java.awt.Rectangle(1, 2, 3, 4);
        tr.translate(-10, 10);
        check(tr.x == -9 && tr.y == 12 && tr.width == 3 && tr.height == 4, "translate keeps size");
    }

}
