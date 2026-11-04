/*
 * @test
 * @summary javax.swing.event.InternalFrameAdapter conformance: white-box structure + black-box behavior.
 * @run main InternalFrameAdapterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.util.HashSet;
import java.util.Set;

public class InternalFrameAdapterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void checkMethod(Class<?> c, String name, Class<?>... params) {
        try {
            Method m = c.getMethod(name, params);
            check(m.getReturnType() == void.class, name + " returns void");
            check(!Modifier.isAbstract(m.getModifiers()), name + " concrete");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("missing method " + name);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.InternalFrameAdapter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.event.InternalFrameListener"), "interface 0");
        // JDK-8 contract: the adapter constructor is public here (JDK 9+ narrows it to protected).
        check(Modifier.isPublic(c.getDeclaredConstructor().getModifiers()), "public constructor (JDK-8)");
        checkMethod(c, "internalFrameOpened", InternalFrameEvent.class);
        checkMethod(c, "internalFrameClosing", InternalFrameEvent.class);
        checkMethod(c, "internalFrameClosed", InternalFrameEvent.class);
        checkMethod(c, "internalFrameIconified", InternalFrameEvent.class);
        checkMethod(c, "internalFrameDeiconified", InternalFrameEvent.class);
        checkMethod(c, "internalFrameActivated", InternalFrameEvent.class);
        checkMethod(c, "internalFrameDeactivated", InternalFrameEvent.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JInternalFrame f = new JInternalFrame("t");
        final String[] fired = new String[1];
        InternalFrameAdapter a = new InternalFrameAdapter() {
            public void internalFrameOpened(InternalFrameEvent e) { fired[0] = "opened"; }
            public void internalFrameClosing(InternalFrameEvent e) { fired[0] = "closing"; }
            public void internalFrameClosed(InternalFrameEvent e) { fired[0] = "closed"; }
            public void internalFrameIconified(InternalFrameEvent e) { fired[0] = "iconified"; }
            public void internalFrameDeiconified(InternalFrameEvent e) { fired[0] = "deiconified"; }
            public void internalFrameActivated(InternalFrameEvent e) { fired[0] = "activated"; }
            public void internalFrameDeactivated(InternalFrameEvent e) { fired[0] = "deactivated"; }
        };
        check(a instanceof InternalFrameListener, "adapter is an InternalFrameListener");

        InternalFrameEvent ev = new InternalFrameEvent(f, InternalFrameEvent.INTERNAL_FRAME_OPENED);
        a.internalFrameOpened(ev);
        check("opened".equals(fired[0]), "internalFrameOpened overridable");
        check(ev.getInternalFrame() == f, "getInternalFrame round-trip");

        a.internalFrameClosing(ev); check("closing".equals(fired[0]), "internalFrameClosing overridable");
        a.internalFrameClosed(ev); check("closed".equals(fired[0]), "internalFrameClosed overridable");
        a.internalFrameIconified(ev); check("iconified".equals(fired[0]), "internalFrameIconified overridable");
        a.internalFrameDeiconified(ev); check("deiconified".equals(fired[0]), "internalFrameDeiconified overridable");
        a.internalFrameActivated(ev); check("activated".equals(fired[0]), "internalFrameActivated overridable");
        a.internalFrameDeactivated(ev); check("deactivated".equals(fired[0]), "internalFrameDeactivated overridable");

        // A plain (non-overriding) adapter's no-op methods must be callable without error.
        InternalFrameAdapter plain = new InternalFrameAdapter() {};
        plain.internalFrameOpened(ev);
        plain.internalFrameClosing(ev);
        plain.internalFrameClosed(ev);
        plain.internalFrameIconified(ev);
        plain.internalFrameDeiconified(ev);
        plain.internalFrameActivated(ev);
        plain.internalFrameDeactivated(ev);
        check(true, "plain adapter no-op methods callable");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.InternalFrameAdapter", false, InternalFrameAdapterTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.swing.event.InternalFrameListener"), "implements javax.swing.event.InternalFrameListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("internalFrameOpened"), "method internalFrameOpened");
        check(names.contains("internalFrameClosing"), "method internalFrameClosing");
        check(names.contains("internalFrameClosed"), "method internalFrameClosed");
        check(names.contains("internalFrameIconified"), "method internalFrameIconified");
        check(names.contains("internalFrameDeiconified"), "method internalFrameDeiconified");
        check(names.contains("internalFrameActivated"), "method internalFrameActivated");
        check(names.contains("internalFrameDeactivated"), "method internalFrameDeactivated");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
