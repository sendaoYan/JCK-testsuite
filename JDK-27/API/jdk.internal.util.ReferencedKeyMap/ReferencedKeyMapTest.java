/*
 * @test
 * @summary jdk.internal.util.ReferencedKeyMap conformance: internal class reflection.
 * @modules java.base/jdk.internal.util:+open
 * @run main ReferencedKeyMapTest
 */
public class ReferencedKeyMapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.ReferencedKeyMap", false, ReferencedKeyMapTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.util.ReferencedKeyMap"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.util.Map"), "interface java.util.Map");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 32, "declared methods >= 32");
        check(hasM(ms, "clear"), "method clear");
        check(hasM(ms, "concurrentHashMapSupplier"), "method concurrentHashMapSupplier");
        check(hasM(ms, "containsKey"), "method containsKey");
        check(hasM(ms, "containsValue"), "method containsValue");
        check(hasM(ms, "create"), "method create");
        check(hasM(ms, "entrySet"), "method entrySet");
        check(hasM(ms, "get"), "method get");
        check(hasM(ms, "isEmpty"), "method isEmpty");
        check(hasM(ms, "keySet"), "method keySet");
        check(hasM(ms, "prepareForAOTCache"), "method prepareForAOTCache");
        check(hasM(ms, "put"), "method put");
        check(hasM(ms, "putAll"), "method putAll");
        check(hasM(ms, "putIfAbsent"), "method putIfAbsent");
        check(hasM(ms, "remove"), "method remove");
        check(hasM(ms, "removeStaleReferences"), "method removeStaleReferences");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 4, "declared fields >= 4");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
