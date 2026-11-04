/*
 * @test
 * @summary javax.swing.event.InternalFrameEvent conformance: white-box structure + black-box behavior.
 * @run main InternalFrameEventTest
 */

import java.lang.reflect.Modifier;
import javax.swing.JInternalFrame;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class InternalFrameEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.InternalFrameEvent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.AWTEvent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() {
        check(javax.swing.event.InternalFrameEvent.INTERNAL_FRAME_FIRST == 25549, "INTERNAL_FRAME_FIRST value");
        check(javax.swing.event.InternalFrameEvent.INTERNAL_FRAME_LAST == 25555, "INTERNAL_FRAME_LAST value");
        check(javax.swing.event.InternalFrameEvent.INTERNAL_FRAME_OPENED == javax.swing.event.InternalFrameEvent.INTERNAL_FRAME_FIRST, "OPENED == FIRST");
        check(javax.swing.event.InternalFrameEvent.INTERNAL_FRAME_CLOSING == javax.swing.event.InternalFrameEvent.INTERNAL_FRAME_FIRST + 1, "CLOSING == FIRST+1");
        check(javax.swing.event.InternalFrameEvent.INTERNAL_FRAME_CLOSED == javax.swing.event.InternalFrameEvent.INTERNAL_FRAME_FIRST + 2, "CLOSED == FIRST+2");
        check(javax.swing.event.InternalFrameEvent.INTERNAL_FRAME_ICONIFIED == javax.swing.event.InternalFrameEvent.INTERNAL_FRAME_FIRST + 3, "ICONIFIED == FIRST+3");
        check(javax.swing.event.InternalFrameEvent.INTERNAL_FRAME_DEICONIFIED == javax.swing.event.InternalFrameEvent.INTERNAL_FRAME_FIRST + 4, "DEICONIFIED == FIRST+4");
        check(javax.swing.event.InternalFrameEvent.INTERNAL_FRAME_ACTIVATED == javax.swing.event.InternalFrameEvent.INTERNAL_FRAME_FIRST + 5, "ACTIVATED == FIRST+5");
        check(javax.swing.event.InternalFrameEvent.INTERNAL_FRAME_DEACTIVATED == javax.swing.event.InternalFrameEvent.INTERNAL_FRAME_FIRST + 6, "DEACTIVATED == FIRST+6");

        JInternalFrame frame = new JInternalFrame();
        javax.swing.event.InternalFrameEvent opened =
            new javax.swing.event.InternalFrameEvent(frame, javax.swing.event.InternalFrameEvent.INTERNAL_FRAME_OPENED);
        check(opened.getSource() == frame, "getSource round-trip");
        check(opened.getInternalFrame() == frame, "getInternalFrame round-trip");
        check(opened.getID() == javax.swing.event.InternalFrameEvent.INTERNAL_FRAME_OPENED, "getID OPENED");
        check(opened.paramString().equals("INTERNAL_FRAME_OPENED"), "paramString OPENED");

        javax.swing.event.InternalFrameEvent activated =
            new javax.swing.event.InternalFrameEvent(frame, javax.swing.event.InternalFrameEvent.INTERNAL_FRAME_ACTIVATED);
        check(activated.getInternalFrame() == frame, "activated getInternalFrame");
        check(activated.paramString().equals("INTERNAL_FRAME_ACTIVATED"), "paramString ACTIVATED");

        javax.swing.event.InternalFrameEvent unknown =
            new javax.swing.event.InternalFrameEvent(frame, 42);
        check(unknown.paramString().equals("unknown type"), "paramString unknown type");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.InternalFrameEvent", false, InternalFrameEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.AWTEvent"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("paramString"), "method paramString");
        check(names.contains("getInternalFrame"), "method getInternalFrame");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
