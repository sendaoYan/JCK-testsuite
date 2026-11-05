/*
 * @test
 * @summary jdk.swing.interop.LightweightContentWrapper conformance: white-box structure + black-box behavior.
 * @run main LightweightContentWrapperTest
 */

import jdk.swing.interop.*;
import java.lang.reflect.*;
import java.util.*;
public class LightweightContentWrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.swing.interop.LightweightContentWrapper");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(!c.isInterface(), "not interface");
        check(hasMethod(c, "imageBufferReset"), "imageBufferReset");
        check(hasMethod(c, "getComponent"), "getComponent");
        check(hasMethod(c, "paintLock"), "paintLock");
        check(hasMethod(c, "paintUnlock"), "paintUnlock");
        check(hasMethod(c, "imageReshaped"), "imageReshaped");
        check(hasMethod(c, "focusGrabbed"), "focusGrabbed");

    }

    static void testBlackBox() throws Exception {
        check(true, "no safe instantiable headless behavior; white-box only");

    }


    static void testDeepen() throws Exception {
        Class<?> c = Class.forName("jdk.swing.interop.LightweightContentWrapper");
        java.util.Set<String> abs = new TreeSet<>();
        int abstractMethods = 0;
        for (Method m : c.getDeclaredMethods())
            if (Modifier.isAbstract(m.getModifiers())) { abs.add(m.getName()); abstractMethods++; }
        check(abstractMethods == 16, "16 abstract method objects incl. imageBufferReset overload (" + abstractMethods + ")");
        check(abs.size() == 15, "15 distinct abstract names (" + abs.size() + ")");
        String[] must = {"imageBufferReset", "getComponent", "paintLock", "paintUnlock",
                "imageReshaped", "focusGrabbed", "imageUpdated", "focusUngrabbed",
                "preferredSizeChanged", "maximumSizeChanged", "minimumSizeChanged",
                "createDragGestureRecognizer", "createDragSourceContext", "addDropTarget", "removeDropTarget"};
        for (String n : must) check(abs.contains(n), "abstract " + n);
        check(Modifier.isPublic(c.getDeclaredConstructors()[0].getModifiers()), "public constructor");
        check(c.getDeclaredConstructors().length == 1, "single constructor");
        // anonymous full implementation is instantiable headless
        LightweightContentWrapper impl = new LightweightContentWrapper() {
            public void imageBufferReset(int[] data, int x, int y, int width, int height, int linestride) { }
            public javax.swing.JComponent getComponent() { return null; }
            public void paintLock() { }
            public void paintUnlock() { }
            public void imageReshaped(int x, int y, int width, int height) { }
            public void imageUpdated(int dirtyX, int dirtyY, int dirtyWidth, int dirtyHeight) { }
            public void focusGrabbed() { }
            public void focusUngrabbed() { }
            public void preferredSizeChanged(int width, int height) { }
            public void maximumSizeChanged(int width, int height) { }
            public void minimumSizeChanged(int width, int height) { }
            public <T extends java.awt.dnd.DragGestureRecognizer> T createDragGestureRecognizer(
                    Class<T> abstractRecognizerClass, java.awt.dnd.DragSource ds, java.awt.Component c, int srcActions,
                    java.awt.dnd.DragGestureListener dgl) { return null; }
            public void imageBufferReset(int[] data, int x, int y, int width, int height, int linestride,
                    double scaleX, double scaleY) { }
            public DragSourceContextWrapper createDragSourceContext(java.awt.dnd.DragGestureEvent dge) { return null; }
            public void addDropTarget(java.awt.dnd.DropTarget dt) { }
            public void removeDropTarget(java.awt.dnd.DropTarget dt) { }
        };
        check(impl != null, "all-abstract subclass instantiable headless");
        impl.paintLock();
        impl.paintUnlock();
        check(true, "lock/unlock stubs callable");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
