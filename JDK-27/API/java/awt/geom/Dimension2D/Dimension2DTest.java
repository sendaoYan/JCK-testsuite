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

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String m) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

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

    // ===================== 契约深化：setSize(null)/自赋值/零值/NaN/±Infinity/clone 独立性 =====================
    static void testDimensionContracts() {
        // setSize(Dimension2D) 传 null：内部调用 d.getWidth() 触发 NPE（只断言类型）。
        checkThrows(NullPointerException.class, () -> new Dim(1, 1).setSize((Dimension2D) null), "setSize(null) NPE");

        // 自赋值：源与目标同对象时值不变。
        Dim self = new Dim(9, 8);
        self.setSize(self);
        check(self.getWidth() == 9.0 && self.getHeight() == 8.0, "setSize(self) no-op");

        // 零宽高：Dimension2D 抽象层不归一化，原样保留。
        Dim zero = new Dim(0, 0);
        check(zero.getWidth() == 0.0 && zero.getHeight() == 0.0, "zero size preserved");

        // NaN / ±Infinity 宽高：用位模式/数值范围断言（不 toString）。
        Dim nan = new Dim(0, 0);
        nan.setSize(Double.NaN, Double.POSITIVE_INFINITY);
        check(Double.isNaN(nan.getWidth()), "NaN width preserved");
        check(nan.getHeight() == Double.POSITIVE_INFINITY, "+Infinity height preserved");
        nan.setSize(Double.NEGATIVE_INFINITY, -1.0);
        check(nan.getWidth() == Double.NEGATIVE_INFINITY, "-Infinity width preserved");
        check(nan.getHeight() == -1.0, "negative height preserved");

        // clone 独立性：改 clone 不影响原对象；clone 与原对象值相等但非同一对象。
        Dim src = new Dim(5, 6);
        Dimension2D cl = (Dimension2D) src.clone();
        check(cl != src, "clone is a distinct object");
        check(cl.getWidth() == 5.0 && cl.getHeight() == 6.0, "clone copies values");
        cl.setSize(100, 200);
        check(src.getWidth() == 5.0 && src.getHeight() == 6.0, "mutating clone leaves original");

        // setSize(Dimension2D) 跨实现：java.awt.Dimension 也是 Dimension2D。
        Dim cross = new Dim(0, 0);
        cross.setSize(new java.awt.Dimension(7, 8));
        check(cross.getWidth() == 7.0 && cross.getHeight() == 8.0, "setSize(java.awt.Dimension) cross-impl");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDimensionContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
