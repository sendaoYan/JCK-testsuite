/*
 * @test
 * @summary java.beans.PropertyEditorSupport conformance: value/source/support flags.
 * @run main PropertyEditorSupportTest
 */

import java.beans.*;
public class PropertyEditorSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        PropertyEditorSupport pe = new PropertyEditorSupport();
        pe.setValue("hello");
        check("hello".equals(pe.getValue()), "getValue");
        pe.setSource("src");
        check("src".equals(pe.getSource()), "getSource");
        check(!pe.isPaintable(), "not paintable");
        check(!pe.supportsCustomEditor(), "no custom editor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
