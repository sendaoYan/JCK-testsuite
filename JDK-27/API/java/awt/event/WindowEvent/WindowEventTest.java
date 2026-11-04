/*
 * @test
 * @summary java.awt.event.WindowEvent conformance: white-box structure + black-box behavior.
 * @run main WindowEventTest
 */

import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.lang.reflect.Modifier;

public class WindowEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.WindowEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "WindowEvent is public");
        check(!Modifier.isAbstract(m), "WindowEvent is concrete");
        check(c.getSuperclass() == ComponentEvent.class, "WindowEvent extends ComponentEvent");
        check(c.getField("WINDOW_OPENED").getType() == int.class, "WINDOW_OPENED int");
        check(c.getField("WINDOW_STATE_CHANGED").getType() == int.class, "WINDOW_STATE_CHANGED int");
        check(c.getConstructor(Window.class, int.class) != null, "has (Window,int) ctor");
        check(c.getConstructor(Window.class, int.class, Window.class) != null, "has (Window,int,Window) ctor");
        check(c.getConstructor(Window.class, int.class, int.class, int.class) != null, "has (Window,int,int,int) ctor");
        check(c.getConstructor(Window.class, int.class, Window.class, int.class, int.class) != null, "has 5-arg ctor");
        check(c.getMethod("getWindow").getReturnType() == Window.class, "getWindow returns Window");
    }

    static void testBlackBox() throws Exception {
        // documented constants (stable across JDKs)
        check(WindowEvent.WINDOW_FIRST == 200, "WINDOW_FIRST == 200");
        check(WindowEvent.WINDOW_OPENED == 200, "WINDOW_OPENED == 200");
        check(WindowEvent.WINDOW_CLOSING == 201, "WINDOW_CLOSING == 201");
        check(WindowEvent.WINDOW_CLOSED == 202, "WINDOW_CLOSED == 202");
        check(WindowEvent.WINDOW_ICONIFIED == 203, "WINDOW_ICONIFIED == 203");
        check(WindowEvent.WINDOW_DEICONIFIED == 204, "WINDOW_DEICONIFIED == 204");
        check(WindowEvent.WINDOW_ACTIVATED == 205, "WINDOW_ACTIVATED == 205");
        check(WindowEvent.WINDOW_DEACTIVATED == 206, "WINDOW_DEACTIVATED == 206");
        check(WindowEvent.WINDOW_GAINED_FOCUS == 207, "WINDOW_GAINED_FOCUS == 207");
        check(WindowEvent.WINDOW_LOST_FOCUS == 208, "WINDOW_LOST_FOCUS == 208");
        check(WindowEvent.WINDOW_STATE_CHANGED == 209, "WINDOW_STATE_CHANGED == 209");
        check(WindowEvent.WINDOW_LAST == 209, "WINDOW_LAST == 209");

        // null source is rejected for every constructor (via EventObject)
        boolean iae1 = false, iae2 = false, iae4 = false, iae5 = false;
        try { new WindowEvent((Window) null, WindowEvent.WINDOW_OPENED); } catch (IllegalArgumentException x) { iae1 = true; }
        try { new WindowEvent((Window) null, WindowEvent.WINDOW_ACTIVATED, (Window) null); } catch (IllegalArgumentException x) { iae2 = true; }
        try { new WindowEvent((Window) null, WindowEvent.WINDOW_STATE_CHANGED, 0, 1); } catch (IllegalArgumentException x) { iae4 = true; }
        try { new WindowEvent((Window) null, WindowEvent.WINDOW_ACTIVATED, (Window) null, 0, 0); } catch (IllegalArgumentException x) { iae5 = true; }
        check(iae1, "null source rejected by 1-arg ctor");
        check(iae2, "null source rejected by 2-arg ctor");
        check(iae4, "null source rejected by 3-arg ctor");
        check(iae5, "null source rejected by 5-arg ctor");

        // Instance-level behavior requires a real Window (headful only).
        if (!GraphicsEnvironment.isHeadless()) {
            Window w = new Window((Window) null);
            try {
                Window opp = new Window((Window) null);
                WindowEvent e = new WindowEvent(w, WindowEvent.WINDOW_ACTIVATED, opp);
                check(e.getID() == WindowEvent.WINDOW_ACTIVATED, "headful getID");
                check(e.getSource() == w, "headful getSource");
                check(e.getWindow() == w, "headful getWindow");
                check(e.getOppositeWindow() == opp, "headful getOppositeWindow");
                check(e.getOldState() == 0 && e.getNewState() == 0, "headful default states 0");
                check(e.paramString().contains("WINDOW_ACTIVATED"), "headful paramString id");

                WindowEvent st = new WindowEvent(w, WindowEvent.WINDOW_STATE_CHANGED, 1, 6);
                check(st.getOldState() == 1 && st.getNewState() == 6, "headful old/new state round-trip");
                check(st.getOppositeWindow() == null, "headful state event opposite null");
                check(st.paramString().contains("oldState=1") && st.paramString().contains("newState=6"), "headful paramString states");
                opp.dispose();
            } finally {
                w.dispose();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
