/*
 * @test
 * @summary java.beans.PropertyEditor conformance: white-box structure + black-box behavior.
 * @run main PropertyEditorTest
 */

import java.beans.PropertyEditor;

public class PropertyEditorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.PropertyEditor");
        check(c.isInterface(), "is an interface");
        boolean gv=false, sat=false, gat=false, paint=false;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("getValue")) gv = true;
            if (m.getName().equals("setAsText")) sat = true;
            if (m.getName().equals("getAsText")) gat = true;
            if (m.getName().equals("isPaintable")) paint = true;
        }
        check(gv && sat && gat && paint, "key methods declared");
    }

    static void testBlackBox() throws Exception {
        PropertyEditor pe = new java.beans.PropertyEditorSupport();
        pe.setValue("x");
        check("x".equals(pe.getValue()), "getValue after setValue");
        check("x".equals(pe.getAsText()), "getAsText");
        check(!pe.isPaintable() && !pe.supportsCustomEditor(), "defaults");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
