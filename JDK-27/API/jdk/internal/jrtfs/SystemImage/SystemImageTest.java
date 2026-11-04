/*
 * @test
 * @summary jdk.internal.jrtfs.SystemImage conformance: internal package reflection.
 * @modules java.base/jdk.internal.jrtfs:+open
 * @run main SystemImageTest
 */
public class SystemImageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.jrtfs.SystemImage", false, SystemImageTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.AutoCloseable"), "interface java.lang.AutoCloseable");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "findNode"), "method findNode");
        check(hasMethod(c, "fromDirectory"), "method fromDirectory");
        check(hasMethod(c, "fromJimage"), "method fromJimage");
        check(hasMethod(c, "getResource"), "method getResource");
        check(hasMethod(c, "open"), "method open");
        check(hasField(c, "RUNTIME_HOME"), "field RUNTIME_HOME");
        check(hasField(c, "explodedModulesDir"), "field explodedModulesDir");
        check(hasField(c, "moduleImageFile"), "field moduleImageFile");
        check(hasField(c, "modulesImageExists"), "field modulesImageExists");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
