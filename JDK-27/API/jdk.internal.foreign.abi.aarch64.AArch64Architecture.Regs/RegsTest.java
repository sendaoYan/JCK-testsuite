/*
 * @test
 * @summary jdk.internal.foreign.abi.aarch64.AArch64Architecture.Regs conformance: internal class reflection.
 * @modules java.base/jdk.internal.foreign.abi.aarch64:+open
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
        Class<?> c = Class.forName("jdk.internal.foreign.abi.aarch64.AArch64Architecture$Regs", false, RegsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.abi.aarch64.AArch64Architecture$Regs"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 64, "declared fields >= 64");
        check(hasF(fs, "r0"), "field r0");
        check(hasF(fs, "r1"), "field r1");
        check(hasF(fs, "r10"), "field r10");
        check(hasF(fs, "r11"), "field r11");
        check(hasF(fs, "r12"), "field r12");
        check(hasF(fs, "r13"), "field r13");
        check(hasF(fs, "r14"), "field r14");
        check(hasF(fs, "r15"), "field r15");
        check(hasF(fs, "r16"), "field r16");
        check(hasF(fs, "r17"), "field r17");
        check(hasF(fs, "r18"), "field r18");
        check(hasF(fs, "r19"), "field r19");
        check(hasF(fs, "r2"), "field r2");
        check(hasF(fs, "r20"), "field r20");
        check(hasF(fs, "r21"), "field r21");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
