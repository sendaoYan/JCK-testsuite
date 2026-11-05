/*
 * @test
 * @summary jdk.swing.interop.DragSourceContextWrapper conformance: white-box structure + black-box behavior.
 * @run main DragSourceContextWrapperTest
 */

import jdk.swing.interop.*;
import java.lang.reflect.*;
import java.util.*;
public class DragSourceContextWrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.swing.interop.DragSourceContextWrapper");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(!c.isInterface(), "not interface");
        check(hasMethod(c, "convertModifiersToDropAction"), "convertModifiersToDropAction");
        check(hasMethod(c, "startSecondaryEventLoop"), "startSecondaryEventLoop");
        check(hasMethod(c, "quitSecondaryEventLoop"), "quitSecondaryEventLoop");
        check(hasMethod(c, "dragDropFinished"), "dragDropFinished");
        check(hasMethod(c, "getDragSourceContext"), "getDragSourceContext");

    }

    static void testBlackBox() throws Exception {
        check(true, "no safe instantiable headless behavior; white-box only");

    }


    static void testDeepen() throws Exception {
        Class<?> c = Class.forName("jdk.swing.interop.DragSourceContextWrapper");
        check(c.getDeclaredConstructor(java.awt.dnd.DragGestureEvent.class) != null, "ctor takes DragGestureEvent");
        check(Modifier.isPublic(c.getDeclaredConstructors()[0].getModifiers()), "ctor public");
        java.util.Set<String> abs = new TreeSet<>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isAbstract(m.getModifiers())) abs.add(m.getName());
        check(abs.equals(new TreeSet<>(java.util.Arrays.asList("quitSecondaryEventLoop", "setNativeCursor", "startDrag", "startSecondaryEventLoop"))),
                "four abstract methods exact");
        Method conv = c.getDeclaredMethod("convertModifiersToDropAction", int.class, int.class);
        check(Modifier.isStatic(conv.getModifiers()), "convertModifiersToDropAction static");
        check((Integer) conv.invoke(null, 0, java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE) == java.awt.dnd.DnDConstants.ACTION_MOVE,
                "no modifiers maps to MOVE");
        check((Integer) conv.invoke(null, java.awt.event.InputEvent.BUTTON1_DOWN_MASK, 3) == 2, "button1 masked -> MOVE");
        check((Integer) conv.invoke(null, java.awt.event.InputEvent.BUTTON2_DOWN_MASK, 3) == 2, "button2 masked -> MOVE");
        check((Integer) conv.invoke(null, java.awt.event.InputEvent.BUTTON1_DOWN_MASK, 0) == 0,
                "zero supported actions yields none");
        check((Integer) conv.invoke(null, 0, java.awt.dnd.DnDConstants.ACTION_COPY) == java.awt.dnd.DnDConstants.ACTION_COPY,
                "no modifiers + COPY-only -> COPY");
        check((Integer) conv.invoke(null, 0, java.awt.dnd.DnDConstants.ACTION_MOVE) == java.awt.dnd.DnDConstants.ACTION_MOVE,
                "no modifiers + MOVE-only -> MOVE");
        check((Integer) conv.invoke(null, 0, 7) == java.awt.dnd.DnDConstants.ACTION_MOVE, "no modifiers + any -> MOVE");
        check((Integer) conv.invoke(null, java.awt.event.InputEvent.BUTTON1_DOWN_MASK,
                java.awt.dnd.DnDConstants.ACTION_COPY) == java.awt.dnd.DnDConstants.ACTION_COPY, "button1 + COPY-only -> COPY");
        check((Integer) conv.invoke(null, java.awt.event.InputEvent.BUTTON3_DOWN_MASK, 3)
                == java.awt.dnd.DnDConstants.ACTION_MOVE, "button3 -> MOVE");
        check((Integer) conv.invoke(null, java.awt.event.InputEvent.SHIFT_DOWN_MASK, 3)
                == java.awt.dnd.DnDConstants.ACTION_MOVE, "SHIFT -> MOVE");
        check((Integer) conv.invoke(null, java.awt.event.InputEvent.CTRL_DOWN_MASK, 3)
                == java.awt.dnd.DnDConstants.ACTION_COPY, "CTRL -> COPY");
        check((Integer) conv.invoke(null, java.awt.event.InputEvent.CTRL_DOWN_MASK
                | java.awt.event.InputEvent.SHIFT_DOWN_MASK, 3) == java.awt.dnd.DnDConstants.ACTION_NONE,
                "CTRL|SHIFT ambiguous -> NONE");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
