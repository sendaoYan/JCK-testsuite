/*
 * @test
 * @summary jdk.internal.foreign.abi.s390.S390Architecture.Regs conformance: internal class reflection.
 * @modules java.base/jdk.internal.foreign.abi.s390:+open
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
        Class<?> c = Class.forName("jdk.internal.foreign.abi.s390.S390Architecture$Regs", false, RegsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.abi.s390.S390Architecture$Regs"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 32, "declared fields >= 32");
        check(hasF(fs, "f0"), "field f0");
        check(hasF(fs, "f1"), "field f1");
        check(hasF(fs, "f10"), "field f10");
        check(hasF(fs, "f11"), "field f11");
        check(hasF(fs, "f12"), "field f12");
        check(hasF(fs, "f13"), "field f13");
        check(hasF(fs, "f14"), "field f14");
        check(hasF(fs, "f15"), "field f15");
        check(hasF(fs, "f2"), "field f2");
        check(hasF(fs, "f3"), "field f3");
        check(hasF(fs, "f4"), "field f4");
        check(hasF(fs, "f5"), "field f5");
        check(hasF(fs, "f6"), "field f6");
        check(hasF(fs, "f7"), "field f7");
        check(hasF(fs, "f8"), "field f8");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
