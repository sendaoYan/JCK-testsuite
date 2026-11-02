/*
 * @test
 * @summary sun.jvm.hotspot.ui.FrameWrapper conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.ui:+open
 * @run main FrameWrapperTest
 */
import java.lang.reflect.*;

public class FrameWrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.jvm.hotspot.ui.FrameWrapper", false, FrameWrapperTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "setSize"), "method setSize");
        check(hasMethod(c, "dispose"), "method dispose");
        check(hasMethod(c, "getComponent"), "method getComponent");
        check(hasMethod(c, "setVisible"), "method setVisible");
        check(hasMethod(c, "setBackground"), "method setBackground");
        check(hasMethod(c, "getContentPane"), "method getContentPane");
        check(hasMethod(c, "setResizable"), "method setResizable");
        check(hasMethod(c, "toFront"), "method toFront");
        check(hasMethod(c, "pack"), "method pack");
        check(hasMethod(c, "setClosable"), "method setClosable");
        check(hasMethod(c, "setClosingActionListener"), "method setClosingActionListener");
        check(hasMethod(c, "setActivatedActionListener"), "method setActivatedActionListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
