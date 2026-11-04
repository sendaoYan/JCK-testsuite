/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.util.ContiguousCharArrayArray conformance: class hierarchy and members.
 * @run main ContiguousCharArrayArrayTest
 */

public class ContiguousCharArrayArrayTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.util.ContiguousCharArrayArray", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.util.ContiguousCharArrayArray"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.fastinfoset.util.ValueArray".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.fastinfoset.util.ValueArray");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("add"), "has method add");
        check(mn.contains("clear"), "has method clear");
        check(mn.contains("ensureSize"), "has method ensureSize");
        check(mn.contains("getArrayIndex"), "has method getArrayIndex");
        check(mn.contains("getCompleteCharArray"), "has method getCompleteCharArray");
        check(mn.contains("getCompleteLengthArray"), "has method getCompleteLengthArray");
        check(mn.contains("getCompleteOffsetArray"), "has method getCompleteOffsetArray");
        check(mn.contains("getString"), "has method getString");
        check(mn.contains("resize"), "has method resize");
        check(mn.contains("resizeArray"), "has method resizeArray");
        check(mn.contains("setReadOnlyArray"), "has method setReadOnlyArray");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
