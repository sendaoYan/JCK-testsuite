/*
 * @test
 * @summary jdk.internal.event.DeserializationEvent conformance: internal package reflection.
 * @modules java.base/jdk.internal.event:+open
 * @run main DeserializationEventTest
 */
public class DeserializationEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.event.DeserializationEvent", false, DeserializationEventTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.event.Event"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "begin"), "method begin");
        check(hasMethod(c, "commit"), "method commit");
        check(hasMethod(c, "end"), "method end");
        check(hasMethod(c, "isEnabled"), "method isEnabled");
        check(hasMethod(c, "shouldCommit"), "method shouldCommit");
        check(hasField(c, "arrayLength"), "field arrayLength");
        check(hasField(c, "bytesRead"), "field bytesRead");
        check(hasField(c, "depth"), "field depth");
        check(hasField(c, "duration"), "field duration");
        check(hasField(c, "eventConfiguration"), "field eventConfiguration");
        check(hasField(c, "exceptionMessage"), "field exceptionMessage");
        check(hasField(c, "exceptionType"), "field exceptionType");
        check(hasField(c, "filterConfigured"), "field filterConfigured");
        check(hasField(c, "filterStatus"), "field filterStatus");
        check(hasField(c, "objectReferences"), "field objectReferences");
        check(hasField(c, "startTime"), "field startTime");
        check(hasField(c, "type"), "field type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
