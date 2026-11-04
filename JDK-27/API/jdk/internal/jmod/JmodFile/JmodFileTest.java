/*
 * @test
 * @summary jdk.internal.jmod.JmodFile conformance: internal package reflection.
 * @modules java.base/jdk.internal.jmod:+open
 * @run main JmodFileTest
 */
public class JmodFileTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.jmod.JmodFile", false, JmodFileTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.AutoCloseable"), "interface java.lang.AutoCloseable");
        check(hasMethod(c, "checkMagic"), "method checkMagic");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "getEntry"), "method getEntry");
        check(hasMethod(c, "getInputStream"), "method getInputStream");
        check(hasMethod(c, "stream"), "method stream");
        check(hasMethod(c, "writeMagicNumber"), "method writeMagicNumber");
        check(hasField(c, "JMOD_MAGIC_NUMBER"), "field JMOD_MAGIC_NUMBER");
        check(hasField(c, "JMOD_MAJOR_VERSION"), "field JMOD_MAJOR_VERSION");
        check(hasField(c, "JMOD_MINOR_VERSION"), "field JMOD_MINOR_VERSION");
        check(hasField(c, "file"), "field file");
        check(hasField(c, "zipfile"), "field zipfile");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
