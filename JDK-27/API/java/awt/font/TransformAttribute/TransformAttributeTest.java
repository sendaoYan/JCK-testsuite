/*
 * @test
 * @summary java.awt.font.TransformAttribute conformance: white-box structure plus black-box behavior.
 * @run main TransformAttributeTest
 */

import java.awt.font.TransformAttribute;
import java.awt.geom.AffineTransform;
import java.io.Serializable;
import java.lang.reflect.*;

public class TransformAttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.font.TransformAttribute");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final class");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(hasMethod(c, "getTransform"), "getTransform()");
        check(hasMethod(c, "isIdentity"), "isIdentity()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(TransformAttribute.IDENTITY.isIdentity(), "IDENTITY isIdentity");
        check(TransformAttribute.IDENTITY.getTransform().isIdentity(), "IDENTITY transform identity");

        AffineTransform tx = AffineTransform.getScaleInstance(2, 3);
        TransformAttribute ta = new TransformAttribute(tx);
        check(!ta.isIdentity(), "scaled not identity");
        check(ta.getTransform().equals(tx), "getTransform equals source");
        // getTransform returns a defensive copy
        AffineTransform t1 = ta.getTransform();
        AffineTransform t2 = ta.getTransform();
        check(t1 != t2 && t1.equals(t2), "getTransform returns independent copies");

        // identity passed explicitly is treated as IDENTITY singleton
        check(new TransformAttribute(new AffineTransform()).isIdentity(), "explicit identity isIdentity");

        // equals / hashCode
        TransformAttribute same = new TransformAttribute(AffineTransform.getScaleInstance(2, 3));
        check(ta.equals(same) && ta.hashCode() == same.hashCode(), "equal attributes equal");
        check(!ta.equals(TransformAttribute.IDENTITY), "scaled != IDENTITY");
        check(!ta.equals(null) && !ta.equals("x"), "equals(null)/non-TA false");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
