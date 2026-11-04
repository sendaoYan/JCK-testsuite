/*
 * @test
 * @summary jdk.internal.foreign.abi.x64.X86_64Architecture.Regs conformance: internal class reflection.
 * @modules java.base/jdk.internal.foreign.abi.x64:+open
 * @run main RegsTest
 */
public class RegsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.abi.x64.X86_64Architecture$Regs", false, RegsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.abi.x64.X86_64Architecture$Regs"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 48, "declared fields >= 48");
        check(hasF(fs, "r10"), "field r10");
        check(hasF(fs, "r11"), "field r11");
        check(hasF(fs, "r12"), "field r12");
        check(hasF(fs, "r13"), "field r13");
        check(hasF(fs, "r14"), "field r14");
        check(hasF(fs, "r15"), "field r15");
        check(hasF(fs, "r8"), "field r8");
        check(hasF(fs, "r9"), "field r9");
        check(hasF(fs, "rax"), "field rax");
        check(hasF(fs, "rbp"), "field rbp");
        check(hasF(fs, "rbx"), "field rbx");
        check(hasF(fs, "rcx"), "field rcx");
        check(hasF(fs, "rdi"), "field rdi");
        check(hasF(fs, "rdx"), "field rdx");
        check(hasF(fs, "rsi"), "field rsi");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
