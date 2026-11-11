/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.nio.fs.UnixUserPrincipals conformance: internal package reflection.
 * @modules java.base/sun.nio.fs:+open
 * @run main UnixUserPrincipalsTest
 */
public class UnixUserPrincipalsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.fs.UnixUserPrincipals", false, UnixUserPrincipalsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "fromGid"), "method fromGid");
        check(hasMethod(c, "fromUid"), "method fromUid");
        check(hasField(c, "SPECIAL_EVERYONE"), "field SPECIAL_EVERYONE");
        check(hasField(c, "SPECIAL_GROUP"), "field SPECIAL_GROUP");
        check(hasField(c, "SPECIAL_OWNER"), "field SPECIAL_OWNER");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
