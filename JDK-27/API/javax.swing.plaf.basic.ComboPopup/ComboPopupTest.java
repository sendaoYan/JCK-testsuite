/*
 * @test
 * @summary javax.swing.plaf.basic.ComboPopup conformance: white-box structure + black-box behavior.
 * @run main ComboPopupTest
 */
import java.lang.reflect.Modifier;

public class ComboPopupTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasDeclaredMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name)) return true;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.ComboPopup");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");

        check(hasDeclaredMethod(c, "show"), "declares method show");
        check(hasDeclaredMethod(c, "hide"), "declares method hide");
        check(hasDeclaredMethod(c, "isVisible"), "declares method isVisible");
        check(hasDeclaredMethod(c, "getList"), "declares method getList");
        check(hasDeclaredMethod(c, "getMouseListener"), "declares method getMouseListener");
        check(hasDeclaredMethod(c, "getMouseMotionListener"), "declares method getMouseMotionListener");
        check(hasDeclaredMethod(c, "getKeyListener"), "declares method getKeyListener");
        check(hasDeclaredMethod(c, "uninstallingUI"), "declares method uninstallingUI");
    }

    static void testBlackBox() throws Exception {
        final boolean[] visible = { false };
        javax.swing.JList<Object> list = new javax.swing.JList<Object>(new Object[]{"a", "b"});
        javax.swing.plaf.basic.ComboPopup p = new javax.swing.plaf.basic.ComboPopup() {
            public void show() { visible[0] = true; }
            public void hide() { visible[0] = false; }
            public boolean isVisible() { return visible[0]; }
            public javax.swing.JList<Object> getList() { return list; }
            public java.awt.event.MouseListener getMouseListener() { return null; }
            public java.awt.event.MouseMotionListener getMouseMotionListener() { return null; }
            public java.awt.event.KeyListener getKeyListener() { return null; }
            public void uninstallingUI() { visible[0] = false; }
        };
        check(!p.isVisible(), "initially hidden");
        p.show();
        check(p.isVisible(), "show makes visible");
        p.hide();
        check(!p.isVisible(), "hide makes hidden");
        check(p.getList() == list, "getList returns list");
        p.uninstallingUI();
        check(!p.isVisible(), "uninstallingUI hides");
    }

}
