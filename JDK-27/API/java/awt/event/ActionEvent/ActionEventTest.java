/*
 * @test
 * @summary java.awt.event.ActionEvent conformance: white-box structure + black-box behavior.
 * @run main ActionEventTest
 */

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.lang.reflect.Modifier;

public class ActionEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.ActionEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "ActionEvent is public");
        check(!Modifier.isAbstract(m), "ActionEvent is concrete");
        check(c.getSuperclass() == AWTEvent.class, "ActionEvent extends AWTEvent");
        check(c.getField("ACTION_PERFORMED").getType() == int.class, "ACTION_PERFORMED is int");
        check(c.getField("SHIFT_MASK").getType() == int.class, "SHIFT_MASK is int");
        check(c.getConstructor(Object.class, int.class, String.class) != null, "has (Object,int,String) ctor");
        check(c.getConstructor(Object.class, int.class, String.class, int.class) != null, "has 4-arg ctor");
        check(c.getConstructor(Object.class, int.class, String.class, long.class, int.class) != null, "has 5-arg ctor");
    }

    static void testBlackBox() throws Exception {
        Object src = new Object();
        long when = 1000000L;
        ActionEvent e = new ActionEvent(src, ActionEvent.ACTION_PERFORMED, "save", when, ActionEvent.CTRL_MASK);

        check(e.getID() == ActionEvent.ACTION_PERFORMED, "getID == ACTION_PERFORMED");
        check(e.getSource() == src, "getSource round-trips");
        check(e.getActionCommand().equals("save"), "getActionCommand round-trips");
        check(e.getWhen() == when, "getWhen round-trips fixed when");
        check(e.getModifiers() == ActionEvent.CTRL_MASK, "getModifiers round-trips");
        check(e.getID() == ActionEvent.ACTION_FIRST && e.getID() == ActionEvent.ACTION_LAST, "ACTION_FIRST==ACTION_LAST==ACTION_PERFORMED");
        check(ActionEvent.ACTION_PERFORMED == 1001, "ACTION_PERFORMED == 1001");

        String ps = e.paramString();
        check(ps.contains("ACTION_PERFORMED"), "paramString has id text");
        check(ps.contains("cmd=save"), "paramString has cmd");
        check(ps.contains("modifiers=Ctrl"), "paramString has Ctrl modifier");

        // 3-arg constructor defaults: when uses current time, modifiers 0
        ActionEvent e2 = new ActionEvent(src, ActionEvent.ACTION_PERFORMED, "x");
        check(e2.getActionCommand().equals("x"), "3-arg ctor command");
        check(e2.getModifiers() == 0, "3-arg ctor modifiers 0");
        check(e2.getWhen() >= 0, "3-arg ctor when non-negative");

        // 4-arg constructor sets modifiers, time defaults
        ActionEvent e3 = new ActionEvent(src, ActionEvent.ACTION_PERFORMED, "y", ActionEvent.ALT_MASK);
        check(e3.getModifiers() == ActionEvent.ALT_MASK, "4-arg ctor modifiers");
        check(e3.getActionCommand().equals("y"), "4-arg ctor command");
        check(e3.getWhen() >= 0, "4-arg ctor when non-negative");

        // modifier mask constants are distinct powers of two
        int[] mods = { ActionEvent.SHIFT_MASK, ActionEvent.CTRL_MASK, ActionEvent.META_MASK, ActionEvent.ALT_MASK };
        for (int m : mods) {
            check(m != 0, "modifier nonzero");
            check((m & (m - 1)) == 0, "modifier is power of two");
        }
        check(ActionEvent.SHIFT_MASK == 1, "SHIFT_MASK == 1");
        check(ActionEvent.CTRL_MASK == 2, "CTRL_MASK == 2");

        // paramString for empty command/0 modifiers
        ActionEvent e4 = new ActionEvent(src, ActionEvent.ACTION_PERFORMED, "", 0L, 0);
        check(e4.paramString().contains("ACTION_PERFORMED"), "empty-command paramString still has id");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
