/*
 * @test
 * @summary jdk.internal.classfile.impl.AbstractBoundLocalVariable conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main AbstractBoundLocalVariableTest
 */
public class AbstractBoundLocalVariableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.AbstractBoundLocalVariable", false, AbstractBoundLocalVariableTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.AbstractBoundLocalVariable"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.classfile.impl.AbstractElement"), "superclass jdk.internal.classfile.impl.AbstractElement");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "jdk.internal.classfile.impl.Util$WritableLocalVariable"), "interface jdk.internal.classfile.impl.Util$WritableLocalVariable");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 10, "declared methods >= 10");
        check(hasM(ms, "endScope"), "method endScope");
        check(hasM(ms, "length"), "method length");
        check(hasM(ms, "name"), "method name");
        check(hasM(ms, "nameIndex"), "method nameIndex");
        check(hasM(ms, "secondaryEntry"), "method secondaryEntry");
        check(hasM(ms, "secondaryIndex"), "method secondaryIndex");
        check(hasM(ms, "slot"), "method slot");
        check(hasM(ms, "startPc"), "method startPc");
        check(hasM(ms, "startScope"), "method startScope");
        check(hasM(ms, "writeLocalTo"), "method writeLocalTo");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 4, "declared fields >= 4");
        check(hasF(fs, "code"), "field code");
        check(hasF(fs, "offset"), "field offset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
