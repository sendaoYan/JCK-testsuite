/*
 * @test
 * @summary jdk.internal.classfile.impl.EntryMap conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main EntryMapTest
 */
public class EntryMapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.EntryMap", false, EntryMapTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.EntryMap"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 8, "declared methods >= 8");
        check(hasM(ms, "arraySize"), "method arraySize");
        check(hasM(ms, "firstToken"), "method firstToken");
        check(hasM(ms, "getIndexByToken"), "method getIndexByToken");
        check(hasM(ms, "nextPowerOfTwo"), "method nextPowerOfTwo");
        check(hasM(ms, "nextToken"), "method nextToken");
        check(hasM(ms, "put"), "method put");
        check(hasM(ms, "size"), "method size");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 7, "declared fields >= 7");
        check(hasF(fs, "NO_VALUE"), "field NO_VALUE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
