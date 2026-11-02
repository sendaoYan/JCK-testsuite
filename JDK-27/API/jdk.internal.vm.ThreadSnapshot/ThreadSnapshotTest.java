/*
 * @test
 * @summary jdk.internal.vm.ThreadSnapshot conformance: internal package reflection.
 * @modules java.base/jdk.internal.vm:+open
 * @run main ThreadSnapshotTest
 */
public class ThreadSnapshotTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.vm.ThreadSnapshot", false, ThreadSnapshotTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "of"), "method of");
        check(hasField(c, "EMPTY_LOCKS"), "field EMPTY_LOCKS");
        check(hasField(c, "EMPTY_STACK"), "field EMPTY_STACK");
        check(hasField(c, "JLA"), "field JLA");
        check(hasField(c, "blocker"), "field blocker");
        check(hasField(c, "blockerObject"), "field blockerObject");
        check(hasField(c, "blockerTypeOrdinal"), "field blockerTypeOrdinal");
        check(hasField(c, "carrierThread"), "field carrierThread");
        check(hasField(c, "locks"), "field locks");
        check(hasField(c, "name"), "field name");
        check(hasField(c, "parkBlockerOwner"), "field parkBlockerOwner");
        check(hasField(c, "stackTrace"), "field stackTrace");
        check(hasField(c, "threadStatus"), "field threadStatus");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
