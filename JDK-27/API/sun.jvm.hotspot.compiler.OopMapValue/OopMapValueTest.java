/*
 * @test
 * @summary sun.jvm.hotspot.compiler.OopMapValue conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.compiler:+open
 * @run main OopMapValueTest
 */
import java.lang.reflect.*;

public class OopMapValueTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.compiler.OopMapValue", false, OopMapValueTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "readFrom"), "method readFrom");
        check(hasMethod(c, "setType"), "method setType");
        check(hasMethod(c, "isOop"), "method isOop");
        check(hasMethod(c, "getReg"), "method getReg");
        check(hasMethod(c, "getContentReg"), "method getContentReg");
        check(hasMethod(c, "getStackOffset"), "method getStackOffset");
        check(hasMethod(c, "setContentReg"), "method setContentReg");
        check(hasMethod(c, "setReg"), "method setReg");
        check(hasMethod(c, "isCalleeSaved"), "method isCalleeSaved");
        check(hasMethod(c, "isDerivedOop"), "method isDerivedOop");
        check(hasMethod(c, "isStackLoc"), "method isStackLoc");
        check(hasMethod(c, "isNarrowOop"), "method isNarrowOop");
        check(hasMethod(c, "isRegisterLoc"), "method isRegisterLoc");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
