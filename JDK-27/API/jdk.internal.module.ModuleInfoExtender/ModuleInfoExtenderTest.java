/*
 * @test
 * @summary jdk.internal.module.ModuleInfoExtender conformance: internal package reflection.
 * @modules java.base/jdk.internal.module:+open
 * @run main ModuleInfoExtenderTest
 */
public class ModuleInfoExtenderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.module.ModuleInfoExtender", false, ModuleInfoExtenderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "hashes"), "method hashes");
        check(hasMethod(c, "mainClass"), "method mainClass");
        check(hasMethod(c, "moduleResolution"), "method moduleResolution");
        check(hasMethod(c, "newExtender"), "method newExtender");
        check(hasMethod(c, "packages"), "method packages");
        check(hasMethod(c, "targetPlatform"), "method targetPlatform");
        check(hasMethod(c, "toByteArray"), "method toByteArray");
        check(hasMethod(c, "version"), "method version");
        check(hasMethod(c, "write"), "method write");
        check(hasField(c, "hashes"), "field hashes");
        check(hasField(c, "in"), "field in");
        check(hasField(c, "mainClass"), "field mainClass");
        check(hasField(c, "moduleResolution"), "field moduleResolution");
        check(hasField(c, "packages"), "field packages");
        check(hasField(c, "targetPlatform"), "field targetPlatform");
        check(hasField(c, "version"), "field version");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
