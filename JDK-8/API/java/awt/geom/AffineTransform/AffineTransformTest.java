/*
 * @test
 * @summary java.awt.geom.AffineTransform conformance: white-box structure plus black-box behavior.
 * @run main AffineTransformTest
 */

import java.awt.Shape;
import java.awt.geom.*;
import java.io.*;
import java.lang.reflect.*;

public class AffineTransformTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean close(double a, double b) {
        return Math.abs(a - b) < 1e-9;
    }

    static void checkClose(double a, double b, String msg) {
        check(close(a, b), msg + " (got " + a + ", want ~" + b + ")");
    }

    static Point2D.Double px(double x, double y) { return new Point2D.Double(x, y); }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.AffineTransform");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "AffineTransform is public");
        check(!c.isInterface(), "AffineTransform is a class, not an interface");
        check(!c.isEnum(), "AffineTransform is not an enum");
        check(c.getSuperclass() == Object.class, "AffineTransform extends Object");
        check(Cloneable.class.isAssignableFrom(c), "AffineTransform implements Cloneable");
        check(Serializable.class.isAssignableFrom(c), "AffineTransform implements Serializable");

        String[] typeNames = {
            "TYPE_IDENTITY", "TYPE_TRANSLATION", "TYPE_UNIFORM_SCALE", "TYPE_GENERAL_SCALE",
            "TYPE_FLIP", "TYPE_QUADRANT_ROTATION", "TYPE_GENERAL_ROTATION", "TYPE_GENERAL_TRANSFORM"
        };
        for (String n : typeNames) {
            Field f = c.getField(n);
            int fm = f.getModifiers();
            check(Modifier.isPublic(fm) && Modifier.isStatic(fm) && Modifier.isFinal(fm), "TYPE constant " + n + " public static final");
        }

        check(hasMethod(c, "getType"), "getType()");
        check(hasMethod(c, "getDeterminant"), "getDeterminant()");
        check(hasMethod(c, "concatenate", AffineTransform.class), "concatenate(AffineTransform)");
        check(hasMethod(c, "preConcatenate", AffineTransform.class), "preConcatenate(AffineTransform)");
        check(hasMethod(c, "createInverse"), "createInverse()");
        check(hasMethod(c, "invert"), "invert()");
        check(hasMethod(c, "transform", Point2D.class, Point2D.class), "transform(Point2D,Point2D)");
        check(hasMethod(c, "inverseTransform", Point2D.class, Point2D.class), "inverseTransform(Point2D,Point2D)");
        check(hasMethod(c, "deltaTransform", Point2D.class, Point2D.class), "deltaTransform(Point2D,Point2D)");
        check(hasMethod(c, "createTransformedShape", Shape.class), "createTransformedShape(Shape)");
        check(hasMethod(c, "getTranslateInstance", double.class, double.class), "getTranslateInstance");
        check(hasMethod(c, "getRotateInstance", double.class, double.class, double.class), "getRotateInstance(theta,px,py)");
        check(hasMethod(c, "getScaleInstance", double.class, double.class), "getScaleInstance");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. identity matrix
        AffineTransform id = new AffineTransform();
        check(id.isIdentity(), "default constructor identity");
        check(id.getType() == AffineTransform.TYPE_IDENTITY, "identity getType == TYPE_IDENTITY");
        checkClose(id.getScaleX(), 1.0, "identity scaleX");
        checkClose(id.getScaleY(), 1.0, "identity scaleY");
        checkClose(id.getShearX(), 0.0, "identity shearX");
        checkClose(id.getShearY(), 0.0, "identity shearY");
        checkClose(id.getTranslateX(), 0.0, "identity translateX");
        checkClose(id.getTranslateY(), 0.0, "identity translateY");

        // 2. constructors and component access
        AffineTransform t = new AffineTransform(1, 2, 3, 4, 5, 6); // (m00,m10,m01,m11,m02,m12)
        checkClose(t.getScaleX(), 1.0, "6-double ctor scaleX");
        checkClose(t.getScaleY(), 4.0, "6-double ctor scaleY");
        checkClose(t.getShearX(), 3.0, "6-double ctor shearX (m01)");
        checkClose(t.getShearY(), 2.0, "6-double ctor shearY (m10)");
        checkClose(t.getTranslateX(), 5.0, "6-double ctor translateX");
        checkClose(t.getTranslateY(), 6.0, "6-double ctor translateY");

        double[] mat = new double[6];
        t.getMatrix(mat);
        check(close(mat[0], 1) && close(mat[1], 2) && close(mat[2], 3)
              && close(mat[3], 4) && close(mat[4], 5) && close(mat[5], 6),
              "getMatrix fills {m00,m10,m01,m11,m02,m12}");

        AffineTransform f6 = new AffineTransform(new float[]{1, 2, 3, 4, 5, 6});
        check(close(f6.getTranslateX(), 5.0) && close(f6.getTranslateY(), 6.0), "float[6] ctor");
        AffineTransform d6 = new AffineTransform(new double[]{1, 2, 3, 4, 5, 6});
        check(close(d6.getTranslateX(), 5.0) && close(d6.getTranslateY(), 6.0), "double[6] ctor");
        AffineTransform cp = new AffineTransform(t);
        check(cp.equals(t), "copy ctor equals");
        check(cp != t, "copy ctor produces distinct object");

        // 3. static factory family with point mapping + getType
        AffineTransform tr = AffineTransform.getTranslateInstance(10, 20);
        checkClose(tr.getTranslateX(), 10.0, "getTranslateInstance x");
        checkClose(tr.getTranslateY(), 20.0, "getTranslateInstance y");
        check(tr.getType() == AffineTransform.TYPE_TRANSLATION, "translate getType == TYPE_TRANSLATION");

        AffineTransform us = AffineTransform.getScaleInstance(2.5, 2.5);
        check(us.getType() == AffineTransform.TYPE_UNIFORM_SCALE, "uniform scale getType");
        Point2D pu = us.transform(px(2, 3), null);
        checkClose(pu.getX(), 5.0, "uniform scale x");
        checkClose(pu.getY(), 7.5, "uniform scale y");

        AffineTransform gs = AffineTransform.getScaleInstance(2, 3);
        check(gs.getType() == AffineTransform.TYPE_GENERAL_SCALE, "general scale getType");
        Point2D pg = gs.transform(px(2, 3), null);
        checkClose(pg.getX(), 4.0, "general scale x");
        checkClose(pg.getY(), 9.0, "general scale y");

        AffineTransform sh = AffineTransform.getShearInstance(1, 0.5);
        Point2D psh = sh.transform(px(1, 2), null);
        checkClose(psh.getX(), 3.0, "shear x (x + shx*y)");
        checkClose(psh.getY(), 2.5, "shear y (shy*x + y)");

        AffineTransform r90 = AffineTransform.getRotateInstance(Math.PI / 2);
        Point2D pr = r90.transform(px(3, 4), null);
        checkClose(pr.getX(), -4.0, "rotate pi/2 x");
        checkClose(pr.getY(), 3.0, "rotate pi/2 y");
        check(r90.getType() == AffineTransform.TYPE_QUADRANT_ROTATION, "rotate pi/2 getType == QUADRANT_ROTATION");
        check(AffineTransform.getRotateInstance(Math.PI / 3).getType() == AffineTransform.TYPE_GENERAL_ROTATION,
              "rotate pi/3 getType == GENERAL_ROTATION");

        AffineTransform rp = AffineTransform.getRotateInstance(Math.PI / 2, 1, 1); // rotate about (1,1)
        Point2D prp = rp.transform(px(1, 1), null);
        checkClose(prp.getX(), 1.0, "rotate about pivot keeps pivot x");
        checkClose(prp.getY(), 1.0, "rotate about pivot keeps pivot y");

        AffineTransform rv = AffineTransform.getRotateInstance(1, 0);
        Point2D prv = rv.transform(px(1, 0), null);
        checkClose(prv.getX(), 1.0, "rotate(vec (1,0)) x");
        checkClose(prv.getY(), 0.0, "rotate(vec (1,0)) y");

        AffineTransform qr = AffineTransform.getQuadrantRotateInstance(3);
        Point2D pqr = qr.transform(px(3, 4), null);
        checkClose(pqr.getX(), 4.0, "quadrantRotate(3) x");
        checkClose(pqr.getY(), -3.0, "quadrantRotate(3) y");
        check(qr.getType() == AffineTransform.TYPE_QUADRANT_ROTATION, "quadrantRotate getType");

        AffineTransform flip = AffineTransform.getScaleInstance(-2, 2);
        check(flip.getType() == 66, "scale(-2,2) getType == FLIP|UNIFORM_SCALE (66)");
        checkClose(flip.getDeterminant(), -4.0, "scale(-2,2) determinant");

        // 4. setToXxx resets state
        AffineTransform dirty = new AffineTransform(3, 4, 5, 6, 7, 8);
        dirty.setToIdentity();
        check(dirty.isIdentity(), "setToIdentity");
        dirty.setToTranslation(5, 7);
        checkClose(dirty.getTranslateX(), 5.0, "setToTranslation x");
        checkClose(dirty.getTranslateY(), 7.0, "setToTranslation y");
        dirty.setToScale(2, 3);
        check(dirty.getType() == AffineTransform.TYPE_GENERAL_SCALE, "setToScale resets to general scale");
        dirty.setToRotation(Math.PI / 2);
        Point2D pd = dirty.transform(px(1, 0), null);
        checkClose(pd.getX(), 0.0, "setToRotation x");
        checkClose(pd.getY(), 1.0, "setToRotation y");
        dirty.setToQuadrantRotation(2);
        Point2D pdq = dirty.transform(px(3, 4), null);
        checkClose(pdq.getX(), -3.0, "setToQuadrantRotation(2) x");
        checkClose(pdq.getY(), -4.0, "setToQuadrantRotation(2) y");
        dirty.setToShear(0.5, 1.0);
        Point2D pds = dirty.transform(px(1, 2), null);
        checkClose(pds.getX(), 2.0, "setToShear x");
        checkClose(pds.getY(), 3.0, "setToShear y");

        // 5. concatenate vs preConcatenate
        AffineTransform A = AffineTransform.getTranslateInstance(10, 0);
        AffineTransform B = AffineTransform.getScaleInstance(2, 2);
        AffineTransform c1 = new AffineTransform(A);
        c1.concatenate(B);
        Point2D pc1 = c1.transform(px(1, 1), null);
        checkClose(pc1.getX(), 12.0, "concatenate: translate then scale x");
        checkClose(pc1.getY(), 2.0, "concatenate: translate then scale y");
        AffineTransform c2 = new AffineTransform(A);
        c2.preConcatenate(B);
        Point2D pc2 = c2.transform(px(1, 1), null);
        checkClose(pc2.getX(), 22.0, "preConcatenate: scale then translate x");
        checkClose(pc2.getY(), 2.0, "preConcatenate: scale then translate y");

        AffineTransform st = new AffineTransform();
        st.setTransform(A);
        check(st.equals(A), "setTransform(AffineTransform)");
        st.setTransform(2, 0, 0, 3, 0, 0);
        check(st.equals(AffineTransform.getScaleInstance(2, 3)), "setTransform(6 doubles)");

        // 6. inverse
        AffineTransform sc23 = AffineTransform.getScaleInstance(2, 3);
        AffineTransform inv = sc23.createInverse();
        Point2D pi = inv.transform(px(2, 3), null);
        checkClose(pi.getX(), 1.0, "createInverse maps (2,3)->(1,1) x");
        checkClose(pi.getY(), 1.0, "createInverse maps (2,3)->(1,1) y");
        AffineTransform roundTrip = (AffineTransform) sc23.clone();
        roundTrip.concatenate(inv);
        check(roundTrip.isIdentity(), "at . inverse == identity");

        AffineTransform invInPlace = AffineTransform.getTranslateInstance(5, 7);
        invInPlace.invert();
        Point2D pii = invInPlace.transform(px(5, 7), null);
        checkClose(pii.getX(), 0.0, "invert in place x");
        checkClose(pii.getY(), 0.0, "invert in place y");

        AffineTransform singular = AffineTransform.getScaleInstance(0, 1);
        boolean threwCreate = false;
        try { singular.createInverse(); } catch (NoninvertibleTransformException e) { threwCreate = true; }
        check(threwCreate, "createInverse on singular throws NoninvertibleTransformException");
        boolean threwInvert = false;
        try { singular.invert(); } catch (NoninvertibleTransformException e) { threwInvert = true; }
        check(threwInvert, "invert on singular throws NoninvertibleTransformException");

        checkClose(AffineTransform.getTranslateInstance(1, 2).getDeterminant(), 1.0, "translate determinant");
        checkClose(sc23.getDeterminant(), 6.0, "scale(2,3) determinant");
        checkClose(singular.getDeterminant(), 0.0, "singular determinant");
        checkClose(AffineTransform.getRotateInstance(Math.PI / 3).getDeterminant(), 1.0, "rotate determinant ~1");

        // 7. point/vector transform APIs
        AffineTransform sc2 = AffineTransform.getScaleInstance(2, 2);
        Point2D src = new Point2D.Double(1, 2);
        Point2D allocated = sc2.transform(src, null);
        check(allocated != null && allocated != src, "transform allocates new Point2D when dst null");
        checkClose(allocated.getX(), 2.0, "transform null-dst x");
        checkClose(allocated.getY(), 4.0, "transform null-dst y");
        Point2D reused = new Point2D.Double();
        Point2D same = sc2.transform(src, reused);
        check(same == reused, "transform reuses provided dst");
        checkClose(reused.getX(), 2.0, "transform provided-dst x");

        Point2D[] pts = { px(1, 2), px(3, 4) };
        Point2D[] out = new Point2D[2];
        sc2.transform(pts, 0, out, 0, 2);
        checkClose(out[0].getX(), 2.0, "Point2D[] batch x0");
        checkClose(out[1].getY(), 8.0, "Point2D[] batch y1");

        double[] din = {1, 2, 3, 4};
        double[] dout = new double[4];
        sc2.transform(din, 0, dout, 0, 2);
        checkClose(dout[0], 2.0, "double[] batch x0");
        checkClose(dout[3], 8.0, "double[] batch y1");
        sc2.transform(din, 0, din, 0, 2); // in-place
        checkClose(din[0], 2.0, "double[] in-place x0");
        checkClose(din[1], 4.0, "double[] in-place y0");

        AffineTransform tr57 = AffineTransform.getTranslateInstance(5, 7);
        Point2D pi2 = tr57.inverseTransform(px(1, 2), null);
        checkClose(pi2.getX(), -4.0, "inverseTransform Point2D x");
        checkClose(pi2.getY(), -5.0, "inverseTransform Point2D y");

        Point2D pdelta = tr57.deltaTransform(px(1, 2), null);
        checkClose(pdelta.getX(), 1.0, "deltaTransform ignores translation x");
        checkClose(pdelta.getY(), 2.0, "deltaTransform ignores translation y");
        Point2D pdelta2 = sc23.deltaTransform(px(2, 3), null);
        checkClose(pdelta2.getX(), 4.0, "deltaTransform applies scale x");
        checkClose(pdelta2.getY(), 9.0, "deltaTransform applies scale y");

        // 8. createTransformedShape, clone, equals/hashCode, toString, serialization
        Rectangle2D rect = new Rectangle2D.Double(0, 0, 10, 20);
        Shape shaped = AffineTransform.getTranslateInstance(5, 5).createTransformedShape(rect);
        Rectangle2D b = shaped.getBounds2D();
        checkClose(b.getX(), 5.0, "createTransformedShape bounds x");
        checkClose(b.getY(), 5.0, "createTransformedShape bounds y");
        checkClose(b.getWidth(), 10.0, "createTransformedShape bounds width");
        checkClose(b.getHeight(), 20.0, "createTransformedShape bounds height");

        AffineTransform cloned = (AffineTransform) sc23.clone();
        check(cloned != sc23 && cloned.equals(sc23), "clone independent but equal");
        cloned.scale(2, 2);
        check(!cloned.equals(sc23), "mutating clone does not affect original");

        AffineTransform sameMat = AffineTransform.getScaleInstance(2, 3);
        check(sc23.equals(sameMat), "equal matrices equal");
        check(sc23.hashCode() == sameMat.hashCode(), "equal matrices same hashCode");
        check(!sc23.equals(AffineTransform.getScaleInstance(3, 3)), "different matrices not equal");
        check(!sc23.equals(null), "equals(null) false");
        check(!sc23.equals("2,3"), "equals(non-AT) false");

        check("AffineTransform[[1.0, 0.0, 0.0], [0.0, 1.0, 0.0]]".equals(new AffineTransform().toString()),
              "identity toString exact");
        check("AffineTransform[[1.0, 0.0, 10.0], [0.0, 1.0, 20.0]]".equals(tr.toString()),
              "translate toString exact");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(tr);
        AffineTransform deser = (AffineTransform) new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(deser.equals(tr), "serialization round-trip equals");
        check(deser.getTranslateX() == 10.0 && deser.getTranslateY() == 20.0, "serialization round-trip components");

        // 9. numeric boundaries: NaN / infinity
        AffineTransform nanT = AffineTransform.getScaleInstance(Double.NaN, 1.0);
        check(nanT.getType() == (AffineTransform.TYPE_FLIP | AffineTransform.TYPE_GENERAL_SCALE),
              "NaN matrix getType == FLIP|GENERAL_SCALE (68), not GENERAL_TRANSFORM");
        Point2D pnan = nanT.transform(px(1, 1), null);
        check(Double.isNaN(pnan.getX()) && pnan.getY() == 1.0, "NaN component propagates NaN through transform");
        // determinant NaN is still "invertible" for this implementation (no exception thrown)
        AffineTransform nanInv = nanT.createInverse();
        check(Double.isNaN(nanInv.getScaleX()), "createInverse on NaN matrix succeeds with NaN entries");
        AffineTransform infT = AffineTransform.getTranslateInstance(Double.POSITIVE_INFINITY, 0.0);
        check(infT.getType() == AffineTransform.TYPE_TRANSLATION, "infinite translation still TYPE_TRANSLATION");
        check(!infT.isIdentity(), "infinite translation not identity");
        // JDK-8 compares matrix entries with ==, so NaN != NaN and two NaN matrices are NOT equal
        // (JDK-9+ switched to a bitwise comparison where they are equal — covered on the JDK-27 side)
        check(!nanT.equals(AffineTransform.getScaleInstance(Double.NaN, 1.0)),
              "equals on identical NaN matrices is false in JDK-8 (== comparison)");

        // 10. remaining vector-rotation overloads
        AffineTransform rvP = AffineTransform.getRotateInstance(1, 0, 5, 5); // unit vector, pivot
        Point2D prvp = rvP.transform(px(5, 5), null);
        checkClose(prvp.getX(), 5.0, "getRotateInstance(vec,px,py) keeps pivot x");
        checkClose(prvp.getY(), 5.0, "getRotateInstance(vec,px,py) keeps pivot y");
        AffineTransform r90v = AffineTransform.getRotateInstance(0, 1); // rotate (1,0)->(0,1)
        Point2D pr90v = r90v.transform(px(1, 0), null);
        checkClose(pr90v.getX(), 0.0, "getRotateInstance(0,1) maps (1,0) x to 0");
        checkClose(pr90v.getY(), 1.0, "getRotateInstance(0,1) maps (1,0) y to 1");
        AffineTransform sr = new AffineTransform();
        sr.setToRotation(0, 1);
        check(sr.getType() == AffineTransform.TYPE_QUADRANT_ROTATION, "setToRotation(vec) quadrant type");
        checkClose(AffineTransform.getRotateInstance(3, 4).getDeterminant(), 1.0,
                   "getRotateInstance(vec 3,4) unit-scale determinant 1");
        AffineTransform qrP = AffineTransform.getQuadrantRotateInstance(1, 2, 3);
        Point2D pqrP = qrP.transform(px(2, 3), null);
        checkClose(pqrP.getX(), 2.0, "getQuadrantRotateInstance(k,px,py) keeps pivot x");
        checkClose(pqrP.getY(), 3.0, "getQuadrantRotateInstance(k,px,py) keeps pivot y");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
