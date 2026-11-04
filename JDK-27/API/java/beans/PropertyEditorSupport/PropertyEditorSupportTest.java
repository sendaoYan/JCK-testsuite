/*
 * @test
 * @summary java.beans.PropertyEditorSupport conformance: white-box structure + black-box behavior.
 * @run main PropertyEditorSupportTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

public class PropertyEditorSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.PropertyEditorSupport");
        check(PropertyEditor.class.isAssignableFrom(c), "implements PropertyEditor");
    }

    static void testBlackBox() throws Exception {
        PropertyEditorSupport pes = new PropertyEditorSupport();
        check(pes.getValue() == null, "value null default");
        check(pes.getAsText() == null, "asText null default");
        check(!pes.isPaintable(), "not paintable");
        check(!pes.supportsCustomEditor(), "no custom editor");
        check(pes.getTags() == null, "tags null");
        check("???".equals(pes.getJavaInitializationString()), "javaInit ???");
        check(pes.getCustomEditor() == null, "customEditor null");

        pes.setValue("x");
        check("x".equals(pes.getAsText()), "asText of String value");
        pes.setAsText("y");
        check("y".equals(pes.getValue()), "setAsText sets value");

        pes.setValue(Integer.valueOf(5));
        check("5".equals(pes.getAsText()), "asText of Integer value");
        boolean threw = false;
        try { pes.setAsText("z"); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "setAsText non-String throws IllegalArgumentException");

        pes.setSource("src");
        check("src".equals(pes.getSource()), "getSource/setSource");

        final int[] cnt = new int[1];
        pes.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { cnt[0]++; }
        });
        pes.firePropertyChange();
        check(cnt[0] == 1, "firePropertyChange fires");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
