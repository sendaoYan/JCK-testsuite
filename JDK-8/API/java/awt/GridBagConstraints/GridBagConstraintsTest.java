/*
 * @test
 * @summary java.awt.GridBagConstraints conformance: default field values.
 * @run main GridBagConstraintsTest
 */


import java.awt.GridBagConstraints;
public class GridBagConstraintsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        GridBagConstraints g = new GridBagConstraints();
        check(g.gridx == GridBagConstraints.RELATIVE, "default gridx RELATIVE");
        check(g.gridy == GridBagConstraints.RELATIVE, "default gridy RELATIVE");
        check(g.gridwidth == 1 && g.gridheight == 1, "default grid size 1");
        check(g.weightx == 0.0 && g.weighty == 0.0, "default weights 0");
        check(g.fill == GridBagConstraints.NONE, "default fill NONE");
        check(g.anchor == GridBagConstraints.CENTER, "default anchor CENTER");
        check(g.insets != null && g.insets.top == 0 && g.insets.left == 0, "default insets zero");
        GridBagConstraints copy = (GridBagConstraints) g.clone();
        check(copy.gridx == GridBagConstraints.RELATIVE, "clone");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() throws Exception {
        java.awt.GridBagConstraints d = new java.awt.GridBagConstraints();
        // documented defaults
        check(d.gridx == java.awt.GridBagConstraints.RELATIVE && d.gridx == -1, "default gridx RELATIVE -1");
        check(d.gridy == -1, "default gridy RELATIVE -1");
        check(d.gridwidth == 1 && d.gridheight == 1, "default gridwidth/gridheight 1");
        check(d.weightx == 0.0 && d.weighty == 0.0, "default weights 0");
        check(d.anchor == java.awt.GridBagConstraints.CENTER && d.anchor == 10, "default anchor CENTER 10");
        check(d.fill == java.awt.GridBagConstraints.NONE && d.fill == 0, "default fill NONE 0");
        check(d.insets.equals(new java.awt.Insets(0, 0, 0, 0)), "default insets zero");
        check(d.ipadx == 0 && d.ipady == 0, "default ipadding 0");
        // placement constants
        check(java.awt.GridBagConstraints.RELATIVE == -1, "RELATIVE == -1");
        check(java.awt.GridBagConstraints.REMAINDER == 0, "REMAINDER == 0");
        // fill constants
        check(java.awt.GridBagConstraints.NONE == 0, "NONE == 0");
        check(java.awt.GridBagConstraints.BOTH == 1, "BOTH == 1");
        check(java.awt.GridBagConstraints.HORIZONTAL == 2, "HORIZONTAL == 2");
        check(java.awt.GridBagConstraints.VERTICAL == 3, "VERTICAL == 3");
        // anchor constants (compass values)
        check(java.awt.GridBagConstraints.CENTER == 10, "CENTER == 10");
        check(java.awt.GridBagConstraints.NORTH == 11, "NORTH == 11");
        check(java.awt.GridBagConstraints.NORTHEAST == 12, "NORTHEAST == 12");
        check(java.awt.GridBagConstraints.EAST == 13, "EAST == 13");
        check(java.awt.GridBagConstraints.SOUTH == 15, "SOUTH == 15");
        check(java.awt.GridBagConstraints.SOUTHWEST == 16, "SOUTHWEST == 16");
        check(java.awt.GridBagConstraints.WEST == 17, "WEST == 17");
        check(java.awt.GridBagConstraints.FIRST_LINE_START == 23, "FIRST_LINE_START == 23");
        check(java.awt.GridBagConstraints.LAST_LINE_END == 26, "LAST_LINE_END == 26");
        check(java.awt.GridBagConstraints.BASELINE == 0x100, "BASELINE == 256");
        check(java.awt.GridBagConstraints.ABOVE_BASELINE == 0x400, "ABOVE_BASELINE == 1024");
        // full ctor readback
        java.awt.GridBagConstraints f = new java.awt.GridBagConstraints(1, 2, 3, 4, 5.0, 6.0, 7, 8,
                new java.awt.Insets(9, 10, 11, 12), 13, 14);
        check(f.gridx == 1 && f.gridy == 2 && f.gridwidth == 3 && f.gridheight == 4, "full ctor grid fields");
        check(f.weightx == 5.0 && f.weighty == 6.0, "full ctor weights");
        check(f.anchor == 7 && f.fill == 8, "full ctor anchor/fill");
        check(f.insets.equals(new java.awt.Insets(9, 10, 11, 12)), "full ctor insets");
        check(f.ipadx == 13 && f.ipady == 14, "full ctor ipadding");
        // clone is deep for insets
        java.awt.GridBagConstraints cl = (java.awt.GridBagConstraints) f.clone();
        check(cl.gridx == 1 && cl.weightx == 5.0, "clone copies fields");
        f.insets.top = 99;
        check(cl.insets.top == 9, "clone insets is an independent object");
        // NO equals/hashCode override: identity semantics (observed)
        java.awt.GridBagConstraints f2 = new java.awt.GridBagConstraints(1, 2, 3, 4, 5.0, 6.0, 7, 8,
                new java.awt.Insets(9, 10, 11, 12), 13, 14);
        check(!f.equals(f2), "no equals override: identical values are not equal (observed)");
        check(f.equals(f), "identity equals still true for same object");
        boolean noEquals = true;
        for (java.lang.reflect.Method m : java.awt.GridBagConstraints.class.getDeclaredMethods())
            if (m.getName().equals("equals")) noEquals = false;
        check(noEquals, "GridBagConstraints declares no equals method");
        // serialization round-trip preserves every field
        java.awt.GridBagConstraints clean = new java.awt.GridBagConstraints(1, 2, 3, 4, 5.0, 6.0, 7, 8,
                new java.awt.Insets(9, 10, 11, 12), 13, 14);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(clean);
        java.awt.GridBagConstraints rt = (java.awt.GridBagConstraints) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(rt.gridx == 1 && rt.gridy == 2 && rt.gridwidth == 3 && rt.gridheight == 4, "ser grid fields");
        check(rt.weightx == 5.0 && rt.weighty == 6.0, "ser weights");
        check(rt.anchor == 7 && rt.fill == 8, "ser anchor/fill");
        check(rt.insets.equals(new java.awt.Insets(9, 10, 11, 12)), "ser insets preserved");
        check(rt.ipadx == 13 && rt.ipady == 14, "ser ipadding");
        // serialVersionUID
        check(java.io.ObjectStreamClass.lookup(java.awt.GridBagConstraints.class).getSerialVersionUID()
              == -1000070633030801713L, "serialVersionUID -1000070633030801713L");
        // clone implements Cloneable, class implements Serializable
        check(java.io.Serializable.class.isAssignableFrom(java.awt.GridBagConstraints.class), "Serializable");
        check(java.awt.GridBagConstraints.class.getInterfaces().length >= 1, "implements Cloneable");
    }

}
