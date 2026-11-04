/*
 * @test
 * @summary jdk.internal.classfile.impl.ClassPrinterImpl.MapNodeImpl conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main MapNodeImplTest
 */
public class MapNodeImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.ClassPrinterImpl$MapNodeImpl", false, MapNodeImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.ClassPrinterImpl$MapNodeImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "jdk.internal.classfile.components.ClassPrinter$MapNode"), "interface jdk.internal.classfile.components.ClassPrinter$MapNode");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 19, "declared methods >= 19");
        check(hasM(ms, "clear"), "method clear");
        check(hasM(ms, "containsKey"), "method containsKey");
        check(hasM(ms, "containsValue"), "method containsValue");
        check(hasM(ms, "entrySet"), "method entrySet");
        check(hasM(ms, "get"), "method get");
        check(hasM(ms, "isEmpty"), "method isEmpty");
        check(hasM(ms, "keySet"), "method keySet");
        check(hasM(ms, "name"), "method name");
        check(hasM(ms, "put"), "method put");
        check(hasM(ms, "putAll"), "method putAll");
        check(hasM(ms, "remove"), "method remove");
        check(hasM(ms, "size"), "method size");
        check(hasM(ms, "style"), "method style");
        check(hasM(ms, "values"), "method values");
        check(hasM(ms, "walk"), "method walk");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 3, "declared fields >= 3");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
