/*
 * @test
 * @summary sun.jvm.hotspot.asm.InstructionVisitor conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.asm:+open
 * @run main InstructionVisitorTest
 */
import java.lang.reflect.*;

public class InstructionVisitorTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.asm.InstructionVisitor", false, InstructionVisitorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "prologue"), "method prologue");
        check(hasMethod(c, "epilogue"), "method epilogue");
        check(hasMethod(c, "beginInstruction"), "method beginInstruction");
        check(hasMethod(c, "endInstruction"), "method endInstruction");
        check(hasMethod(c, "printAddress"), "method printAddress");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
