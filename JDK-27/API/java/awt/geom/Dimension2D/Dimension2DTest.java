/*
 * @test
 * @summary java.awt.geom.Dimension2D conformance: white-box structure plus black-box behavior.
 * @run main Dimension2DTest
 */

import java.awt.geom.Dimension2D;
import java.lang.reflect.*;

public class Dimension2DTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.Dimension2D");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isAbstract(m), "abstract public class");
        check(!c.isInterface() && !c.isEnum(), "class, not interface/enum");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "getWidth"), "getWidth()");
        check(hasMethod(c, "getHeight"), "getHeight()");
        check(hasMethod(c, "setSize", double.class, double.class), "setSize(double,double)");
        check(hasMethod(c, "setSize", Dimension2D.class), "setSize(Dimension2D)");
        check(hasMethod(c, "clone"), "clone()");
    }

    // concrete anonymous subclass
    static class Dim extends Dimension2D {
        double w, h;
        Dim(double w, double h) { this.w = w; this.h = h; }
        public double getWidth() { return w; }
        public double getHeight() { return h; }
        public void setSize(double w, double h) { this.w = w; this.h = h; }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Dim d = new Dim(10, 20);
        check(d.getWidth() == 10.0 && d.getHeight() == 20.0, "ctor + getWidth/getHeight");
        d.setSize(30, 40);
        check(d.getWidth() == 30.0 && d.getHeight() == 40.0, "setSize(double,double)");

        Dim other = new Dim(5, 6);
        d.setSize(other);
        check(d.getWidth() == 5.0 && d.getHeight() == 6.0, "setSize(Dimension2D) copies width/height");
        other.setSize(99, 99);
        check(d.getWidth() == 5.0 && d.getHeight() == 6.0, "setSize(Dimension2D) copies by value");

        Dimension2D cl = (Dimension2D) d.clone();
        check(cl != d && cl.getWidth() == 5.0 && cl.getHeight() == 6.0, "clone independent but equal");

        // negative sizes are legal in Dimension2D (no normalization at this abstraction level)
        Dim neg = new Dim(-3, -4);
        check(neg.getWidth() == -3.0 && neg.getHeight() == -4.0, "negative size preserved");

        // cross-implementation semantics with java.awt.Dimension:
        // java.awt.Dimension.equals only accepts java.awt.Dimension, not arbitrary Dimension2D
        java.awt.Dimension awtD = new java.awt.Dimension(3, 4);
        check(awtD.equals(new Dim(3, 4)) == false, "java.awt.Dimension.equals(Dimension2D) is false (type-checked)");
        check(awtD.equals(new java.awt.Dimension(3, 4)), "java.awt.Dimension.equals(Dimension) value-based");
        check(awtD.hashCode() == new java.awt.Dimension(3, 4).hashCode(), "equal Dimensions share hashCode");

        // java.awt.Dimension.setSize(double,double) rounds by ceiling to int fields
        java.awt.Dimension frac = new java.awt.Dimension();
        frac.setSize(2.6, 3.2);
        check(frac.width == 3 && frac.height == 4, "java.awt.Dimension.setSize(double) ceils to int");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
