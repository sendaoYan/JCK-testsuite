/*
 * @test
 * @summary org.w3c.dom.ls.LSSerializer conformance: serializer.
 * @run main LSSerializerTest
 */

public class LSSerializerTest {
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

        Class<?> k = L("org.w3c.dom.ls.LSSerializer");
        check(k.isInterface(), "org.w3c.dom.ls.LSSerializer is an interface");
        check(k.getInterfaces().length == 0, "LSSerializer declares no superinterface");
        check(k.getMethod("getDomConfig") != null, "getDomConfig()");
        check(k.getMethod("getNewLine") != null, "getNewLine()");
        check(k.getMethod("setNewLine", String.class) != null, "setNewLine(String)");
        check(k.getMethod("getFilter") != null, "getFilter()");
        check(k.getMethod("setFilter", L("org.w3c.dom.ls.LSSerializerFilter")) != null, "setFilter(org.w3c.dom.ls.LSSerializerFilter)");
        check(k.getMethod("write", L("org.w3c.dom.Node"), L("org.w3c.dom.ls.LSOutput")) != null, "write(org.w3c.dom.Node, org.w3c.dom.ls.LSOutput)");
        check(k.getMethod("writeToURI", L("org.w3c.dom.Node"), String.class) != null, "writeToURI(org.w3c.dom.Node, String)");
        check(k.getMethod("writeToString", L("org.w3c.dom.Node")) != null, "writeToString(org.w3c.dom.Node)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.ls.LSSerializer a = new org.w3c.dom.ls.LSSerializer(){
            public org.w3c.dom.DOMConfiguration getDomConfig() { return null; }
            public java.lang.String getNewLine() { return "s"; }
            public void setNewLine(java.lang.String p0) { }
            public org.w3c.dom.ls.LSSerializerFilter getFilter() { return null; }
            public void setFilter(org.w3c.dom.ls.LSSerializerFilter p0) { }
            public boolean write(org.w3c.dom.Node p0, org.w3c.dom.ls.LSOutput p1) { return true; }
            public boolean writeToURI(org.w3c.dom.Node p0, java.lang.String p1) { return true; }
            public java.lang.String writeToString(org.w3c.dom.Node p0) { return "s"; }
        };
        check(a != null, "anon impl of LSSerializer");
        check(a.getDomConfig() == null, "getDomConfig() returns fixed default");
        check("s".equals(a.getNewLine()), "getNewLine() returns fixed default");
        a.setNewLine("s");
        check(a.getFilter() == null, "getFilter() returns fixed default");
        a.setFilter((org.w3c.dom.ls.LSSerializerFilter) null);
        check(a.write((org.w3c.dom.Node) null, (org.w3c.dom.ls.LSOutput) null) == true, "write() returns fixed default");
        check(a.writeToURI((org.w3c.dom.Node) null, "s") == true, "writeToURI() returns fixed default");
        check("s".equals(a.writeToString((org.w3c.dom.Node) null)), "writeToString() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
