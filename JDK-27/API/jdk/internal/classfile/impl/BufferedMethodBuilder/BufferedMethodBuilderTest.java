/*
 * @test
 * @summary jdk.internal.classfile.impl.BufferedMethodBuilder conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main BufferedMethodBuilderTest
 */
public class BufferedMethodBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.BufferedMethodBuilder", false, BufferedMethodBuilderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.BufferedMethodBuilder"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "jdk.internal.classfile.impl.TerminalMethodBuilder"), "interface jdk.internal.classfile.impl.TerminalMethodBuilder");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 14, "declared methods >= 14");
        check(hasM(ms, "bufferedCodeBuilder"), "method bufferedCodeBuilder");
        check(hasM(ms, "constantPool"), "method constantPool");
        check(hasM(ms, "methodFlags"), "method methodFlags");
        check(hasM(ms, "methodName"), "method methodName");
        check(hasM(ms, "methodType"), "method methodType");
        check(hasM(ms, "methodTypeSymbol"), "method methodTypeSymbol");
        check(hasM(ms, "parameterSlot"), "method parameterSlot");
        check(hasM(ms, "run"), "method run");
        check(hasM(ms, "toModel"), "method toModel");
        check(hasM(ms, "transformCode"), "method transformCode");
        check(hasM(ms, "with"), "method with");
        check(hasM(ms, "withCode"), "method withCode");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 8, "declared fields >= 8");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
