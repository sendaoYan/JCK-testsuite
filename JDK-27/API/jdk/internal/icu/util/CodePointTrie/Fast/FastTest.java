/*
 * @test
 * @summary jdk.internal.icu.util.CodePointTrie.Fast conformance: internal abstract class reflection.
 * @modules java.base/jdk.internal.icu.util:+open
 * @run main FastTest
 */
public class FastTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.util.CodePointTrie$Fast", false, FastTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.util.CodePointTrie$Fast"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.icu.util.CodePointTrie"), "superclass jdk.internal.icu.util.CodePointTrie");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 6, "declared methods >= 6");
        check(hasM(ms, "bmpGet"), "method bmpGet");
        check(hasM(ms, "cpIndex"), "method cpIndex");
        check(hasM(ms, "fromBinary"), "method fromBinary");
        check(hasM(ms, "getType"), "method getType");
        check(hasM(ms, "stringIterator"), "method stringIterator");
        check(hasM(ms, "suppGet"), "method suppGet");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
