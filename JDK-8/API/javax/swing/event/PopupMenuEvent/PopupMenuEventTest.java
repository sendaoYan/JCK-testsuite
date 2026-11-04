/*
 * @test
 * @summary javax.swing.event.PopupMenuEvent conformance: white-box structure + black-box behavior.
 * @run main PopupMenuEventTest
 */

import java.lang.reflect.Modifier;
import javax.swing.event.PopupMenuEvent;

public class PopupMenuEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.PopupMenuEvent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.EventObject"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() {
        Object source = new Object();
        javax.swing.event.PopupMenuEvent ev = new javax.swing.event.PopupMenuEvent(source);
        check(ev.getSource() == source, "getSource round-trip");

        javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
        javax.swing.event.PopupMenuEvent menuEvent = new javax.swing.event.PopupMenuEvent(menu);
        check(menuEvent.getSource() == menu, "popup source round-trip");
        check(menuEvent.getSource().equals(menu), "popup source equals");
        check(menuEvent.toString().contains(menu.toString()), "toString embeds popup source");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static void test() throws Exception {
        PopupMenuEvent e = new PopupMenuEvent("src");
        check("src".equals(e.getSource()), "source");
    }



    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
