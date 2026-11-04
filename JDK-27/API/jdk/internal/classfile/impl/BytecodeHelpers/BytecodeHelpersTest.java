/*
 * @test
 * @summary jdk.internal.classfile.impl.BytecodeHelpers conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main BytecodeHelpersTest
 */
public class BytecodeHelpersTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.BytecodeHelpers", false, BytecodeHelpersTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.BytecodeHelpers"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 51, "declared methods >= 51");
        check(hasM(ms, "aload"), "method aload");
        check(hasM(ms, "arrayLoadBytecode"), "method arrayLoadBytecode");
        check(hasM(ms, "arrayLoadOpcode"), "method arrayLoadOpcode");
        check(hasM(ms, "arrayLoadType"), "method arrayLoadType");
        check(hasM(ms, "arrayStoreBytecode"), "method arrayStoreBytecode");
        check(hasM(ms, "arrayStoreOpcode"), "method arrayStoreOpcode");
        check(hasM(ms, "arrayStoreType"), "method arrayStoreType");
        check(hasM(ms, "astore"), "method astore");
        check(hasM(ms, "cannotConvertException"), "method cannotConvertException");
        check(hasM(ms, "convertFromType"), "method convertFromType");
        check(hasM(ms, "convertOpcode"), "method convertOpcode");
        check(hasM(ms, "convertToType"), "method convertToType");
        check(hasM(ms, "dload"), "method dload");
        check(hasM(ms, "dstore"), "method dstore");
        check(hasM(ms, "fload"), "method fload");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
