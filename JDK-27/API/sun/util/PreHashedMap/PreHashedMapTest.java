/*
 * @test
 * @summary sun.util.PreHashedMap conformance: internal package reflection.
 * @modules java.base/sun.util:+open
 * @run main PreHashedMapTest
 */
public class PreHashedMapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.PreHashedMap", false, PreHashedMapTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.AbstractMap"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "entrySet"), "method entrySet");
        check(hasMethod(c, "get"), "method get");
        check(hasMethod(c, "init"), "method init");
        check(hasMethod(c, "keySet"), "method keySet");
        check(hasMethod(c, "put"), "method put");
        check(hasField(c, "ht"), "field ht");
        check(hasField(c, "mask"), "field mask");
        check(hasField(c, "rows"), "field rows");
        check(hasField(c, "shift"), "field shift");
        check(hasField(c, "size"), "field size");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
