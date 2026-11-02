/*
 * @test
 * @summary jdk.internal.foreign.abi.LinkerOptions.Critical conformance: internal enum reflection.
 * @modules java.base/jdk.internal.foreign.abi:+open
 * @run main CriticalTest
 */
public class CriticalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.abi.LinkerOptions$Critical", false, CriticalTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.abi.LinkerOptions$Critical"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "jdk.internal.foreign.abi.LinkerOptions$LinkerOptionImpl"), "interface jdk.internal.foreign.abi.LinkerOptions$LinkerOptionImpl");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 5, "declared methods >= 5");
        check(hasM(ms, "allowHeapAccess"), "method allowHeapAccess");
        check(hasM(ms, "validateForDowncall"), "method validateForDowncall");
        check(hasM(ms, "valueOf"), "method valueOf");
        check(hasM(ms, "values"), "method values");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 3, "declared fields >= 3");
        check(hasF(fs, "ALLOW_HEAP"), "field ALLOW_HEAP");
        check(hasF(fs, "DONT_ALLOW_HEAP"), "field DONT_ALLOW_HEAP");
        check(hasEnum(fs, "ALLOW_HEAP"), "enum constant ALLOW_HEAP");
        check(hasEnum(fs, "DONT_ALLOW_HEAP"), "enum constant DONT_ALLOW_HEAP");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
