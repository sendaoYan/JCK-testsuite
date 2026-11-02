/*
 * @test
 * @summary org.w3c.dom.xpath.XPathResult conformance: result type constants.
 * @run main XPathResultTest
 */

public class XPathResultTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    static Class<?> L(String n) throws Exception { return Class.forName(n); }

    static short sconst(String c, String f) throws Exception {
        return Class.forName(c).getField(f).getShort(null);
    }

    public static void main(String[] args) throws Exception {
        Class<?> k = L("org.w3c.dom.xpath.XPathResult");
        check(k.isInterface(), "org.w3c.dom.xpath.XPathResult is an interface");
        check(k.getInterfaces().length == 0, "XPathResult declares no superinterface");
        check(sconst("org.w3c.dom.xpath.XPathResult", "ANY_TYPE") == 0, "ANY_TYPE == 0");
        check(sconst("org.w3c.dom.xpath.XPathResult", "NUMBER_TYPE") == 1, "NUMBER_TYPE == 1");
        check(sconst("org.w3c.dom.xpath.XPathResult", "STRING_TYPE") == 2, "STRING_TYPE == 2");
        check(sconst("org.w3c.dom.xpath.XPathResult", "BOOLEAN_TYPE") == 3, "BOOLEAN_TYPE == 3");
        check(sconst("org.w3c.dom.xpath.XPathResult", "UNORDERED_NODE_ITERATOR_TYPE") == 4, "UNORDERED_NODE_ITERATOR_TYPE == 4");
        check(sconst("org.w3c.dom.xpath.XPathResult", "ORDERED_NODE_ITERATOR_TYPE") == 5, "ORDERED_NODE_ITERATOR_TYPE == 5");
        check(sconst("org.w3c.dom.xpath.XPathResult", "UNORDERED_NODE_SNAPSHOT_TYPE") == 6, "UNORDERED_NODE_SNAPSHOT_TYPE == 6");
        check(sconst("org.w3c.dom.xpath.XPathResult", "ORDERED_NODE_SNAPSHOT_TYPE") == 7, "ORDERED_NODE_SNAPSHOT_TYPE == 7");
        check(sconst("org.w3c.dom.xpath.XPathResult", "ANY_UNORDERED_NODE_TYPE") == 8, "ANY_UNORDERED_NODE_TYPE == 8");
        check(sconst("org.w3c.dom.xpath.XPathResult", "FIRST_ORDERED_NODE_TYPE") == 9, "FIRST_ORDERED_NODE_TYPE == 9");
        check(k.getMethod("getResultType") != null, "getResultType()");
        check(k.getMethod("getNumberValue") != null, "getNumberValue()");
        check(k.getMethod("getStringValue") != null, "getStringValue()");
        check(k.getMethod("getBooleanValue") != null, "getBooleanValue()");
        check(k.getMethod("getSingleNodeValue") != null, "getSingleNodeValue()");
        check(k.getMethod("getInvalidIteratorState") != null, "getInvalidIteratorState()");
        check(k.getMethod("getSnapshotLength") != null, "getSnapshotLength()");
        check(k.getMethod("iterateNext") != null, "iterateNext()");
        check(k.getMethod("snapshotItem", int.class) != null, "snapshotItem(int)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
