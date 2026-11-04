/*
 * @test
 * @summary jdk.internal.foreign.abi.ppc64.linux.LinuxPPC64leLinker conformance: internal class reflection.
 * @modules java.base/jdk.internal.foreign.abi.ppc64.linux:+open
 * @run main LinuxPPC64leLinkerTest
 */
public class LinuxPPC64leLinkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.abi.ppc64.linux.LinuxPPC64leLinker", false, LinuxPPC64leLinkerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.abi.ppc64.linux.LinuxPPC64leLinker"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.foreign.abi.AbstractLinker"), "superclass jdk.internal.foreign.abi.AbstractLinker");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 4, "declared methods >= 4");
        check(hasM(ms, "arrangeDowncall"), "method arrangeDowncall");
        check(hasM(ms, "arrangeUpcall"), "method arrangeUpcall");
        check(hasM(ms, "canonicalLayouts"), "method canonicalLayouts");
        check(hasM(ms, "getInstance"), "method getInstance");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 1, "declared fields >= 1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
