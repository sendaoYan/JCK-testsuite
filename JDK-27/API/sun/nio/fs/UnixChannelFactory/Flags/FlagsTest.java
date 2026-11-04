/*
 * @test
 * @summary sun.nio.fs.UnixChannelFactory.Flags conformance: internal package reflection.
 * @modules java.base/sun.nio.fs:+open
 * @run main FlagsTest
 */
public class FlagsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.fs.UnixChannelFactory$Flags", false, FlagsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "append"), "field append");
        check(hasField(c, "create"), "field create");
        check(hasField(c, "createNew"), "field createNew");
        check(hasField(c, "deleteOnClose"), "field deleteOnClose");
        check(hasField(c, "direct"), "field direct");
        check(hasField(c, "dsync"), "field dsync");
        check(hasField(c, "noFollowLinks"), "field noFollowLinks");
        check(hasField(c, "read"), "field read");
        check(hasField(c, "sync"), "field sync");
        check(hasField(c, "truncateExisting"), "field truncateExisting");
        check(hasField(c, "write"), "field write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
