/*
 * @test
 * @summary jdk.internal.util.Architecture conformance: internal enum reflection.
 * @modules java.base/jdk.internal.util:+open
 * @run main ArchitectureTest
 */
public class ArchitectureTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.Architecture", false, ArchitectureTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.util.Architecture"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 23, "declared methods >= 23");
        check(hasM(ms, "addressSize"), "method addressSize");
        check(hasM(ms, "byteOrder"), "method byteOrder");
        check(hasM(ms, "current"), "method current");
        check(hasM(ms, "is64bit"), "method is64bit");
        check(hasM(ms, "isAARCH64"), "method isAARCH64");
        check(hasM(ms, "isARM"), "method isARM");
        check(hasM(ms, "isLOONGARCH64"), "method isLOONGARCH64");
        check(hasM(ms, "isLittleEndian"), "method isLittleEndian");
        check(hasM(ms, "isMIPS64EL"), "method isMIPS64EL");
        check(hasM(ms, "isMIPSEL"), "method isMIPSEL");
        check(hasM(ms, "isPPC"), "method isPPC");
        check(hasM(ms, "isPPC64"), "method isPPC64");
        check(hasM(ms, "isPPC64LE"), "method isPPC64LE");
        check(hasM(ms, "isRISCV64"), "method isRISCV64");
        check(hasM(ms, "isS390"), "method isS390");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 18, "declared fields >= 18");
        check(hasF(fs, "AARCH64"), "field AARCH64");
        check(hasF(fs, "ARM"), "field ARM");
        check(hasF(fs, "LOONGARCH64"), "field LOONGARCH64");
        check(hasF(fs, "MIPS64EL"), "field MIPS64EL");
        check(hasF(fs, "MIPSEL"), "field MIPSEL");
        check(hasF(fs, "OTHER"), "field OTHER");
        check(hasF(fs, "PPC"), "field PPC");
        check(hasF(fs, "PPC64"), "field PPC64");
        check(hasF(fs, "PPC64LE"), "field PPC64LE");
        check(hasF(fs, "RISCV64"), "field RISCV64");
        check(hasF(fs, "S390"), "field S390");
        check(hasF(fs, "SPARCV9"), "field SPARCV9");
        check(hasF(fs, "X64"), "field X64");
        check(hasF(fs, "X86"), "field X86");
        check(hasEnum(fs, "AARCH64"), "enum constant AARCH64");
        check(hasEnum(fs, "ARM"), "enum constant ARM");
        check(hasEnum(fs, "LOONGARCH64"), "enum constant LOONGARCH64");
        check(hasEnum(fs, "MIPS64EL"), "enum constant MIPS64EL");
        check(hasEnum(fs, "MIPSEL"), "enum constant MIPSEL");
        check(hasEnum(fs, "OTHER"), "enum constant OTHER");
        check(hasEnum(fs, "PPC"), "enum constant PPC");
        check(hasEnum(fs, "PPC64"), "enum constant PPC64");
        check(hasEnum(fs, "PPC64LE"), "enum constant PPC64LE");
        check(hasEnum(fs, "RISCV64"), "enum constant RISCV64");
        check(hasEnum(fs, "S390"), "enum constant S390");
        check(hasEnum(fs, "SPARCV9"), "enum constant SPARCV9");
        check(hasEnum(fs, "X64"), "enum constant X64");
        check(hasEnum(fs, "X86"), "enum constant X86");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
