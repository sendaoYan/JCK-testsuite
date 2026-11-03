/*
 * @test
 * @summary javax.swing.text.TextAction conformance: white-box structure + black-box behavior.
 * @run main TextActionTest
 */

import java.awt.event.ActionEvent;
import java.lang.reflect.Modifier;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

public class TextActionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.TextAction");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.AbstractAction"), "superclass");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static class Probe extends TextAction {
        Probe(String nm) { super(nm); }
        public void actionPerformed(ActionEvent e) {}
        public JTextComponent probe(ActionEvent e) { return getTextComponent(e); }
    }

    static Action named(final String name) {
        return new AbstractAction(name) {
            public void actionPerformed(ActionEvent e) {}
        };
    }

    static Action findByName(Action[] actions, String name) {
        for (Action a : actions) {
            if (name.equals(a.getValue(Action.NAME))) return a;
        }
        return null;
    }

    static void testBlackBox() {
        Probe p = new Probe("probe");
        check("probe".equals(p.getValue(Action.NAME)), "constructor sets name");

        JTextArea area = new JTextArea();
        check(p.probe(new ActionEvent(area, ActionEvent.ACTION_PERFORMED, "x")) == area, "getTextComponent from source");
        check(p.probe(new ActionEvent(new Object(), ActionEvent.ACTION_PERFORMED, "x")) == null, "non-text source null");
        check(p.probe(null) == null, "null event null");

        // augmentList: list2 wins on duplicate names, and empty lists are handled
        Action a1 = named("one");
        Action a2 = named("two");
        Action a3 = named("two");
        Action a4 = named("three");
        Action[] r = TextAction.augmentList(new Action[] { a1, a2 }, new Action[] { a3, a4 });
        check(r.length == 3, "augmentList dedups by name");
        check(findByName(r, "one") == a1, "augmentList keeps list1-only");
        check(findByName(r, "two") == a3, "augmentList list2 overrides");
        check(findByName(r, "three") == a4, "augmentList adds list2-only");

        Action[] r2 = TextAction.augmentList(new Action[] { a1, a2 }, new Action[0]);
        check(r2.length == 2, "augmentList empty list2");
        Action[] r3 = TextAction.augmentList(new Action[0], new Action[] { a1 });
        check(r3.length == 1 && findByName(r3, "one") == a1, "augmentList empty list1");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
