/*
 * @test
 * @summary jdk.internal.event.VirtualThreadSubmitFailedEvent conformance: internal package reflection.
 * @modules java.base/jdk.internal.event:+open
 * @run main VirtualThreadSubmitFailedEventTest
 */
public class VirtualThreadSubmitFailedEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.event.VirtualThreadSubmitFailedEvent", false, VirtualThreadSubmitFailedEventTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.event.Event"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "begin"), "method begin");
        check(hasMethod(c, "commit"), "method commit");
        check(hasMethod(c, "end"), "method end");
        check(hasMethod(c, "isEnabled"), "method isEnabled");
        check(hasMethod(c, "shouldCommit"), "method shouldCommit");
        check(hasField(c, "duration"), "field duration");
        check(hasField(c, "eventConfiguration"), "field eventConfiguration");
        check(hasField(c, "exceptionMessage"), "field exceptionMessage");
        check(hasField(c, "javaThreadId"), "field javaThreadId");
        check(hasField(c, "startTime"), "field startTime");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
