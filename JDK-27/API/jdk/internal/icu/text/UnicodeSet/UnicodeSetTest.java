/*
 * @test
 * @summary jdk.internal.icu.text.UnicodeSet conformance: internal class reflection.
 * @modules java.base/jdk.internal.icu.text:+open
 * @run main UnicodeSetTest
 */
public class UnicodeSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.text.UnicodeSet", false, UnicodeSetTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.text.UnicodeSet"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.Cloneable"), "interface java.lang.Cloneable");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 36, "declared methods >= 36");
        check(hasM(ms, "add"), "method add");
        check(hasM(ms, "clear"), "method clear");
        check(hasM(ms, "clone"), "method clone");
        check(hasM(ms, "cloneAsThawed"), "method cloneAsThawed");
        check(hasM(ms, "complement"), "method complement");
        check(hasM(ms, "contains"), "method contains");
        check(hasM(ms, "freeze"), "method freeze");
        check(hasM(ms, "getRangeCount"), "method getRangeCount");
        check(hasM(ms, "getRangeEnd"), "method getRangeEnd");
        check(hasM(ms, "getRangeStart"), "method getRangeStart");
        check(hasM(ms, "isFrozen"), "method isFrozen");
        check(hasM(ms, "retainAll"), "method retainAll");
        check(hasM(ms, "set"), "method set");
        check(hasM(ms, "size"), "method size");
        check(hasM(ms, "span"), "method span");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 16, "declared fields >= 16");
        check(hasF(fs, "MAX_VALUE"), "field MAX_VALUE");
        check(hasF(fs, "MIN_VALUE"), "field MIN_VALUE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
