/*
 * @test
 * @summary sun.jvm.hotspot.debugger.posix.elf.ELFProgramHeader conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.posix.elf:+open
 * @run main ELFProgramHeaderTest
 */
import java.lang.reflect.*;

public class ELFProgramHeaderTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.posix.elf.ELFProgramHeader", false, ELFProgramHeaderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getType"), "method getType");
        check(hasField(c, "TYPE_NULL"), "field TYPE_NULL");
        check(hasField(c, "TYPE_LOAD"), "field TYPE_LOAD");
        check(hasField(c, "TYPE_DYNAMIC"), "field TYPE_DYNAMIC");
        check(hasField(c, "TYPE_INTERP"), "field TYPE_INTERP");
        check(hasField(c, "TYPE_NOTE"), "field TYPE_NOTE");
        check(hasField(c, "TYPE_SHLIB"), "field TYPE_SHLIB");
        check(hasField(c, "TYPE_PHDR"), "field TYPE_PHDR");
        check(hasField(c, "TYPE_LOPROC"), "field TYPE_LOPROC");
        check(hasField(c, "TYPE_HIPROC"), "field TYPE_HIPROC");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
