/*
 * @test
 * @summary jdk.internal.classfile.impl.BufferedFieldBuilder conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main BufferedFieldBuilderTest
 */
public class BufferedFieldBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.BufferedFieldBuilder", false, BufferedFieldBuilderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.BufferedFieldBuilder"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "jdk.internal.classfile.impl.TerminalFieldBuilder"), "interface jdk.internal.classfile.impl.TerminalFieldBuilder");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 5, "declared methods >= 5");
        check(hasM(ms, "constantPool"), "method constantPool");
        check(hasM(ms, "run"), "method run");
        check(hasM(ms, "toModel"), "method toModel");
        check(hasM(ms, "with"), "method with");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 6, "declared fields >= 6");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
