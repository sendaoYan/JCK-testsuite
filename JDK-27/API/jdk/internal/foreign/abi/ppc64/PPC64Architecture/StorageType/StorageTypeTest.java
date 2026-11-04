/*
 * @test
 * @summary jdk.internal.foreign.abi.ppc64.PPC64Architecture.StorageType conformance: internal interface reflection.
 * @modules java.base/jdk.internal.foreign.abi.ppc64:+open
 * @run main StorageTypeTest
 */
public class StorageTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.abi.ppc64.PPC64Architecture$StorageType", false, StorageTypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.abi.ppc64.PPC64Architecture$StorageType"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 4, "declared fields >= 4");
        check(hasF(fs, "FLOAT"), "field FLOAT");
        check(hasF(fs, "INTEGER"), "field INTEGER");
        check(hasF(fs, "PLACEHOLDER"), "field PLACEHOLDER");
        check(hasF(fs, "STACK"), "field STACK");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
