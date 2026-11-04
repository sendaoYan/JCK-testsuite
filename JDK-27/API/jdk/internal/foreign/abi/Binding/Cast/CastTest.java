/*
 * @test
 * @summary jdk.internal.foreign.abi.Binding.Cast conformance: internal enum reflection.
 * @modules java.base/jdk.internal.foreign.abi:+open
 * @run main CastTest
 */
public class CastTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.abi.Binding$Cast", false, CastTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.abi.Binding$Cast"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "jdk.internal.foreign.abi.Binding"), "interface jdk.internal.foreign.abi.Binding");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 7, "declared methods >= 7");
        check(hasM(ms, "fromType"), "method fromType");
        check(hasM(ms, "interpret"), "method interpret");
        check(hasM(ms, "toType"), "method toType");
        check(hasM(ms, "valueOf"), "method valueOf");
        check(hasM(ms, "values"), "method values");
        check(hasM(ms, "verify"), "method verify");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 19, "declared fields >= 19");
        check(hasF(fs, "BOOLEAN_TO_INT"), "field BOOLEAN_TO_INT");
        check(hasF(fs, "BYTE_TO_INT"), "field BYTE_TO_INT");
        check(hasF(fs, "BYTE_TO_LONG"), "field BYTE_TO_LONG");
        check(hasF(fs, "CHAR_TO_INT"), "field CHAR_TO_INT");
        check(hasF(fs, "CHAR_TO_LONG"), "field CHAR_TO_LONG");
        check(hasF(fs, "INT_TO_BOOLEAN"), "field INT_TO_BOOLEAN");
        check(hasF(fs, "INT_TO_BYTE"), "field INT_TO_BYTE");
        check(hasF(fs, "INT_TO_CHAR"), "field INT_TO_CHAR");
        check(hasF(fs, "INT_TO_LONG"), "field INT_TO_LONG");
        check(hasF(fs, "INT_TO_SHORT"), "field INT_TO_SHORT");
        check(hasF(fs, "LONG_TO_BYTE"), "field LONG_TO_BYTE");
        check(hasF(fs, "LONG_TO_CHAR"), "field LONG_TO_CHAR");
        check(hasF(fs, "LONG_TO_INT"), "field LONG_TO_INT");
        check(hasF(fs, "LONG_TO_SHORT"), "field LONG_TO_SHORT");
        check(hasF(fs, "SHORT_TO_INT"), "field SHORT_TO_INT");
        check(hasEnum(fs, "BOOLEAN_TO_INT"), "enum constant BOOLEAN_TO_INT");
        check(hasEnum(fs, "BYTE_TO_INT"), "enum constant BYTE_TO_INT");
        check(hasEnum(fs, "BYTE_TO_LONG"), "enum constant BYTE_TO_LONG");
        check(hasEnum(fs, "CHAR_TO_INT"), "enum constant CHAR_TO_INT");
        check(hasEnum(fs, "CHAR_TO_LONG"), "enum constant CHAR_TO_LONG");
        check(hasEnum(fs, "INT_TO_BOOLEAN"), "enum constant INT_TO_BOOLEAN");
        check(hasEnum(fs, "INT_TO_BYTE"), "enum constant INT_TO_BYTE");
        check(hasEnum(fs, "INT_TO_CHAR"), "enum constant INT_TO_CHAR");
        check(hasEnum(fs, "INT_TO_LONG"), "enum constant INT_TO_LONG");
        check(hasEnum(fs, "INT_TO_SHORT"), "enum constant INT_TO_SHORT");
        check(hasEnum(fs, "LONG_TO_BYTE"), "enum constant LONG_TO_BYTE");
        check(hasEnum(fs, "LONG_TO_CHAR"), "enum constant LONG_TO_CHAR");
        check(hasEnum(fs, "LONG_TO_INT"), "enum constant LONG_TO_INT");
        check(hasEnum(fs, "LONG_TO_SHORT"), "enum constant LONG_TO_SHORT");
        check(hasEnum(fs, "SHORT_TO_INT"), "enum constant SHORT_TO_INT");
        check(hasEnum(fs, "SHORT_TO_LONG"), "enum constant SHORT_TO_LONG");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
