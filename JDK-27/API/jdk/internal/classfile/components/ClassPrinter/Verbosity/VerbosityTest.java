/*
 * @test
 * @summary jdk.internal.classfile.components.ClassPrinter.Verbosity conformance: internal enum reflection.
 * @modules java.base/jdk.internal.classfile.components:+open
 * @run main VerbosityTest
 */
public class VerbosityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.components.ClassPrinter$Verbosity", false, VerbosityTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.components.ClassPrinter$Verbosity"), "name");
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
        check(fs.length >= 4, "declared fields >= 4");
        check(hasF(fs, "CRITICAL_ATTRIBUTES"), "field CRITICAL_ATTRIBUTES");
        check(hasF(fs, "MEMBERS_ONLY"), "field MEMBERS_ONLY");
        check(hasF(fs, "TRACE_ALL"), "field TRACE_ALL");
        check(hasEnum(fs, "CRITICAL_ATTRIBUTES"), "enum constant CRITICAL_ATTRIBUTES");
        check(hasEnum(fs, "MEMBERS_ONLY"), "enum constant MEMBERS_ONLY");
        check(hasEnum(fs, "TRACE_ALL"), "enum constant TRACE_ALL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
