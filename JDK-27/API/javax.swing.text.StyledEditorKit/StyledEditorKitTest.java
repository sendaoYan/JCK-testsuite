/*
 * @test
 * @summary javax.swing.text.StyledEditorKit conformance: white-box structure + black-box behavior.
 * @run main StyledEditorKitTest
 */

import java.lang.reflect.Modifier;
import javax.swing.Action;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.StyledEditorKit;
import javax.swing.JTextPane;

public class StyledEditorKitTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyledEditorKit");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.DefaultEditorKit"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static boolean hasAction(Action[] actions, String name) {
        for (Action a : actions) {
            if (name.equals(a.getValue(Action.NAME))) return true;
        }
        return false;
    }

    static void testBlackBox() {
        StyledEditorKit kit = new StyledEditorKit();
        check("text/plain".equals(kit.getContentType()), "content type inherited");
        check(kit.createDefaultDocument() instanceof DefaultStyledDocument, "default document DefaultStyledDocument");
        check(kit.getViewFactory() != null, "view factory non-null");

        Action[] actions = kit.getActions();
        check(actions.length > 0, "actions non-empty");
        check(hasAction(actions, "font-bold"), "has font-bold");
        check(hasAction(actions, "font-italic"), "has font-italic");
        check(hasAction(actions, "font-underline"), "has font-underline");
        check(hasAction(actions, "font-family-Serif"), "has font-family-Serif");
        check(hasAction(actions, "font-size-24"), "has font-size-24");
        check(hasAction(actions, "center-justify"), "has center-justify");
        check(hasAction(actions, DefaultEditorKit.insertContentAction), "inherited insert-content");

        check(kit.getInputAttributes() != null, "input attributes non-null");
        check(kit.getCharacterAttributeRun() == null, "character run null before install");

        Object clone = kit.clone();
        check(clone != null && clone != kit && clone instanceof StyledEditorKit, "clone distinct StyledEditorKit");
        check(((StyledEditorKit) clone).getInputAttributes() != null, "clone input attributes non-null");

        // install/deinstall toggles currentRun tracking
        JTextPane pane = new JTextPane();
        kit.install(pane);
        check(kit.getCharacterAttributeRun() != null, "character run after install");
        kit.deinstall(pane);
        check(kit.getCharacterAttributeRun() == null, "character run null after deinstall");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
