/*
 * @test
 * @summary javax.naming.RefAddr conformance: abstract class hierarchy and
 *          members, plus behavioral equals/hashCode/toString using a concrete
 *          subclass.
 * @run main RefAddrTest
 */

import javax.naming.RefAddr;

public class RefAddrTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static class TestAddr extends RefAddr {
        TestAddr(String type, String content) { super(type); this.content = content; }
        String content;
        public Object getContent() { return content; }
        private static final long serialVersionUID = 1L;
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testHierarchy() throws Exception {
        Class<?> c = Class.forName("javax.naming.RefAddr");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.io.Serializable");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getContent"), "has method getContent");
        check(names.contains("getType"), "has method getType");
    }

    static void testBehavioral() {
        TestAddr a = new TestAddr("TYPE", "VAL");
        check("TYPE".equals(a.getType()), "getType");
        check("VAL".equals(a.getContent()), "getContent");
        TestAddr same = new TestAddr("TYPE", "VAL");
        TestAddr diffType = new TestAddr("TYPE2", "VAL");
        TestAddr diffContent = new TestAddr("TYPE", "VAL2");
        check(a.equals(same) && same.equals(a), "equals symmetric");
        check(a.hashCode() == same.hashCode(), "hashCode equal");
        check(!a.equals(diffType), "different type");
        check(!a.equals(diffContent), "different content");
        check(!a.equals(null) && !a.equals("TYPE"), "equals null/non-RefAddr");
        String s = a.toString();
        check(s.contains("Type: TYPE"), "toString type");
        check(s.contains("Content: VAL"), "toString content");
    }

    public static void main(String[] args) throws Exception {
        testHierarchy();
        testBehavioral();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
