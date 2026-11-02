/*
 * @test
 * @summary jdk.internal.foreign.StringSupport.CharsetKind conformance: internal enum reflection.
 * @modules java.base/jdk.internal.foreign:+open
 * @run main CharsetKindTest
 */
public class CharsetKindTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.StringSupport$CharsetKind", false, CharsetKindTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.StringSupport$CharsetKind"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 5, "declared methods >= 5");
        check(hasM(ms, "of"), "method of");
        check(hasM(ms, "terminatorCharSize"), "method terminatorCharSize");
        check(hasM(ms, "valueOf"), "method valueOf");
        check(hasM(ms, "values"), "method values");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 5, "declared fields >= 5");
        check(hasF(fs, "DOUBLE_BYTE"), "field DOUBLE_BYTE");
        check(hasF(fs, "QUAD_BYTE"), "field QUAD_BYTE");
        check(hasF(fs, "SINGLE_BYTE"), "field SINGLE_BYTE");
        check(hasEnum(fs, "DOUBLE_BYTE"), "enum constant DOUBLE_BYTE");
        check(hasEnum(fs, "QUAD_BYTE"), "enum constant QUAD_BYTE");
        check(hasEnum(fs, "SINGLE_BYTE"), "enum constant SINGLE_BYTE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
