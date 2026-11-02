/*
 * @test
 * @summary java.awt.GridBagConstraints conformance: default field values.
 * @run main GridBagConstraintsTest
 */


import java.awt.GridBagConstraints;
public class GridBagConstraintsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
