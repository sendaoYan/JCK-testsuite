/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.events.EventImpl conformance: class hierarchy and members.
 * @run main EventImplTest
 */
public class EventImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.events.EventImpl", false, EventImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.w3c.dom.events.Event"), "has interface org.w3c.dom.events.Event");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("initEvent"), "method initEvent");
        check(mn.contains("getBubbles"), "method getBubbles");
        check(mn.contains("getCancelable"), "method getCancelable");
        check(mn.contains("getCurrentTarget"), "method getCurrentTarget");
        check(mn.contains("getEventPhase"), "method getEventPhase");
        check(mn.contains("getTarget"), "method getTarget");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("getTimeStamp"), "method getTimeStamp");
        check(mn.contains("stopPropagation"), "method stopPropagation");
        check(mn.contains("preventDefault"), "method preventDefault");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
