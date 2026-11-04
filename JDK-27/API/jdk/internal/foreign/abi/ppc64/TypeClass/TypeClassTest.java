/*
 * @test
 * @summary jdk.internal.foreign.abi.ppc64.TypeClass conformance: internal enum reflection.
 * @modules java.base/jdk.internal.foreign.abi.ppc64:+open
 * @run main TypeClassTest
 */
public class TypeClassTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.abi.ppc64.TypeClass", false, TypeClassTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.abi.ppc64.TypeClass"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 11, "declared methods >= 11");
        check(hasM(ms, "classifyLayout"), "method classifyLayout");
        check(hasM(ms, "valueOf"), "method valueOf");
        check(hasM(ms, "values"), "method values");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 7, "declared fields >= 7");
        check(hasF(fs, "FLOAT"), "field FLOAT");
        check(hasF(fs, "INTEGER"), "field INTEGER");
        check(hasF(fs, "POINTER"), "field POINTER");
        check(hasF(fs, "STRUCT_HFA"), "field STRUCT_HFA");
        check(hasF(fs, "STRUCT_REGISTER"), "field STRUCT_REGISTER");
        check(hasEnum(fs, "FLOAT"), "enum constant FLOAT");
        check(hasEnum(fs, "INTEGER"), "enum constant INTEGER");
        check(hasEnum(fs, "POINTER"), "enum constant POINTER");
        check(hasEnum(fs, "STRUCT_HFA"), "enum constant STRUCT_HFA");
        check(hasEnum(fs, "STRUCT_REGISTER"), "enum constant STRUCT_REGISTER");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
