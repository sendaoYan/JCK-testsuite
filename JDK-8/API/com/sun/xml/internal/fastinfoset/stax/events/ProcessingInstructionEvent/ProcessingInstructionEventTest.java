/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.stax.events.ProcessingInstructionEvent conformance: class hierarchy and members.
 * @run main ProcessingInstructionEventTest
 */

public class ProcessingInstructionEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.stax.events.ProcessingInstructionEvent", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.stax.events.ProcessingInstructionEvent"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.fastinfoset.stax.events.EventBase".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.fastinfoset.stax.events.EventBase");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.stream.events.ProcessingInstruction"), "has interface javax.xml.stream.events.ProcessingInstruction");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getData"), "has method getData");
        check(mn.contains("getTarget"), "has method getTarget");
        check(mn.contains("init"), "has method init");
        check(mn.contains("setData"), "has method setData");
        check(mn.contains("setTarget"), "has method setTarget");
        check(mn.contains("toString"), "has method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
