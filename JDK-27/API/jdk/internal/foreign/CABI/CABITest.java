/*
 * @test
 * @summary jdk.internal.foreign.CABI conformance: internal enum reflection.
 * @modules java.base/jdk.internal.foreign:+open
 * @run main CABITest
 */
public class CABITest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.CABI", false, CABITest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.CABI"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 5, "declared methods >= 5");
        check(hasM(ms, "current"), "method current");
        check(hasM(ms, "valueOf"), "method valueOf");
        check(hasM(ms, "values"), "method values");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 14, "declared fields >= 14");
        check(hasF(fs, "AIX_PPC_64"), "field AIX_PPC_64");
        check(hasF(fs, "FALLBACK"), "field FALLBACK");
        check(hasF(fs, "LINUX_AARCH_64"), "field LINUX_AARCH_64");
        check(hasF(fs, "LINUX_PPC_64"), "field LINUX_PPC_64");
        check(hasF(fs, "LINUX_PPC_64_LE"), "field LINUX_PPC_64_LE");
        check(hasF(fs, "LINUX_RISCV_64"), "field LINUX_RISCV_64");
        check(hasF(fs, "LINUX_S390"), "field LINUX_S390");
        check(hasF(fs, "MAC_OS_AARCH_64"), "field MAC_OS_AARCH_64");
        check(hasF(fs, "SYS_V"), "field SYS_V");
        check(hasF(fs, "UNSUPPORTED"), "field UNSUPPORTED");
        check(hasF(fs, "WIN_64"), "field WIN_64");
        check(hasF(fs, "WIN_AARCH_64"), "field WIN_AARCH_64");
        check(hasEnum(fs, "AIX_PPC_64"), "enum constant AIX_PPC_64");
        check(hasEnum(fs, "FALLBACK"), "enum constant FALLBACK");
        check(hasEnum(fs, "LINUX_AARCH_64"), "enum constant LINUX_AARCH_64");
        check(hasEnum(fs, "LINUX_PPC_64"), "enum constant LINUX_PPC_64");
        check(hasEnum(fs, "LINUX_PPC_64_LE"), "enum constant LINUX_PPC_64_LE");
        check(hasEnum(fs, "LINUX_RISCV_64"), "enum constant LINUX_RISCV_64");
        check(hasEnum(fs, "LINUX_S390"), "enum constant LINUX_S390");
        check(hasEnum(fs, "MAC_OS_AARCH_64"), "enum constant MAC_OS_AARCH_64");
        check(hasEnum(fs, "SYS_V"), "enum constant SYS_V");
        check(hasEnum(fs, "UNSUPPORTED"), "enum constant UNSUPPORTED");
        check(hasEnum(fs, "WIN_64"), "enum constant WIN_64");
        check(hasEnum(fs, "WIN_AARCH_64"), "enum constant WIN_AARCH_64");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
