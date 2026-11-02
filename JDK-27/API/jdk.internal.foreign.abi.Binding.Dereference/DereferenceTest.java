/*
 * @test
 * @summary jdk.internal.foreign.abi.Binding.Dereference conformance: internal interface reflection.
 * @modules java.base/jdk.internal.foreign.abi:+open
 * @run main DereferenceTest
 */
public class DereferenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.abi.Binding$Dereference", false, DereferenceTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.abi.Binding$Dereference"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "jdk.internal.foreign.abi.Binding"), "interface jdk.internal.foreign.abi.Binding");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 2, "declared methods >= 2");
        check(hasM(ms, "offset"), "method offset");
        check(hasM(ms, "type"), "method type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
