/*
 * @test
 * @summary javax.swing.event.InternalFrameListener conformance: white-box structure + black-box behavior.
 * @run main InternalFrameListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

public class InternalFrameListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void checkMethod(Class<?> c, String name, Class<?>... params) {
        try {
            Method m = c.getMethod(name, params);
            check(m.getReturnType() == void.class, name + " returns void");
            check(Modifier.isAbstract(m.getModifiers()), name + " abstract");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("missing method " + name);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.InternalFrameListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.util.EventListener"), "interface 0");
        checkMethod(c, "internalFrameOpened", InternalFrameEvent.class);
        checkMethod(c, "internalFrameClosing", InternalFrameEvent.class);
        checkMethod(c, "internalFrameClosed", InternalFrameEvent.class);
        checkMethod(c, "internalFrameIconified", InternalFrameEvent.class);
        checkMethod(c, "internalFrameDeiconified", InternalFrameEvent.class);
        checkMethod(c, "internalFrameActivated", InternalFrameEvent.class);
        checkMethod(c, "internalFrameDeactivated", InternalFrameEvent.class);
        check(c.getDeclaredMethods().length == 7, "declared method count == 7");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final String[] fired = new String[1];
        final InternalFrameEvent[] last = new InternalFrameEvent[1];
        InternalFrameListener l = new InternalFrameListener() {
            public void internalFrameOpened(InternalFrameEvent e) { fired[0] = "opened"; last[0] = e; }
            public void internalFrameClosing(InternalFrameEvent e) { fired[0] = "closing"; last[0] = e; }
            public void internalFrameClosed(InternalFrameEvent e) { fired[0] = "closed"; last[0] = e; }
            public void internalFrameIconified(InternalFrameEvent e) { fired[0] = "iconified"; last[0] = e; }
            public void internalFrameDeiconified(InternalFrameEvent e) { fired[0] = "deiconified"; last[0] = e; }
            public void internalFrameActivated(InternalFrameEvent e) { fired[0] = "activated"; last[0] = e; }
            public void internalFrameDeactivated(InternalFrameEvent e) { fired[0] = "deactivated"; last[0] = e; }
        };

        JInternalFrame f = new JInternalFrame("title");
        int[] ids = new int[] {
            InternalFrameEvent.INTERNAL_FRAME_OPENED,
            InternalFrameEvent.INTERNAL_FRAME_CLOSING,
            InternalFrameEvent.INTERNAL_FRAME_CLOSED,
            InternalFrameEvent.INTERNAL_FRAME_ICONIFIED,
            InternalFrameEvent.INTERNAL_FRAME_DEICONIFIED,
            InternalFrameEvent.INTERNAL_FRAME_ACTIVATED,
            InternalFrameEvent.INTERNAL_FRAME_DEACTIVATED
        };
        String[] names = new String[] {
            "opened", "closing", "closed", "iconified", "deiconified", "activated", "deactivated"
        };

        InternalFrameEvent e0 = new InternalFrameEvent(f, ids[0]);
        l.internalFrameOpened(e0);
        check("opened".equals(fired[0]) && last[0] == e0 && last[0].getInternalFrame() == f, "internalFrameOpened");

        InternalFrameEvent e1 = new InternalFrameEvent(f, ids[1]);
        l.internalFrameClosing(e1);
        check("closing".equals(fired[0]) && last[0] == e1, "internalFrameClosing");

        InternalFrameEvent e2 = new InternalFrameEvent(f, ids[2]);
        l.internalFrameClosed(e2);
        check("closed".equals(fired[0]) && last[0] == e2, "internalFrameClosed");

        InternalFrameEvent e3 = new InternalFrameEvent(f, ids[3]);
        l.internalFrameIconified(e3);
        check("iconified".equals(fired[0]) && last[0] == e3, "internalFrameIconified");

        InternalFrameEvent e4 = new InternalFrameEvent(f, ids[4]);
        l.internalFrameDeiconified(e4);
        check("deiconified".equals(fired[0]) && last[0] == e4, "internalFrameDeiconified");

        InternalFrameEvent e5 = new InternalFrameEvent(f, ids[5]);
        l.internalFrameActivated(e5);
        check("activated".equals(fired[0]) && last[0] == e5, "internalFrameActivated");

        InternalFrameEvent e6 = new InternalFrameEvent(f, ids[6]);
        l.internalFrameDeactivated(e6);
        check("deactivated".equals(fired[0]) && last[0] == e6, "internalFrameDeactivated");

        // event id round-trip + distinct ids
        check(e0.getID() == InternalFrameEvent.INTERNAL_FRAME_OPENED, "getID round-trip");
        for (int i = 0; i < ids.length; i++) {
            for (int j = i + 1; j < ids.length; j++) {
                check(ids[i] != ids[j], "distinct internal-frame ids");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
