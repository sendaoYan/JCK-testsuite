/*
 * @test
 * @summary javax.swing.JMenuBar conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JMenuBarTest
 */

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Insets;
import java.lang.reflect.Modifier;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class JMenuBarTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JMenuBar");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.MenuElement"), "interface 1");
        check(c.getConstructor() != null, "public no-arg constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // construction + empty state
        JMenuBar bar = new JMenuBar();
        check(bar.getMenuCount() == 0, "empty JMenuBar has 0 menus");
        check(bar.getMenu(0) == null, "getMenu(0) on empty bar is null");
        check(bar.getMenu(-1) == null, "getMenu(-1) is null");
        check(bar.getComponentAtIndex(0) == null, "getComponentAtIndex(0) on empty bar is null");
        check("MenuBarUI".equals(bar.getUIClassID()), "UIClassID is MenuBarUI");
        check(bar.getComponent() == bar, "MenuElement.getComponent() returns the bar itself");

        // add / getMenu / getMenuCount / index round-trips
        JMenu file = new JMenu("File");
        JMenu edit = new JMenu("Edit");
        check(bar.add(file) == file, "add(JMenu) returns the added menu");
        check(bar.getMenuCount() == 1, "menu count 1 after first add");
        check(bar.getMenu(0) == file, "getMenu(0) returns the added menu");
        check(bar.getComponentAtIndex(0) == file, "getComponentAtIndex(0) returns the menu");
        check(bar.getComponentIndex(file) == 0, "getComponentIndex(file) is 0");
        check(bar.getComponentIndex(new JMenu("ghost")) == -1, "getComponentIndex of unknown menu is -1");
        check(bar.add(edit) == edit, "second add returns menu");
        check(bar.getMenuCount() == 2, "menu count 2 after second add");
        check(bar.getMenu(1) == edit, "getMenu(1) returns second menu");
        check(bar.getMenu(2) == null, "getMenu(2) beyond size is null");

        // getSubElements lists only MenuElement children (JMenu is a MenuElement)
        check(bar.getSubElements().length == 2, "getSubElements length equals menu count");
        check(bar.getSubElements()[0] == file, "getSubElements[0] is first menu");
        check(bar.getSubElements()[1] == edit, "getSubElements[1] is second menu");

        // help menu is not implemented and throws Error
        boolean helpSetThrew = false;
        try { bar.setHelpMenu(edit); } catch (Error e) { helpSetThrew = true; }
        check(helpSetThrew, "setHelpMenu throws Error (not implemented)");
        boolean helpGetThrew = false;
        try { bar.getHelpMenu(); } catch (Error e) { helpGetThrew = true; }
        check(helpGetThrew, "getHelpMenu throws Error (not implemented)");

        // selection model round-trip through setSelected/isSelected
        check(bar.getSelectionModel() != null, "selection model is non-null");
        bar.setSelected(file);
        check(bar.isSelected(), "isSelected after setSelected(file)");
        bar.setSelected(edit);
        check(bar.isSelected(), "isSelected after switching to edit");
        bar.setSelected(new JMenu("not-in-bar"));
        check(!bar.isSelected(), "setSelected with unknown component clears selection");

        // borderPainted round-trip
        check(bar.isBorderPainted(), "borderPainted defaults to true");
        bar.setBorderPainted(false);
        check(!bar.isBorderPainted(), "setBorderPainted(false) reflected");
        bar.setBorderPainted(true);
        check(bar.isBorderPainted(), "setBorderPainted(true) reflected");

        // margin round-trip
        Insets zero = new Insets(0, 0, 0, 0);
        check(zero.equals(bar.getMargin()), "default margin is zero insets");
        Insets m = new Insets(1, 2, 3, 4);
        bar.setMargin(m);
        check(m.equals(bar.getMargin()), "setMargin(Insets) reflected");
        bar.setMargin(null);
        check(zero.equals(bar.getMargin()), "setMargin(null) restores zero margin");

        // remove resets counts
        bar.remove(file);
        bar.remove(edit);
        check(bar.getMenuCount() == 0, "menu count 0 after removing both menus");
    }

    interface ThrowingRunnable { void run() throws Exception; }

    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JMenuBarTest::testWhiteBox);
        runOnEDT(JMenuBarTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
