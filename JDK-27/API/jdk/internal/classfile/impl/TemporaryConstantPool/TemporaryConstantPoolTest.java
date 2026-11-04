/*
 * @test
 * @summary jdk.internal.classfile.impl.TemporaryConstantPool conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main TemporaryConstantPoolTest
 */
public class TemporaryConstantPoolTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.TemporaryConstantPool", false, TemporaryConstantPoolTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.TemporaryConstantPool"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.classfile.constantpool.ConstantPoolBuilder"), "interface java.lang.classfile.constantpool.ConstantPoolBuilder");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 25, "declared methods >= 25");
        check(hasM(ms, "bootstrapMethodCount"), "method bootstrapMethodCount");
        check(hasM(ms, "bootstrapMethodEntry"), "method bootstrapMethodEntry");
        check(hasM(ms, "bsmEntry"), "method bsmEntry");
        check(hasM(ms, "canWriteDirect"), "method canWriteDirect");
        check(hasM(ms, "classEntry"), "method classEntry");
        check(hasM(ms, "constantDynamicEntry"), "method constantDynamicEntry");
        check(hasM(ms, "doubleEntry"), "method doubleEntry");
        check(hasM(ms, "entryByIndex"), "method entryByIndex");
        check(hasM(ms, "fieldRefEntry"), "method fieldRefEntry");
        check(hasM(ms, "floatEntry"), "method floatEntry");
        check(hasM(ms, "intEntry"), "method intEntry");
        check(hasM(ms, "interfaceMethodRefEntry"), "method interfaceMethodRefEntry");
        check(hasM(ms, "invokeDynamicEntry"), "method invokeDynamicEntry");
        check(hasM(ms, "longEntry"), "method longEntry");
        check(hasM(ms, "methodHandleEntry"), "method methodHandleEntry");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 1, "declared fields >= 1");
        check(hasF(fs, "INSTANCE"), "field INSTANCE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
