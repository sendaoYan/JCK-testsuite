/*
 * @test
 * @summary javax.swing.event.MenuEvent conformance: white-box structure + black-box behavior.
 * @run main MenuEventTest
 */

import java.lang.reflect.Modifier;
import javax.swing.event.MenuEvent;

public class MenuEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.MenuEvent");
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
        javax.swing.event.MenuEvent ev = new javax.swing.event.MenuEvent(source);
        check(ev.getSource() == source, "getSource round-trip");

        javax.swing.JMenu menu = new javax.swing.JMenu("menu");
        javax.swing.event.MenuEvent menuEvent = new javax.swing.event.MenuEvent(menu);
        check(menuEvent.getSource() == menu, "menu source round-trip");
        check(menuEvent.getSource().equals(menu), "menu source equals");
        check(menuEvent.toString().contains(menu.toString()), "toString embeds menu source");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static void test() throws Exception {
        MenuEvent e = new MenuEvent("src");
        check("src".equals(e.getSource()), "source");
    }



    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
