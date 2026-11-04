/*
 * @test
 * @summary sun.jvm.hotspot.debugger.posix.elf.ELFSectionHeader conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.posix.elf:+open
 * @run main ELFSectionHeaderTest
 */
import java.lang.reflect.*;

public class ELFSectionHeaderTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.posix.elf.ELFSectionHeader", false, ELFSectionHeaderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getOffset"), "method getOffset");
        check(hasMethod(c, "getLink"), "method getLink");
        check(hasMethod(c, "getStringTable"), "method getStringTable");
        check(hasMethod(c, "getHashTable"), "method getHashTable");
        check(hasMethod(c, "getELFSymbol"), "method getELFSymbol");
        check(hasMethod(c, "getNumberOfSymbols"), "method getNumberOfSymbols");
        check(hasField(c, "NDX_UNDEFINED"), "field NDX_UNDEFINED");
        check(hasField(c, "NDX_LORESERVE"), "field NDX_LORESERVE");
        check(hasField(c, "NDX_LOPROC"), "field NDX_LOPROC");
        check(hasField(c, "NDX_HIPROC"), "field NDX_HIPROC");
        check(hasField(c, "NDX_ABS"), "field NDX_ABS");
        check(hasField(c, "NDX_COMMON"), "field NDX_COMMON");
        check(hasField(c, "NDX_HIRESERVE"), "field NDX_HIRESERVE");
        check(hasField(c, "TYPE_NULL"), "field TYPE_NULL");
        check(hasField(c, "TYPE_PROGBITS"), "field TYPE_PROGBITS");
        check(hasField(c, "TYPE_SYMTBL"), "field TYPE_SYMTBL");
        check(hasField(c, "TYPE_STRTBL"), "field TYPE_STRTBL");
        check(hasField(c, "TYPE_RELO_EXPLICIT"), "field TYPE_RELO_EXPLICIT");
        check(hasField(c, "TYPE_HASH"), "field TYPE_HASH");
        check(hasField(c, "TYPE_DYNAMIC"), "field TYPE_DYNAMIC");
        check(hasField(c, "TYPE_NOTE"), "field TYPE_NOTE");
        check(hasField(c, "TYPE_NOBITS"), "field TYPE_NOBITS");
        check(hasField(c, "TYPE_RELO"), "field TYPE_RELO");
        check(hasField(c, "TYPE_SHLIB"), "field TYPE_SHLIB");
        check(hasField(c, "TYPE_DYNSYM"), "field TYPE_DYNSYM");
        check(hasField(c, "TYPE_LOPROC"), "field TYPE_LOPROC");
        check(hasField(c, "TYPE_HIPROC"), "field TYPE_HIPROC");
        check(hasField(c, "TYPE_LOUSER"), "field TYPE_LOUSER");
        check(hasField(c, "TYPE_HIUSER"), "field TYPE_HIUSER");
        check(hasField(c, "FLAG_WRITE"), "field FLAG_WRITE");
        check(hasField(c, "FLAG_ALLOC"), "field FLAG_ALLOC");
        check(hasField(c, "FLAG_EXEC_INSTR"), "field FLAG_EXEC_INSTR");
        check(hasField(c, "FLAG_MASK"), "field FLAG_MASK");
        check(hasField(c, "STRING_TABLE_NAME"), "field STRING_TABLE_NAME");
        check(hasField(c, "DYNAMIC_STRING_TABLE_NAME"), "field DYNAMIC_STRING_TABLE_NAME");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
