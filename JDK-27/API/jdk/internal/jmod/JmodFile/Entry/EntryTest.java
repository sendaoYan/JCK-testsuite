/*
 * @test
 * @summary jdk.internal.jmod.JmodFile.Entry conformance: internal package reflection.
 * @modules java.base/jdk.internal.jmod:+open
 * @run main EntryTest
 */
public class EntryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.jmod.JmodFile$Entry", false, EntryTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "isDirectory"), "method isDirectory");
        check(hasMethod(c, "name"), "method name");
        check(hasMethod(c, "section"), "method section");
        check(hasMethod(c, "size"), "method size");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "zipEntry"), "method zipEntry");
        check(hasField(c, "NAME_TO_SECTION"), "field NAME_TO_SECTION");
        check(hasField(c, "name"), "field name");
        check(hasField(c, "section"), "field section");
        check(hasField(c, "zipEntry"), "field zipEntry");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
