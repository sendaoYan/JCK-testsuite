/*
 * @test
 * @summary sun.nio.fs.ExtendedOptions conformance: internal package reflection.
 * @modules java.base/sun.nio.fs:+open
 * @run main ExtendedOptionsTest
 */
public class ExtendedOptionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.fs.ExtendedOptions", false, ExtendedOptionsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "DIRECT"), "field DIRECT");
        check(hasField(c, "FILE_TREE"), "field FILE_TREE");
        check(hasField(c, "INTERRUPTIBLE"), "field INTERRUPTIBLE");
        check(hasField(c, "NOSHARE_DELETE"), "field NOSHARE_DELETE");
        check(hasField(c, "NOSHARE_READ"), "field NOSHARE_READ");
        check(hasField(c, "NOSHARE_WRITE"), "field NOSHARE_WRITE");
        check(hasField(c, "SENSITIVITY_HIGH"), "field SENSITIVITY_HIGH");
        check(hasField(c, "SENSITIVITY_LOW"), "field SENSITIVITY_LOW");
        check(hasField(c, "SENSITIVITY_MEDIUM"), "field SENSITIVITY_MEDIUM");
        check(hasField(c, "internalToExternal"), "field internalToExternal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
