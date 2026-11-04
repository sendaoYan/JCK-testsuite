/*
 * @test
 * @summary jdk.internal.javac.PreviewFeature.Feature conformance: internal enum reflection.
 * @modules java.base/jdk.internal.javac:+open
 * @run main FeatureTest
 */
public class FeatureTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.javac.PreviewFeature$Feature", false, FeatureTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.javac.PreviewFeature$Feature"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 3, "declared methods >= 3");
        check(hasM(ms, "valueOf"), "method valueOf");
        check(hasM(ms, "values"), "method values");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 6, "declared fields >= 6");
        check(hasF(fs, "LANGUAGE_MODEL"), "field LANGUAGE_MODEL");
        check(hasF(fs, "LAZY_CONSTANTS"), "field LAZY_CONSTANTS");
        check(hasF(fs, "PEM_API"), "field PEM_API");
        check(hasF(fs, "STRUCTURED_CONCURRENCY"), "field STRUCTURED_CONCURRENCY");
        check(hasF(fs, "TEST"), "field TEST");
        check(hasEnum(fs, "LANGUAGE_MODEL"), "enum constant LANGUAGE_MODEL");
        check(hasEnum(fs, "LAZY_CONSTANTS"), "enum constant LAZY_CONSTANTS");
        check(hasEnum(fs, "PEM_API"), "enum constant PEM_API");
        check(hasEnum(fs, "STRUCTURED_CONCURRENCY"), "enum constant STRUCTURED_CONCURRENCY");
        check(hasEnum(fs, "TEST"), "enum constant TEST");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
