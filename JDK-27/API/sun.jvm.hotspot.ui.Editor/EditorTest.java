/*
 * @test
 * @summary sun.jvm.hotspot.ui.Editor conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.ui:+open
 * @run main EditorTest
 */
import java.lang.reflect.*;

public class EditorTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.ui.Editor", false, EditorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "toFront"), "method toFront");
        check(hasMethod(c, "getSourceFileName"), "method getSourceFileName");
        check(hasMethod(c, "getCurrentLineNumber"), "method getCurrentLineNumber");
        check(hasMethod(c, "showLineNumber"), "method showLineNumber");
        check(hasMethod(c, "highlightLineNumber"), "method highlightLineNumber");
        check(hasMethod(c, "showBreakpointAtLine"), "method showBreakpointAtLine");
        check(hasMethod(c, "hasBreakpointAtLine"), "method hasBreakpointAtLine");
        check(hasMethod(c, "clearBreakpointAtLine"), "method clearBreakpointAtLine");
        check(hasMethod(c, "clearBreakpoints"), "method clearBreakpoints");
        check(hasMethod(c, "setUserData"), "method setUserData");
        check(hasMethod(c, "getUserData"), "method getUserData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
