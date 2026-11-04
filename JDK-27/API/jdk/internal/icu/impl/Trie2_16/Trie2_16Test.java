/*
 * @test
 * @summary jdk.internal.icu.impl.Trie2_16 conformance: internal class reflection.
 * @modules java.base/jdk.internal.icu.impl:+open
 * @run main Trie2_16Test
 */
public class Trie2_16Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.impl.Trie2_16", false, Trie2_16Test.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.impl.Trie2_16"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.icu.impl.Trie2"), "superclass jdk.internal.icu.impl.Trie2");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 6, "declared methods >= 6");
        check(hasM(ms, "createFromSerialized"), "method createFromSerialized");
        check(hasM(ms, "get"), "method get");
        check(hasM(ms, "getFromU16SingleLead"), "method getFromU16SingleLead");
        check(hasM(ms, "getSerializedLength"), "method getSerializedLength");
        check(hasM(ms, "iterator"), "method iterator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
