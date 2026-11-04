/*
 * @test
 * @summary jdk.internal.util.ReferencedKeySet conformance: internal class reflection.
 * @modules java.base/jdk.internal.util:+open
 * @run main ReferencedKeySetTest
 */
public class ReferencedKeySetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.ReferencedKeySet", false, ReferencedKeySetTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.util.ReferencedKeySet"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.AbstractSet"), "superclass java.util.AbstractSet");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 14, "declared methods >= 14");
        check(hasM(ms, "add"), "method add");
        check(hasM(ms, "clear"), "method clear");
        check(hasM(ms, "concurrentHashMapSupplier"), "method concurrentHashMapSupplier");
        check(hasM(ms, "contains"), "method contains");
        check(hasM(ms, "create"), "method create");
        check(hasM(ms, "get"), "method get");
        check(hasM(ms, "intern"), "method intern");
        check(hasM(ms, "isEmpty"), "method isEmpty");
        check(hasM(ms, "iterator"), "method iterator");
        check(hasM(ms, "prepareForAOTCache"), "method prepareForAOTCache");
        check(hasM(ms, "remove"), "method remove");
        check(hasM(ms, "removeStaleReferences"), "method removeStaleReferences");
        check(hasM(ms, "size"), "method size");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 1, "declared fields >= 1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
