/*
 * @test
 * @summary jdk.internal.foreign.abi.Binding.BufferLoad conformance: internal class reflection.
 * @modules java.base/jdk.internal.foreign.abi:+open
 * @run main BufferLoadTest
 */
public class BufferLoadTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.abi.Binding$BufferLoad", false, BufferLoadTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.abi.Binding$BufferLoad"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Record"), "superclass java.lang.Record");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "jdk.internal.foreign.abi.Binding$Dereference"), "interface jdk.internal.foreign.abi.Binding$Dereference");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 8, "declared methods >= 8");
        check(hasM(ms, "byteWidth"), "method byteWidth");
        check(hasM(ms, "equals"), "method equals");
        check(hasM(ms, "hashCode"), "method hashCode");
        check(hasM(ms, "interpret"), "method interpret");
        check(hasM(ms, "offset"), "method offset");
        check(hasM(ms, "toString"), "method toString");
        check(hasM(ms, "type"), "method type");
        check(hasM(ms, "verify"), "method verify");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 3, "declared fields >= 3");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
