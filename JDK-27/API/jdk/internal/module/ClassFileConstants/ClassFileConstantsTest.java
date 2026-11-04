/*
 * @test
 * @summary jdk.internal.module.ClassFileConstants conformance: internal package reflection.
 * @modules java.base/jdk.internal.module:+open
 * @run main ClassFileConstantsTest
 */
public class ClassFileConstantsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.module.ClassFileConstants", false, ClassFileConstantsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "ACC_MANDATED"), "field ACC_MANDATED");
        check(hasField(c, "ACC_MODULE"), "field ACC_MODULE");
        check(hasField(c, "ACC_OPEN"), "field ACC_OPEN");
        check(hasField(c, "ACC_STATIC_PHASE"), "field ACC_STATIC_PHASE");
        check(hasField(c, "ACC_SYNTHETIC"), "field ACC_SYNTHETIC");
        check(hasField(c, "ACC_TRANSITIVE"), "field ACC_TRANSITIVE");
        check(hasField(c, "DO_NOT_RESOLVE_BY_DEFAULT"), "field DO_NOT_RESOLVE_BY_DEFAULT");
        check(hasField(c, "MODULE"), "field MODULE");
        check(hasField(c, "MODULE_HASHES"), "field MODULE_HASHES");
        check(hasField(c, "MODULE_MAIN_CLASS"), "field MODULE_MAIN_CLASS");
        check(hasField(c, "MODULE_PACKAGES"), "field MODULE_PACKAGES");
        check(hasField(c, "MODULE_RESOLUTION"), "field MODULE_RESOLUTION");
        check(hasField(c, "MODULE_TARGET"), "field MODULE_TARGET");
        check(hasField(c, "SDE"), "field SDE");
        check(hasField(c, "SOURCE_FILE"), "field SOURCE_FILE");
        check(hasField(c, "WARN_DEPRECATED"), "field WARN_DEPRECATED");
        check(hasField(c, "WARN_DEPRECATED_FOR_REMOVAL"), "field WARN_DEPRECATED_FOR_REMOVAL");
        check(hasField(c, "WARN_INCUBATING"), "field WARN_INCUBATING");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
