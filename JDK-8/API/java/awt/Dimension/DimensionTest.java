/*
 * @test
 * @summary java.awt.Dimension conformance: width/height and copy.
 * @run main DimensionTest
 */


import java.awt.Dimension;
public class DimensionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Dimension d = new Dimension(30, 40);
        check(d.width == 30 && d.height == 40, "width/height fields");
        check(d.getWidth() == 30.0 && d.getHeight() == 40.0, "getWidth/getHeight");
        Dimension copy = new Dimension(d);
        check(copy.width == 30 && copy.height == 40, "copy ctor");
        d.setSize(50, 60);
        check(d.getSize().width == 50 && d.getSize().height == 60, "setSize");
        check(d.equals(new Dimension(50, 60)), "equals");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static java.awt.Dimension serDim(java.awt.Dimension d) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(d);
        return (java.awt.Dimension) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void testContract() throws Exception {
        java.awt.Dimension d = new java.awt.Dimension();
        // constructors
        check(d.width == 0 && d.height == 0, "default 0x0");
        java.awt.Dimension d5 = new java.awt.Dimension(new java.awt.Dimension(7, 9));
        check(d5.width == 7 && d5.height == 9, "copy ctor");
        d5.width = 100;
        check(new java.awt.Dimension(7, 9).width == 7, "copy ctor independent source");
        // setSize(int,int) / setSize(Dimension) / setSize(double,double) CEILS (unlike Point rounding)
        d.setSize(3, 4);        check(d.width == 3 && d.height == 4, "setSize(int,int)");
        d.setSize(new java.awt.Dimension(8, 2)); check(d.width == 8 && d.height == 2, "setSize(Dimension)");
        d.setSize(10.2, 20.0);  check(d.width == 11 && d.height == 20, "setSize(double) ceils 10.2->11");
        d.setSize(10.0, 20.9);  check(d.width == 10 && d.height == 21, "setSize(double) ceils 20.9->21");
        d.setSize(-3.7, -4.2);  check(d.width == -3 && d.height == -4, "setSize(double) ceil toward zero for negatives: " + d);
        // getSize returns independent copy
        java.awt.Dimension got = d.getSize();
        check(got.width == d.width, "getSize equal");
        got.width = 555;
        check(d.width == -3, "getSize independent");
        // getWidth/getHeight double view
        check(d.getWidth() == -3.0 && d.getHeight() == -4.0, "double view");
        // equals/hashCode across instances
        check(new java.awt.Dimension(3, 4).equals(new java.awt.Dimension(3, 4)), "equals equal");
        check(new java.awt.Dimension(3, 4).hashCode() == new java.awt.Dimension(3, 4).hashCode(), "hashCode equal for equal");
        check(!new java.awt.Dimension(3, 4).equals(new java.awt.Dimension(3, 5)), "differing height");
        check(!new java.awt.Dimension(3, 4).equals(new java.awt.Dimension(4, 3)), "swapped not equal");
        check(!new java.awt.Dimension().equals(null), "equals(null)");
        check(!new java.awt.Dimension().equals("3x4"), "equals unrelated");
        // negative sizes are permitted by ctor (no validation)
        java.awt.Dimension neg = new java.awt.Dimension(-5, -6);
        check(neg.width == -5 && neg.height == -6, "negative sizes allowed");
        // serialization round-trip + serialVersionUID
        java.awt.Dimension rt = serDim(neg);
        check(rt.width == -5 && rt.height == -6, "serialization round-trip");
        check(java.io.ObjectStreamClass.lookup(java.awt.Dimension.class).getSerialVersionUID()
              == 4723952579491349524L, "serialVersionUID");
        // toString exact format
        check(neg.toString().equals("java.awt.Dimension[width=-5,height=-6]"), "toString exact: " + neg);
        // hashCode differs for different sizes
        check(new java.awt.Dimension(1, 2).hashCode() != new java.awt.Dimension(2, 3).hashCode(), "hashCode differs");
    }

}
