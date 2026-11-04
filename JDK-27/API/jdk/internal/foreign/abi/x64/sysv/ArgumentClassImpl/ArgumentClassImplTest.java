/*
 * @test
 * @summary jdk.internal.foreign.abi.x64.sysv.ArgumentClassImpl conformance: internal enum reflection.
 * @modules java.base/jdk.internal.foreign.abi.x64.sysv:+open
 * @run main ArgumentClassImplTest
 */
public class ArgumentClassImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.abi.x64.sysv.ArgumentClassImpl", false, ArgumentClassImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.abi.x64.sysv.ArgumentClassImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 7, "declared methods >= 7");
        check(hasM(ms, "isIndirect"), "method isIndirect");
        check(hasM(ms, "isIntegral"), "method isIntegral");
        check(hasM(ms, "isPointer"), "method isPointer");
        check(hasM(ms, "merge"), "method merge");
        check(hasM(ms, "valueOf"), "method valueOf");
        check(hasM(ms, "values"), "method values");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 10, "declared fields >= 10");
        check(hasF(fs, "COMPLEX_X87"), "field COMPLEX_X87");
        check(hasF(fs, "INTEGER"), "field INTEGER");
        check(hasF(fs, "MEMORY"), "field MEMORY");
        check(hasF(fs, "NO_CLASS"), "field NO_CLASS");
        check(hasF(fs, "POINTER"), "field POINTER");
        check(hasF(fs, "SSE"), "field SSE");
        check(hasF(fs, "SSEUP"), "field SSEUP");
        check(hasF(fs, "X87"), "field X87");
        check(hasF(fs, "X87UP"), "field X87UP");
        check(hasEnum(fs, "COMPLEX_X87"), "enum constant COMPLEX_X87");
        check(hasEnum(fs, "INTEGER"), "enum constant INTEGER");
        check(hasEnum(fs, "MEMORY"), "enum constant MEMORY");
        check(hasEnum(fs, "NO_CLASS"), "enum constant NO_CLASS");
        check(hasEnum(fs, "POINTER"), "enum constant POINTER");
        check(hasEnum(fs, "SSE"), "enum constant SSE");
        check(hasEnum(fs, "SSEUP"), "enum constant SSEUP");
        check(hasEnum(fs, "X87"), "enum constant X87");
        check(hasEnum(fs, "X87UP"), "enum constant X87UP");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
