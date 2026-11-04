/*
 * @test
 * @summary jdk.internal.module.ModuleReferenceImpl conformance: internal package reflection.
 * @modules java.base/jdk.internal.module:+open
 * @run main ModuleReferenceImplTest
 */
public class ModuleReferenceImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.module.ModuleReferenceImpl", false, ModuleReferenceImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.module.ModuleReference"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "computeHash"), "method computeHash");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "isPatched"), "method isPatched");
        check(hasMethod(c, "moduleResolution"), "method moduleResolution");
        check(hasMethod(c, "moduleTarget"), "method moduleTarget");
        check(hasMethod(c, "open"), "method open");
        check(hasMethod(c, "recordedHashes"), "method recordedHashes");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "cachedHash"), "field cachedHash");
        check(hasField(c, "hash"), "field hash");
        check(hasField(c, "hasher"), "field hasher");
        check(hasField(c, "location"), "field location");
        check(hasField(c, "moduleResolution"), "field moduleResolution");
        check(hasField(c, "patcher"), "field patcher");
        check(hasField(c, "readerSupplier"), "field readerSupplier");
        check(hasField(c, "recordedHashes"), "field recordedHashes");
        check(hasField(c, "target"), "field target");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
