/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.KeyIndex conformance: reflection.
 * @run main KeyIndexTest
 */

public class KeyIndexTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.KeyIndex", false, KeyIndexTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("KeyIndex"), "simple name KeyIndex");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase"), "superclass com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "setRestartable"), "method setRestartable");
        check(has(c, "add"), "method add");
        check(has(c, "merge"), "method merge");
        check(has(c, "lookupId"), "method lookupId");
        check(has(c, "getDOMNodeById"), "method getDOMNodeById");
        check(has(c, "lookupKey"), "method lookupKey");
        check(has(c, "next"), "method next");
        check(has(c, "containsID"), "method containsID");
        check(has(c, "containsKey"), "method containsKey");
        check(has(c, "reset"), "method reset");
        check(has(c, "getLast"), "method getLast");
        check(has(c, "getPosition"), "method getPosition");
        check(has(c, "setMark"), "method setMark");
        check(has(c, "gotoMark"), "method gotoMark");
        check(has(c, "setStartNode"), "method setStartNode");
        check(has(c, "getStartNode"), "method getStartNode");
        check(has(c, "isReverse"), "method isReverse");
        check(has(c, "cloneIterator"), "method cloneIterator");
        check(has(c, "setDom"), "method setDom");
        check(has(c, "getKeyIndexIterator"), "method getKeyIndexIterator");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
