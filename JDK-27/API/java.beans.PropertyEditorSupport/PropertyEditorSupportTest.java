/*
 * @test
 * @summary java.beans.PropertyEditorSupport conformance: value and property change.
 * @run main PropertyEditorSupportTest
 */

import java.beans.*;

public class PropertyEditorSupportTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        PropertyEditorSupport pes = new PropertyEditorSupport();
        pes.setValue("abc");
        check("abc".equals(pes.getValue()), "value");
        pes.setSource("src"); check("src".equals(pes.getSource()), "source");
        check(!pes.supportsCustomEditor(), "no custom editor");
        check(!pes.isPaintable(), "not paintable");
        check(pes.getJavaInitializationString() != null, "java init string");
        final boolean[] fired = new boolean[1];
        pes.addPropertyChangeListener(evt -> fired[0] = true);
        pes.firePropertyChange();
        check(fired[0], "property change fired");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
