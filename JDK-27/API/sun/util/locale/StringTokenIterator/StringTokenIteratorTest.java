/*
 * @test
 * @summary sun.util.locale.StringTokenIterator conformance: internal package reflection.
 * @modules java.base/sun.util.locale:+open
 * @run main StringTokenIteratorTest
 */
public class StringTokenIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.StringTokenIterator", false, StringTokenIteratorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "current"), "method current");
        check(hasMethod(c, "currentEnd"), "method currentEnd");
        check(hasMethod(c, "currentStart"), "method currentStart");
        check(hasMethod(c, "first"), "method first");
        check(hasMethod(c, "hasNext"), "method hasNext");
        check(hasMethod(c, "isDone"), "method isDone");
        check(hasMethod(c, "next"), "method next");
        check(hasMethod(c, "setStart"), "method setStart");
        check(hasField(c, "delimiterChar"), "field delimiterChar");
        check(hasField(c, "dlms"), "field dlms");
        check(hasField(c, "done"), "field done");
        check(hasField(c, "end"), "field end");
        check(hasField(c, "start"), "field start");
        check(hasField(c, "text"), "field text");
        check(hasField(c, "token"), "field token");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
