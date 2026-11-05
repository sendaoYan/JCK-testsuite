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

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

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
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(org.w3c.dom.xpath.XPathResult.ANY_TYPE == 0, "ANY_TYPE == 0");
        check(org.w3c.dom.xpath.XPathResult.NUMBER_TYPE == 1, "NUMBER_TYPE == 1");
        check(org.w3c.dom.xpath.XPathResult.STRING_TYPE == 2, "STRING_TYPE == 2");
        check(org.w3c.dom.xpath.XPathResult.BOOLEAN_TYPE == 3, "BOOLEAN_TYPE == 3");
        check(org.w3c.dom.xpath.XPathResult.UNORDERED_NODE_ITERATOR_TYPE == 4, "UNORDERED_NODE_ITERATOR_TYPE == 4");
        check(org.w3c.dom.xpath.XPathResult.ORDERED_NODE_ITERATOR_TYPE == 5, "ORDERED_NODE_ITERATOR_TYPE == 5");
        check(org.w3c.dom.xpath.XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE == 6, "UNORDERED_NODE_SNAPSHOT_TYPE == 6");
        check(org.w3c.dom.xpath.XPathResult.ORDERED_NODE_SNAPSHOT_TYPE == 7, "ORDERED_NODE_SNAPSHOT_TYPE == 7");
        check(org.w3c.dom.xpath.XPathResult.ANY_UNORDERED_NODE_TYPE == 8, "ANY_UNORDERED_NODE_TYPE == 8");
        check(org.w3c.dom.xpath.XPathResult.FIRST_ORDERED_NODE_TYPE == 9, "FIRST_ORDERED_NODE_TYPE == 9");
        org.w3c.dom.xpath.XPathResult a = new org.w3c.dom.xpath.XPathResult(){
            public short getResultType() { return (short)2; }
            public double getNumberValue() { return 6.25; }
            public java.lang.String getStringValue() { return "s"; }
            public boolean getBooleanValue() { return true; }
            public org.w3c.dom.Node getSingleNodeValue() { return null; }
            public boolean getInvalidIteratorState() { return true; }
            public int getSnapshotLength() { return 3; }
            public org.w3c.dom.Node iterateNext() { return null; }
            public org.w3c.dom.Node snapshotItem(int p0) { return null; }
        };
        check(a != null, "anon impl of XPathResult");
        check(a.getResultType() == (short)2, "getResultType() returns fixed default");
        check(a.getNumberValue() == 6.25, "getNumberValue() returns fixed default");
        check("s".equals(a.getStringValue()), "getStringValue() returns fixed default");
        check(a.getBooleanValue() == true, "getBooleanValue() returns fixed default");
        check(a.getSingleNodeValue() == null, "getSingleNodeValue() returns fixed default");
        check(a.getInvalidIteratorState() == true, "getInvalidIteratorState() returns fixed default");
        check(a.getSnapshotLength() == 3, "getSnapshotLength() returns fixed default");
        check(a.iterateNext() == null, "iterateNext() returns fixed default");
        check(a.snapshotItem(3) == null, "snapshotItem() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: XPathResult contract (constant table + shape) ----
    static void deepContract() throws Exception {
        check(org.w3c.dom.xpath.XPathResult.ANY_TYPE == 0, "ANY_TYPE = 0");
        check(org.w3c.dom.xpath.XPathResult.NUMBER_TYPE == 1, "NUMBER_TYPE = 1");
        check(org.w3c.dom.xpath.XPathResult.STRING_TYPE == 2, "STRING_TYPE = 2");
        check(org.w3c.dom.xpath.XPathResult.BOOLEAN_TYPE == 3, "BOOLEAN_TYPE = 3");
        check(org.w3c.dom.xpath.XPathResult.UNORDERED_NODE_ITERATOR_TYPE == 4, "UNORDERED_NODE_ITERATOR_TYPE = 4");
        check(org.w3c.dom.xpath.XPathResult.ORDERED_NODE_ITERATOR_TYPE == 5, "ORDERED_NODE_ITERATOR_TYPE = 5");
        check(org.w3c.dom.xpath.XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE == 6, "UNORDERED_NODE_SNAPSHOT_TYPE = 6");
        check(org.w3c.dom.xpath.XPathResult.ORDERED_NODE_SNAPSHOT_TYPE == 7, "ORDERED_NODE_SNAPSHOT_TYPE = 7");
        check(org.w3c.dom.xpath.XPathResult.ANY_UNORDERED_NODE_TYPE == 8, "ANY_UNORDERED_NODE_TYPE = 8");
        check(org.w3c.dom.xpath.XPathResult.FIRST_ORDERED_NODE_TYPE == 9, "FIRST_ORDERED_NODE_TYPE = 9");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.xpath.XPathResult.class.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getResultType") && names.contains("getNumberValue") && names.contains("getStringValue"), "typed getters declared (number/string)");
        check(names.contains("getBooleanValue") && names.contains("getSingleNodeValue") && names.contains("getInvalidIteratorState"), "typed getters declared (bool/single/iterator-state)");
        check(names.contains("getSnapshotLength") && names.contains("iterateNext") && names.contains("snapshotItem"), "iteration/snapshot methods declared");
        // anonymous implementation honors the typed-getter contract
        org.w3c.dom.xpath.XPathResult res = new org.w3c.dom.xpath.XPathResult() {
            public short getResultType() { return org.w3c.dom.xpath.XPathResult.STRING_TYPE; }
            public double getNumberValue() { return 1.5; }
            public String getStringValue() { return "sv"; }
            public boolean getBooleanValue() { return true; }
            public org.w3c.dom.Node getSingleNodeValue() { return null; }
            public boolean getInvalidIteratorState() { return false; }
            public int getSnapshotLength() { return 0; }
            public org.w3c.dom.Node iterateNext() { return null; }
            public org.w3c.dom.Node snapshotItem(int i) { return null; }
        };
        check(res.getResultType() == 2 && res.getNumberValue() == 1.5 && "sv".equals(res.getStringValue()), "anonymous XPathResult typed getters");
        check(res.getBooleanValue() && !res.getInvalidIteratorState() && res.snapshotItem(0) == null, "anonymous XPathResult remaining getters");
    }
}
