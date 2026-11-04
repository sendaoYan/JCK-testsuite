/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.DebugVC50SymbolTypes conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main DebugVC50SymbolTypesTest
 */
import java.lang.reflect.*;

public class DebugVC50SymbolTypesTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.DebugVC50SymbolTypes", false, DebugVC50SymbolTypesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "S_COMPILE"), "field S_COMPILE");
        check(hasField(c, "S_SSEARCH"), "field S_SSEARCH");
        check(hasField(c, "S_END"), "field S_END");
        check(hasField(c, "S_SKIP"), "field S_SKIP");
        check(hasField(c, "S_CVRESERVE"), "field S_CVRESERVE");
        check(hasField(c, "S_OBJNAME"), "field S_OBJNAME");
        check(hasField(c, "S_ENDARG"), "field S_ENDARG");
        check(hasField(c, "S_COBOLUDT"), "field S_COBOLUDT");
        check(hasField(c, "S_MANYREG"), "field S_MANYREG");
        check(hasField(c, "S_RETURN"), "field S_RETURN");
        check(hasField(c, "S_ENTRYTHIS"), "field S_ENTRYTHIS");
        check(hasField(c, "S_REGISTER"), "field S_REGISTER");
        check(hasField(c, "S_CONSTANT"), "field S_CONSTANT");
        check(hasField(c, "S_UDT"), "field S_UDT");
        check(hasField(c, "S_COBOLUDT2"), "field S_COBOLUDT2");
        check(hasField(c, "S_MANYREG2"), "field S_MANYREG2");
        check(hasField(c, "S_BPREL32"), "field S_BPREL32");
        check(hasField(c, "S_LDATA32"), "field S_LDATA32");
        check(hasField(c, "S_GDATA32"), "field S_GDATA32");
        check(hasField(c, "S_PUB32"), "field S_PUB32");
        check(hasField(c, "S_LPROC32"), "field S_LPROC32");
        check(hasField(c, "S_GPROC32"), "field S_GPROC32");
        check(hasField(c, "S_THUNK32"), "field S_THUNK32");
        check(hasField(c, "S_BLOCK32"), "field S_BLOCK32");
        check(hasField(c, "S_WITH32"), "field S_WITH32");
        check(hasField(c, "S_LABEL32"), "field S_LABEL32");
        check(hasField(c, "S_CEXMODEL32"), "field S_CEXMODEL32");
        check(hasField(c, "S_VFTTABLE32"), "field S_VFTTABLE32");
        check(hasField(c, "S_REGREL32"), "field S_REGREL32");
        check(hasField(c, "S_LTHREAD32"), "field S_LTHREAD32");
        check(hasField(c, "S_GTHREAD32"), "field S_GTHREAD32");
        check(hasField(c, "S_LPROCMIPS"), "field S_LPROCMIPS");
        check(hasField(c, "S_GPROCMIPS"), "field S_GPROCMIPS");
        check(hasField(c, "S_PROCREF"), "field S_PROCREF");
        check(hasField(c, "S_DATAREF"), "field S_DATAREF");
        check(hasField(c, "S_ALIGN"), "field S_ALIGN");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
