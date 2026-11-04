/*
 * @test
 * @summary jdk.internal.foreign.abi.x64.X86_64Architecture conformance: internal class reflection.
 * @modules java.base/jdk.internal.foreign.abi.x64:+open
 * @run main X86_64ArchitectureTest
 */
public class X86_64ArchitectureTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.abi.x64.X86_64Architecture", false, X86_64ArchitectureTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.abi.x64.X86_64Architecture"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "jdk.internal.foreign.abi.Architecture"), "interface jdk.internal.foreign.abi.Architecture");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 8, "declared methods >= 8");
        check(hasM(ms, "abiFor"), "method abiFor");
        check(hasM(ms, "isStackType"), "method isStackType");
        check(hasM(ms, "stackStorage"), "method stackStorage");
        check(hasM(ms, "typeSize"), "method typeSize");
        check(hasM(ms, "x87Storage"), "method x87Storage");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 13, "declared fields >= 13");
        check(hasF(fs, "INSTANCE"), "field INSTANCE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
