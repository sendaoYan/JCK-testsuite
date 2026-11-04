/*
 * @test
 * @summary sun.jvm.hotspot.debugger.posix.elf.ELFSymbol conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.posix.elf:+open
 * @run main ELFSymbolTest
 */
import java.lang.reflect.*;

public class ELFSymbolTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.posix.elf.ELFSymbol", false, ELFSymbolTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "getOffset"), "method getOffset");
        check(hasMethod(c, "getBinding"), "method getBinding");
        check(hasField(c, "BINDING_LOCAL"), "field BINDING_LOCAL");
        check(hasField(c, "BINDING_GLOBAL"), "field BINDING_GLOBAL");
        check(hasField(c, "BINDING_WEAK"), "field BINDING_WEAK");
        check(hasField(c, "BINDING_LOPROC"), "field BINDING_LOPROC");
        check(hasField(c, "BINDING_HIPROC"), "field BINDING_HIPROC");
        check(hasField(c, "TYPE_NOOBJECT"), "field TYPE_NOOBJECT");
        check(hasField(c, "TYPE_OBJECT"), "field TYPE_OBJECT");
        check(hasField(c, "TYPE_FUNCTION"), "field TYPE_FUNCTION");
        check(hasField(c, "TYPE_SECTION"), "field TYPE_SECTION");
        check(hasField(c, "TYPE_FILE"), "field TYPE_FILE");
        check(hasField(c, "TYPE_LOPROC"), "field TYPE_LOPROC");
        check(hasField(c, "TYPE_HIPROC"), "field TYPE_HIPROC");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
