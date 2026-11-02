/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.RangeImpl conformance: class hierarchy and members.
 * @run main RangeImplTest
 */
public class RangeImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.RangeImpl", false, RangeImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "org.w3c.dom.ranges.Range"), "has interface org.w3c.dom.ranges.Range");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getStartContainer"), "method getStartContainer");
        check(mn.contains("getStartOffset"), "method getStartOffset");
        check(mn.contains("getEndContainer"), "method getEndContainer");
        check(mn.contains("getEndOffset"), "method getEndOffset");
        check(mn.contains("getCollapsed"), "method getCollapsed");
        check(mn.contains("getCommonAncestorContainer"), "method getCommonAncestorContainer");
        check(mn.contains("setStart"), "method setStart");
        check(mn.contains("setEnd"), "method setEnd");
        check(mn.contains("setStartBefore"), "method setStartBefore");
        check(mn.contains("setStartAfter"), "method setStartAfter");
        check(mn.contains("setEndBefore"), "method setEndBefore");
        check(mn.contains("setEndAfter"), "method setEndAfter");
        check(mn.contains("collapse"), "method collapse");
        check(mn.contains("selectNode"), "method selectNode");
        check(mn.contains("selectNodeContents"), "method selectNodeContents");
        check(mn.contains("compareBoundaryPoints"), "method compareBoundaryPoints");
        check(mn.contains("deleteContents"), "method deleteContents");
        check(mn.contains("extractContents"), "method extractContents");
        check(mn.contains("cloneContents"), "method cloneContents");
        check(mn.contains("insertNode"), "method insertNode");
        check(mn.contains("surroundContents"), "method surroundContents");
        check(mn.contains("cloneRange"), "method cloneRange");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("detach"), "method detach");
        check(mn.contains("signalSplitData"), "method signalSplitData");
        check(mn.contains("receiveSplitData"), "method receiveSplitData");
        check(mn.contains("deleteData"), "method deleteData");
        check(mn.contains("receiveDeletedText"), "method receiveDeletedText");
        check(mn.contains("insertData"), "method insertData");
        check(mn.contains("receiveInsertedText"), "method receiveInsertedText");
        check(mn.contains("receiveReplacedText"), "method receiveReplacedText");
        check(mn.contains("insertedNodeFromDOM"), "method insertedNodeFromDOM");
        check(mn.contains("removeChild"), "method removeChild");
        check(mn.contains("removeNode"), "method removeNode");
        check(mn.contains("checkIndex"), "method checkIndex");
        check(mn.contains("nextNode"), "method nextNode");
        check(mn.contains("isAncestorOf"), "method isAncestorOf");
        check(mn.contains("indexOf"), "method indexOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
