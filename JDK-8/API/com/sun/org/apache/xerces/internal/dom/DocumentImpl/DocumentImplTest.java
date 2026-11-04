/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.DocumentImpl conformance: class hierarchy and members.
 * @run main DocumentImplTest
 */
public class DocumentImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.DocumentImpl", false, DocumentImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl"), "superclass com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl");
        check(hasInterface(c, "org.w3c.dom.traversal.DocumentTraversal"), "has interface org.w3c.dom.traversal.DocumentTraversal");
        check(hasInterface(c, "org.w3c.dom.events.DocumentEvent"), "has interface org.w3c.dom.events.DocumentEvent");
        check(hasInterface(c, "org.w3c.dom.ranges.DocumentRange"), "has interface org.w3c.dom.ranges.DocumentRange");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("cloneNode"), "method cloneNode");
        check(mn.contains("getImplementation"), "method getImplementation");
        check(mn.contains("createNodeIterator"), "method createNodeIterator");
        check(mn.contains("createTreeWalker"), "method createTreeWalker");
        check(mn.contains("removeNodeIterator"), "method removeNodeIterator");
        check(mn.contains("createRange"), "method createRange");
        check(mn.contains("removeRange"), "method removeRange");
        check(mn.contains("replacedText"), "method replacedText");
        check(mn.contains("deletedText"), "method deletedText");
        check(mn.contains("insertedText"), "method insertedText");
        check(mn.contains("splitData"), "method splitData");
        check(mn.contains("createEvent"), "method createEvent");
        check(mn.contains("setMutationEvents"), "method setMutationEvents");
        check(mn.contains("getMutationEvents"), "method getMutationEvents");
        check(mn.contains("addEventListener"), "method addEventListener");
        check(mn.contains("removeEventListener"), "method removeEventListener");
        check(mn.contains("copyEventListeners"), "method copyEventListeners");
        check(mn.contains("dispatchEvent"), "method dispatchEvent");
        check(mn.contains("dispatchEventToSubtree"), "method dispatchEventToSubtree");
        check(mn.contains("dispatchingEventToSubtree"), "method dispatchingEventToSubtree");
        check(mn.contains("dispatchAggregateEvents"), "method dispatchAggregateEvents");
        check(mn.contains("saveEnclosingAttr"), "method saveEnclosingAttr");
        check(mn.contains("modifyingCharacterData"), "method modifyingCharacterData");
        check(mn.contains("modifiedCharacterData"), "method modifiedCharacterData");
        check(mn.contains("replacedCharacterData"), "method replacedCharacterData");
        check(mn.contains("insertingNode"), "method insertingNode");
        check(mn.contains("insertedNode"), "method insertedNode");
        check(mn.contains("removingNode"), "method removingNode");
        check(mn.contains("removedNode"), "method removedNode");
        check(mn.contains("replacingNode"), "method replacingNode");
        check(mn.contains("replacingData"), "method replacingData");
        check(mn.contains("replacedNode"), "method replacedNode");
        check(mn.contains("modifiedAttrValue"), "method modifiedAttrValue");
        check(mn.contains("setAttrNode"), "method setAttrNode");
        check(mn.contains("removedAttrNode"), "method removedAttrNode");
        check(mn.contains("renamedAttrNode"), "method renamedAttrNode");
        check(mn.contains("renamedElement"), "method renamedElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
