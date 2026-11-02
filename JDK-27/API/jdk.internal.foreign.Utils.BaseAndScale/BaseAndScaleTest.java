/*
 * @test
 * @summary jdk.internal.foreign.Utils.BaseAndScale conformance: internal class reflection.
 * @modules java.base/jdk.internal.foreign:+open
 * @run main BaseAndScaleTest
 */
public class BaseAndScaleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.Utils$BaseAndScale", false, BaseAndScaleTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.Utils$BaseAndScale"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Record"), "superclass java.lang.Record");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 6, "declared methods >= 6");
        check(hasM(ms, "base"), "method base");
        check(hasM(ms, "equals"), "method equals");
        check(hasM(ms, "hashCode"), "method hashCode");
        check(hasM(ms, "of"), "method of");
        check(hasM(ms, "scale"), "method scale");
        check(hasM(ms, "toString"), "method toString");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 9, "declared fields >= 9");
        check(hasF(fs, "BYTE"), "field BYTE");
        check(hasF(fs, "CHAR"), "field CHAR");
        check(hasF(fs, "DOUBLE"), "field DOUBLE");
        check(hasF(fs, "FLOAT"), "field FLOAT");
        check(hasF(fs, "INT"), "field INT");
        check(hasF(fs, "LONG"), "field LONG");
        check(hasF(fs, "SHORT"), "field SHORT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
