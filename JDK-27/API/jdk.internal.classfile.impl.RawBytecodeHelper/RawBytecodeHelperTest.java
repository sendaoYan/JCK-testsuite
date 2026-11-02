/*
 * @test
 * @summary jdk.internal.classfile.impl.RawBytecodeHelper conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main RawBytecodeHelperTest
 */
public class RawBytecodeHelperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.RawBytecodeHelper", false, RawBytecodeHelperTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.RawBytecodeHelper"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 24, "declared methods >= 24");
        check(hasM(ms, "align"), "method align");
        check(hasM(ms, "bci"), "method bci");
        check(hasM(ms, "dest"), "method dest");
        check(hasM(ms, "destW"), "method destW");
        check(hasM(ms, "endBci"), "method endBci");
        check(hasM(ms, "getIndex"), "method getIndex");
        check(hasM(ms, "getIndexU1"), "method getIndexU1");
        check(hasM(ms, "getIndexU2"), "method getIndexU2");
        check(hasM(ms, "getInt"), "method getInt");
        check(hasM(ms, "getIntUnchecked"), "method getIntUnchecked");
        check(hasM(ms, "getShort"), "method getShort");
        check(hasM(ms, "getShortUnchecked"), "method getShortUnchecked");
        check(hasM(ms, "getU1"), "method getU1");
        check(hasM(ms, "getU1Unchecked"), "method getU1Unchecked");
        check(hasM(ms, "getU2"), "method getU2");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 210, "declared fields >= 210");
        check(hasF(fs, "AALOAD"), "field AALOAD");
        check(hasF(fs, "AASTORE"), "field AASTORE");
        check(hasF(fs, "ACONST_NULL"), "field ACONST_NULL");
        check(hasF(fs, "ALOAD"), "field ALOAD");
        check(hasF(fs, "ALOAD_0"), "field ALOAD_0");
        check(hasF(fs, "ALOAD_1"), "field ALOAD_1");
        check(hasF(fs, "ALOAD_2"), "field ALOAD_2");
        check(hasF(fs, "ALOAD_3"), "field ALOAD_3");
        check(hasF(fs, "ANEWARRAY"), "field ANEWARRAY");
        check(hasF(fs, "ARETURN"), "field ARETURN");
        check(hasF(fs, "ARRAYLENGTH"), "field ARRAYLENGTH");
        check(hasF(fs, "ASTORE"), "field ASTORE");
        check(hasF(fs, "ASTORE_0"), "field ASTORE_0");
        check(hasF(fs, "ASTORE_1"), "field ASTORE_1");
        check(hasF(fs, "ASTORE_2"), "field ASTORE_2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
