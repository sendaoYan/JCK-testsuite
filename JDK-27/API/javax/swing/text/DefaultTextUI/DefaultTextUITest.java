/*
 * @test
 * @summary javax.swing.text.DefaultTextUI conformance: white-box structure + black-box behavior.
 * @run main DefaultTextUITest
 */

import java.lang.reflect.Modifier;
import javax.swing.JTextArea;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultTextUI;
import javax.swing.text.EditorKit;

@SuppressWarnings("deprecation")
public class DefaultTextUITest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // concrete subclass for behavioral tests (BasicTextUI.getPropertyPrefix is abstract)
    static class TestUI extends DefaultTextUI {
        @Override
        protected String getPropertyPrefix() {
            return "Test";
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultTextUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicTextUI"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        TestUI ui = new TestUI();
        check(ui instanceof BasicTextUI, "extends BasicTextUI");
        check(ui instanceof javax.swing.plaf.TextUI, "is a TextUI");

        JTextArea area = new JTextArea("hello");
        EditorKit kit = ui.getEditorKit(area);
        check(kit != null, "getEditorKit non-null");
        check(kit instanceof DefaultEditorKit, "getEditorKit returns DefaultEditorKit");
        check(ui.getEditorKit(null) != null, "getEditorKit(null) non-null");

        ui.installUI(area);
        ui.uninstallUI(area);
        check(true, "installUI/uninstallUI complete without exception");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
