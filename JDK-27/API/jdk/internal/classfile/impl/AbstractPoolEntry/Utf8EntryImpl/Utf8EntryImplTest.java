/*
 * @test
 * @summary jdk.internal.classfile.impl.AbstractPoolEntry.Utf8EntryImpl conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main Utf8EntryImplTest
 */
public class Utf8EntryImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.AbstractPoolEntry$Utf8EntryImpl", false, Utf8EntryImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.AbstractPoolEntry$Utf8EntryImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.classfile.impl.AbstractPoolEntry"), "superclass jdk.internal.classfile.impl.AbstractPoolEntry");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.classfile.constantpool.Utf8Entry"), "interface java.lang.classfile.constantpool.Utf8Entry");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 25, "declared methods >= 25");
        check(hasM(ms, "charAt"), "method charAt");
        check(hasM(ms, "clone"), "method clone");
        check(hasM(ms, "constantValue"), "method constantValue");
        check(hasM(ms, "equals"), "method equals");
        check(hasM(ms, "equalsString"), "method equalsString");
        check(hasM(ms, "equalsUtf8"), "method equalsUtf8");
        check(hasM(ms, "fieldTypeSymbol"), "method fieldTypeSymbol");
        check(hasM(ms, "hashCode"), "method hashCode");
        check(hasM(ms, "isFieldType"), "method isFieldType");
        check(hasM(ms, "isMethodType"), "method isMethodType");
        check(hasM(ms, "length"), "method length");
        check(hasM(ms, "methodTypeSymbol"), "method methodTypeSymbol");
        check(hasM(ms, "stringValue"), "method stringValue");
        check(hasM(ms, "subSequence"), "method subSequence");
        check(hasM(ms, "tag"), "method tag");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 10, "declared fields >= 10");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
