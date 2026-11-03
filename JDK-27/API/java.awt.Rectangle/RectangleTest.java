/*
 * @test
 * @summary java.awt.Rectangle conformance: white-box structure + black-box behavior.
 * @run main RectangleTest
 */

import java.lang.reflect.*;

public class RectangleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Rectangle", false, RectangleTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Rectangle");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(!Modifier.isAbstract(m), "concrete class");
        check(!c.isInterface(), "not an interface");
        check(c.getEnumConstants() == null, "non-enum");
        check(c.getSuperclass() != null, "has superclass");
        check(c.getSuperclass().getName().equals("java.awt.geom.Rectangle2D"),
              "extends java.awt.geom.Rectangle2D");
        check(java.awt.Shape.class.isAssignableFrom(c), "implements java.awt.Shape");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");

        // public mutable int fields
        check(c.getField("x").getType() == int.class, "public int x");
        check(c.getField("y").getType() == int.class, "public int y");
        check(c.getField("width").getType() == int.class, "public int width");
        check(c.getField("height").getType() == int.class, "public int height");

        // constructors
        check(c.getConstructor() != null, "ctor ()");
        check(c.getConstructor(java.awt.Rectangle.class) != null, "ctor (Rectangle)");
        check(c.getConstructor(int.class, int.class, int.class, int.class) != null, "ctor (int,int,int,int)");
        check(c.getConstructor(int.class, int.class) != null, "ctor (int,int)");
        check(c.getConstructor(java.awt.Point.class, java.awt.Dimension.class) != null, "ctor (Point,Dimension)");
        check(c.getConstructor(java.awt.Point.class) != null, "ctor (Point)");
        check(c.getConstructor(java.awt.Dimension.class) != null, "ctor (Dimension)");

        // key methods
        for (String name : new String[] {"getX", "getY", "getWidth", "getHeight",
                "getBounds", "getBounds2D", "setBounds", "setRect", "getLocation",
                "setLocation", "translate", "getSize", "setSize", "contains",
                "intersects", "intersection", "union", "add", "grow", "isEmpty",
                "outcode", "equals", "toString"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(mm -> mm.getName().equals(name)), "method present: " + name);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // constructors
        java.awt.Rectangle r0 = new java.awt.Rectangle();
        check(r0.x == 0 && r0.y == 0 && r0.width == 0 && r0.height == 0, "default ctor 0,0,0,0");
        java.awt.Rectangle r = new java.awt.Rectangle(1, 2, 3, 4);
        check(r.x == 1 && r.y == 2 && r.width == 3 && r.height == 4, "(x,y,w,h) ctor");
        java.awt.Rectangle wh = new java.awt.Rectangle(7, 8);
        check(wh.x == 0 && wh.y == 0 && wh.width == 7 && wh.height == 8, "(w,h) ctor origin 0,0");
        java.awt.Rectangle copy = new java.awt.Rectangle(r);
        check(copy.equals(r), "copy ctor equal");
        copy.x = 99;
        check(r.x == 1, "copy ctor independent");
        java.awt.Rectangle pd = new java.awt.Rectangle(new java.awt.Point(10, 20), new java.awt.Dimension(30, 40));
        check(pd.x == 10 && pd.y == 20 && pd.width == 30 && pd.height == 40, "(Point,Dimension) ctor");
        java.awt.Rectangle pp = new java.awt.Rectangle(new java.awt.Point(5, 6));
        check(pp.x == 5 && pp.y == 6 && pp.width == 0 && pp.height == 0, "(Point) ctor");
        java.awt.Rectangle dd = new java.awt.Rectangle(new java.awt.Dimension(11, 12));
        check(dd.x == 0 && dd.y == 0 && dd.width == 11 && dd.height == 12, "(Dimension) ctor");

        // double view
        check(r.getX() == 1.0 && r.getY() == 2.0 && r.getWidth() == 3.0 && r.getHeight() == 4.0,
              "getX/getY/getWidth/getHeight double view");

        // getBounds returns independent copy
        java.awt.Rectangle b = r.getBounds();
        check(b.x == 1 && b.y == 2 && b.width == 3 && b.height == 4, "getBounds values");
        b.width = 100;
        check(r.width == 3, "getBounds independent");
        check(r.getBounds2D().getX() == 1.0 && r.getBounds2D().getWidth() == 3.0,
              "getBounds2D Rectangle2D view");

        // setBounds
        r.setBounds(new java.awt.Rectangle(5, 6, 7, 8));
        check(r.x == 5 && r.y == 6 && r.width == 7 && r.height == 8, "setBounds(Rectangle)");
        java.awt.Rectangle arg = new java.awt.Rectangle(1, 1, 1, 1);
        r.setBounds(arg);
        arg.width = 999;
        check(r.width == 1, "setBounds(Rectangle) copies not aliases");
        r.setBounds(10, 20, 30, 40);
        check(r.x == 10 && r.y == 20 && r.width == 30 && r.height == 40, "setBounds(int,int,int,int)");

        // setRect (double)
        r.setRect(5.0, 6.0, 7.0, 8.0);
        check(r.x == 5 && r.y == 6 && r.width == 7 && r.height == 8, "setRect exact doubles");
        r.setRect(10.9, 20.1, 5.0, 6.0);
        check(r.x == 10 && r.y == 20 && r.width == 6 && r.height == 7,
              "setRect floors origin and ceils far edge: " + r);

        // reshape / move / resize / translate / grow
        r.reshape(1, 2, 3, 4);
        check(r.x == 1 && r.y == 2 && r.width == 3 && r.height == 4, "reshape");
        r.move(11, 22);
        check(r.x == 11 && r.y == 22, "move(int,int)");
        r.translate(4, 5);
        check(r.x == 15 && r.y == 27, "translate(int,int)");
        r.setSize(new java.awt.Dimension(6, 7));
        check(r.width == 6 && r.height == 7, "setSize(Dimension)");
        r.resize(8, 9);
        check(r.width == 8 && r.height == 9, "resize(int,int)");
        r.setLocation(new java.awt.Point(3, 4));
        check(r.x == 3 && r.y == 4, "setLocation(Point)");
        java.awt.Point loc = r.getLocation();
        check(loc.x == 3 && loc.y == 4, "getLocation values");
        r.grow(2, 3);
        check(r.x == 1 && r.y == 1 && r.width == 12 && r.height == 15, "grow expands: " + r);

        // contains (point)
        java.awt.Rectangle box = new java.awt.Rectangle(10, 10, 10, 10);
        check(box.contains(10, 10), "contains top-left corner");
        check(box.contains(19, 19), "contains inside near far corner");
        check(!box.contains(20, 20), "right/bottom edge exclusive");
        check(!box.contains(9, 10), "outside left");
        check(box.contains(new java.awt.Point(15, 15)), "contains(Point)");
        check(box.inside(15, 15), "inside(int,int) delegates to contains");

        // contains (rect)
        check(box.contains(12, 12, 6, 6), "contains inner rect");
        check(box.contains(new java.awt.Rectangle(12, 12, 6, 6)), "contains(Rectangle)");
        check(!box.contains(12, 12, 6, 6) || box.contains(new java.awt.Rectangle(12, 12, 6, 6)), "rect contains consistency");
        check(!box.contains(0, 0, 30, 30), "does not contain larger rect");

        // intersects
        check(box.intersects(15, 15, 10, 10), "intersects overlapping");
        check(!box.intersects(30, 30, 10, 10), "no intersect disjoint");
        check(box.intersects(new java.awt.Rectangle(15, 15, 10, 10)), "intersects(Rectangle)");

        // intersection / union
        java.awt.Rectangle a = new java.awt.Rectangle(0, 0, 20, 20);
        java.awt.Rectangle c = new java.awt.Rectangle(10, 10, 20, 20);
        java.awt.Rectangle inter = a.intersection(c);
        check(inter.x == 10 && inter.y == 10 && inter.width == 10 && inter.height == 10,
              "intersection exact: " + inter);
        java.awt.Rectangle disjoint = new java.awt.Rectangle(100, 100, 5, 5);
        java.awt.Rectangle empty = a.intersection(disjoint);
        check(empty.width < 0 && empty.height < 0, "empty intersection has negative size: " + empty);
        java.awt.Rectangle u = a.union(c);
        check(u.x == 0 && u.y == 0 && u.width == 30 && u.height == 30, "union exact: " + u);

        // add point / add rect
        java.awt.Rectangle grower = new java.awt.Rectangle(0, 0, 10, 10);
        grower.add(15, 20);
        check(grower.x == 0 && grower.y == 0 && grower.width == 15 && grower.height == 20,
              "add(int,int) grows to include: " + grower);
        grower.add(new java.awt.Rectangle(50, 60, 0, 0));
        check(grower.width == 50 && grower.height == 60, "add(Rectangle) grows: " + grower);
        grower.add(new java.awt.Point(0, 0));
        check(grower.width == 50 && grower.height == 60, "add(Point) no-op when inside");

        // isEmpty / outcode
        check(new java.awt.Rectangle(0, 0, 0, 0).isEmpty(), "0x0 empty");
        check(new java.awt.Rectangle(0, 0, -1, 5).isEmpty(), "negative width empty");
        check(!new java.awt.Rectangle(0, 0, 5, 5).isEmpty(), "non-empty");
        check(box.outcode(15.0, 15.0) == 0, "outcode inside == 0");
        check(box.outcode(-1.0, 15.0) != 0, "outcode left nonzero");
        check(box.outcode(15.0, 100.0) != 0, "outcode below nonzero");

        // createIntersection / createUnion (Rectangle2D view)
        java.awt.geom.Rectangle2D ci = a.createIntersection(c);
        check(ci.getX() == 10.0 && ci.getY() == 10.0 && ci.getWidth() == 10.0 && ci.getHeight() == 10.0,
              "createIntersection exact");
        java.awt.geom.Rectangle2D cu = a.createUnion(c);
        check(cu.getX() == 0.0 && cu.getY() == 0.0 && cu.getWidth() == 30.0 && cu.getHeight() == 30.0,
              "createUnion exact");

        // equals / hashCode
        java.awt.Rectangle e1 = new java.awt.Rectangle(1, 2, 3, 4);
        java.awt.Rectangle e2 = new java.awt.Rectangle(1, 2, 3, 4);
        check(e1.equals(e2), "equal rectangles");
        check(e1.hashCode() == e2.hashCode(), "equal hashCodes");
        check(!e1.equals(new java.awt.Rectangle(1, 2, 3, 5)), "differing height not equal");
        check(!e1.equals(null), "not equal null");
        check(!e1.equals("1,2,3,4"), "not equal unrelated type");

        // toString
        check(r.toString().equals("java.awt.Rectangle[x=1,y=1,width=12,height=15]"),
              "toString format: " + r);

        // combination chain
        java.awt.Rectangle chain = new java.awt.Rectangle();
        chain.setBounds(0, 0, 100, 100);
        chain.translate(5, 5);
        check(chain.x == 5 && chain.y == 5 && chain.width == 100 && chain.height == 100,
              "translate shifts origin: " + chain);
        chain.grow(-5, -5);
        check(chain.x == 10 && chain.y == 10 && chain.width == 90 && chain.height == 90,
              "grow(-n) shrinks by n each side: " + chain);
        chain.add(200, 200);
        check(chain.width == 190 && chain.height == 190, "add(point) grows bounding box: " + chain);
        check(chain.contains(100, 100) && !chain.contains(200, 200),
              "contains after add respects right edge");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
