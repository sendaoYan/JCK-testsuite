/*
 * @test
 * @summary sun.jvm.hotspot.debugger.posix.elf.ELFHeader conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.posix.elf:+open
 * @run main ELFHeaderTest
 */
import java.lang.reflect.*;

public class ELFHeaderTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.posix.elf.ELFHeader", false, ELFHeaderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getFileType"), "method getFileType");
        check(hasMethod(c, "getArch"), "method getArch");
        check(hasMethod(c, "getSectionHeaderSize"), "method getSectionHeaderSize");
        check(hasMethod(c, "getNumberOfSectionHeaders"), "method getNumberOfSectionHeaders");
        check(hasMethod(c, "getSectionHeader"), "method getSectionHeader");
        check(hasMethod(c, "getSectionHeaderStringTable"), "method getSectionHeaderStringTable");
        check(hasMethod(c, "getStringTable"), "method getStringTable");
        check(hasMethod(c, "getDynamicStringTable"), "method getDynamicStringTable");
        check(hasMethod(c, "getHashTable"), "method getHashTable");
        check(hasMethod(c, "getSymbolTableSection"), "method getSymbolTableSection");
        check(hasMethod(c, "getDynamicSymbolTableSection"), "method getDynamicSymbolTableSection");
        check(hasMethod(c, "getELFSymbol"), "method getELFSymbol");
        check(hasField(c, "FT_NONE"), "field FT_NONE");
        check(hasField(c, "FT_REL"), "field FT_REL");
        check(hasField(c, "FT_EXEC"), "field FT_EXEC");
        check(hasField(c, "FT_DYN"), "field FT_DYN");
        check(hasField(c, "FT_CORE"), "field FT_CORE");
        check(hasField(c, "FT_LOCPROC"), "field FT_LOCPROC");
        check(hasField(c, "FT_HICPROC"), "field FT_HICPROC");
        check(hasField(c, "ARCH_NONE"), "field ARCH_NONE");
        check(hasField(c, "ARCH_ATT"), "field ARCH_ATT");
        check(hasField(c, "ARCH_SPARC"), "field ARCH_SPARC");
        check(hasField(c, "ARCH_i386"), "field ARCH_i386");
        check(hasField(c, "ARCH_68k"), "field ARCH_68k");
        check(hasField(c, "ARCH_88k"), "field ARCH_88k");
        check(hasField(c, "ARCH_i860"), "field ARCH_i860");
        check(hasField(c, "ARCH_MIPS"), "field ARCH_MIPS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
