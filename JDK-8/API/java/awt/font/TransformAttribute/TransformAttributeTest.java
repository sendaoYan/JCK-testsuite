/*
 * @test
 * @summary java.awt.font.TransformAttribute conformance: transform wrapper.
 * @run main TransformAttributeTest
 */


import java.awt.font.TransformAttribute;
import java.awt.geom.AffineTransform;
public class TransformAttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        TransformAttribute identity = new TransformAttribute(new AffineTransform());
        check(identity.isIdentity(), "identity");
        TransformAttribute scaled = new TransformAttribute(new AffineTransform(2, 0, 0, 2, 0, 0));
        check(!scaled.isIdentity(), "not identity");
        check(scaled.getTransform().getScaleX() == 2.0, "scale");
        check(TransformAttribute.IDENTITY.isIdentity(), "IDENTITY constant");
        check(identity.equals(TransformAttribute.IDENTITY), "equals IDENTITY");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
