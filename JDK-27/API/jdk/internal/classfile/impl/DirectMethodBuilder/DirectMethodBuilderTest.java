/*
 * @test
 * @summary jdk.internal.classfile.impl.DirectMethodBuilder conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main DirectMethodBuilderTest
 */
public class DirectMethodBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.DirectMethodBuilder", false, DirectMethodBuilderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.DirectMethodBuilder"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.classfile.impl.AbstractDirectBuilder"), "superclass jdk.internal.classfile.impl.AbstractDirectBuilder");
        check(c.getInterfaces().length >= 2, "direct interfaces >= 2");
        check(hasI(c.getInterfaces(), "jdk.internal.classfile.impl.TerminalMethodBuilder"), "interface jdk.internal.classfile.impl.TerminalMethodBuilder");
        check(hasI(c.getInterfaces(), "jdk.internal.classfile.impl.Util$Writable"), "interface jdk.internal.classfile.impl.Util$Writable");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 16, "declared methods >= 16");
        check(hasM(ms, "bufferedCodeBuilder"), "method bufferedCodeBuilder");
        check(hasM(ms, "constantPool"), "method constantPool");
        check(hasM(ms, "methodFlags"), "method methodFlags");
        check(hasM(ms, "methodName"), "method methodName");
        check(hasM(ms, "methodType"), "method methodType");
        check(hasM(ms, "methodTypeSymbol"), "method methodTypeSymbol");
        check(hasM(ms, "parameterSlot"), "method parameterSlot");
        check(hasM(ms, "run"), "method run");
        check(hasM(ms, "transformCode"), "method transformCode");
        check(hasM(ms, "with"), "method with");
        check(hasM(ms, "withCode"), "method withCode");
        check(hasM(ms, "withFlags"), "method withFlags");
        check(hasM(ms, "writeTo"), "method writeTo");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 4, "declared fields >= 4");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
