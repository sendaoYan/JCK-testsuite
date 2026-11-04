/*
 * @test
 * @summary sun.nio.fs.UnixFileSystem.Flags conformance: internal package reflection.
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
        Class<?> c = Class.forName("sun.nio.fs.UnixFileSystem$Flags", false, FlagsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "atomicMove"), "field atomicMove");
        check(hasField(c, "copyBasicAttributes"), "field copyBasicAttributes");
        check(hasField(c, "copyNonPosixAttributes"), "field copyNonPosixAttributes");
        check(hasField(c, "copyPosixAttributes"), "field copyPosixAttributes");
        check(hasField(c, "failIfUnableToCopyBasic"), "field failIfUnableToCopyBasic");
        check(hasField(c, "failIfUnableToCopyNonPosix"), "field failIfUnableToCopyNonPosix");
        check(hasField(c, "failIfUnableToCopyPosix"), "field failIfUnableToCopyPosix");
        check(hasField(c, "followLinks"), "field followLinks");
        check(hasField(c, "interruptible"), "field interruptible");
        check(hasField(c, "replaceExisting"), "field replaceExisting");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
