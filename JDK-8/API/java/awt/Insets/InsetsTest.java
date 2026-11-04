/*
 * @test
 * @summary java.awt.Insets conformance: top/left/bottom/right.
 * @run main InsetsTest
 */


import java.awt.Insets;
public class InsetsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Insets i = new Insets(1, 2, 3, 4);
        check(i.top == 1 && i.left == 2 && i.bottom == 3 && i.right == 4, "fields");
        check(i.equals(new Insets(1, 2, 3, 4)), "equals");
        Insets c = (Insets) i.clone();
        check(c.top == 1 && c.left == 2 && c.bottom == 3 && c.right == 4, "clone");
        check(!i.equals(new Insets(0, 0, 0, 0)), "not equal");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static java.awt.Insets serInsets(java.awt.Insets i) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(i);
        return (java.awt.Insets) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void testContract() throws Exception {
        java.awt.Insets i = new java.awt.Insets(1, 2, 3, 4);
        // field order is (top, left, bottom, right) - classic source of bugs
        check(i.top == 1 && i.left == 2 && i.bottom == 3 && i.right == 4, "ctor field order top,left,bottom,right");
        // set()
        i.set(9, 8, 7, 6);
        check(i.top == 9 && i.left == 8 && i.bottom == 7 && i.right == 6, "set() same order");
        // negative insets permitted
        java.awt.Insets neg = new java.awt.Insets(-1, -2, -3, -4);
        check(neg.top == -1, "negative insets allowed");
        // equals/hashCode contract
        check(new java.awt.Insets(1, 2, 3, 4).equals(new java.awt.Insets(1, 2, 3, 4)), "equals");
        check(!new java.awt.Insets(1, 2, 3, 4).equals(new java.awt.Insets(4, 3, 2, 1)), "reversed not equal");
        check(!new java.awt.Insets(1, 2, 3, 4).equals(new java.awt.Insets(1, 2, 3, 5)), "one field differs");
        check(new java.awt.Insets(1, 2, 3, 4).hashCode() == new java.awt.Insets(1, 2, 3, 4).hashCode(),
              "hashCode equal for equal");
        check(new java.awt.Insets(1, 2, 3, 4).hashCode() == 577, "hashCode exact for (1,2,3,4): " + new java.awt.Insets(1,2,3,4).hashCode());
        check(!new java.awt.Insets(1, 2, 3, 4).equals(null), "equals(null)");
        check(!new java.awt.Insets(1, 2, 3, 4).equals("insets"), "equals unrelated");
        // toString exact format
        check(new java.awt.Insets(1, 2, 3, 4).toString().equals("java.awt.Insets[top=1,left=2,bottom=3,right=4]"),
              "toString exact");
        // clone (Cloneable)
        java.awt.Insets cl = (java.awt.Insets) i.clone();
        check(cl.equals(i), "clone equal");
        cl.top = 100;
        check(i.top == 9, "clone independent");
        // serialization round-trip incl. negative
        java.awt.Insets rt = serInsets(neg);
        check(rt.top == -1 && rt.left == -2 && rt.bottom == -3 && rt.right == -4, "serialization round-trip");
        check(java.io.ObjectStreamClass.lookup(java.awt.Insets.class).getSerialVersionUID()
              == -2272572637695466749L, "serialVersionUID");
        check(rt.toString().equals("java.awt.Insets[top=-1,left=-2,bottom=-3,right=-4]"), "toString after round-trip");
        // set mutates all four fields atomically
        java.awt.Insets mut = new java.awt.Insets(0, 0, 0, 0);
        mut.set(1, 2, 3, 4); mut.set(5, 6, 7, 8);
        check(mut.top == 5 && mut.left == 6 && mut.bottom == 7 && mut.right == 8, "set overwrites previous");
    }

}
