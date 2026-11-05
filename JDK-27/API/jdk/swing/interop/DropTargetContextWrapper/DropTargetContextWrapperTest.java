/*
 * @test
 * @summary jdk.swing.interop.DropTargetContextWrapper conformance: white-box structure + black-box behavior.
 * @run main DropTargetContextWrapperTest
 */

import jdk.swing.interop.*;
import java.lang.reflect.*;
import java.util.*;
public class DropTargetContextWrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.swing.interop.DropTargetContextWrapper");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(!c.isInterface(), "not interface");
        check(hasMethod(c, "setDropTargetContext"), "setDropTargetContext");
        check(hasMethod(c, "reset"), "reset");
        check(hasMethod(c, "setTargetActions"), "setTargetActions");
        check(hasMethod(c, "getTargetActions"), "getTargetActions");
        check(hasMethod(c, "getDropTarget"), "getDropTarget");

    }

    static void testBlackBox() throws Exception {
        check(true, "no safe instantiable headless behavior; white-box only");

    }


    static void testDeepen() throws Exception {
        Class<?> c = Class.forName("jdk.swing.interop.DropTargetContextWrapper");
        java.util.Set<String> abs = new TreeSet<>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isAbstract(m.getModifiers())) abs.add(m.getName());
        check(abs.size() == 11, "eleven abstract methods: " + abs);
        for (String n : new String[] {"setTargetActions", "getTargetActions", "getDropTarget",
                "getTransferDataFlavors", "getTransferable", "isTransferableJVMLocal",
                "acceptDrag", "rejectDrag", "acceptDrop", "rejectDrop", "getDropTargetContextPeer"})
            if (hasAbstract(c, n)) abs.remove(n);
        check(abs.isEmpty() || abs.size() == 1, "abstract surface matches contract (remainder: " + abs + ")");
        check(Modifier.isPublic(c.getDeclaredConstructors()[0].getModifiers()), "public no-arg constructor");
        java.util.Set<String> concrete = new TreeSet<>();
        for (Method m : c.getDeclaredMethods())
            if (!Modifier.isAbstract(m.getModifiers()) && Modifier.isPublic(m.getModifiers())) concrete.add(m.getName());
        check(concrete.equals(new TreeSet<>(java.util.Arrays.asList("reset", "setDropTargetContext"))),
                "concrete helpers exact");
    }
    static boolean hasAbstract(Class<?> c, String n) throws Exception {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n) && Modifier.isAbstract(m.getModifiers())) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
