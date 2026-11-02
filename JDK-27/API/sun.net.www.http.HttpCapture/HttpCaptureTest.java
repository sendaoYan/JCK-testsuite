/*
 * @test
 * @summary sun.net.www.http.HttpCapture conformance: internal package reflection.
 * @modules java.base/sun.net.www.http:+open
 * @run main HttpCaptureTest
 */
public class HttpCaptureTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.http.HttpCapture", false, HttpCaptureTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "flush"), "method flush");
        check(hasMethod(c, "getCapture"), "method getCapture");
        check(hasMethod(c, "received"), "method received");
        check(hasMethod(c, "sent"), "method sent");
        check(hasField(c, "capFiles"), "field capFiles");
        check(hasField(c, "file"), "field file");
        check(hasField(c, "incoming"), "field incoming");
        check(hasField(c, "initialized"), "field initialized");
        check(hasField(c, "out"), "field out");
        check(hasField(c, "patterns"), "field patterns");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
