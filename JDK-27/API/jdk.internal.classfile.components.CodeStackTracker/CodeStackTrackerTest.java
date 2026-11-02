/*
 * @test
 * @summary jdk.internal.classfile.components.CodeStackTracker conformance: internal interface reflection.
 * @modules java.base/jdk.internal.classfile.components:+open
 * @run main CodeStackTrackerTest
 */
public class CodeStackTrackerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.components.CodeStackTracker", false, CodeStackTrackerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.components.CodeStackTracker"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.classfile.CodeTransform"), "interface java.lang.classfile.CodeTransform");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 3, "declared methods >= 3");
        check(hasM(ms, "maxStackSize"), "method maxStackSize");
        check(hasM(ms, "of"), "method of");
        check(hasM(ms, "stack"), "method stack");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
