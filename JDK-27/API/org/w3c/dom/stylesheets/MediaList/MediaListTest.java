/*
 * @test
 * @summary org.w3c.dom.stylesheets.MediaList conformance: interface and methods.
 * @run main MediaListTest
 */

import org.w3c.dom.stylesheets.MediaList;

public class MediaListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(MediaList.class.isInterface(), "MediaList is an interface");
        check(MediaList.class.getMethod("getMediaText") != null, "getMediaText()");
        check(MediaList.class.getMethod("getLength").getReturnType() == int.class,
              "getLength() returns int");
        check(MediaList.class.getMethod("item", int.class)
                .getReturnType() == String.class, "item(int) returns String");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.stylesheets.MediaList a = new org.w3c.dom.stylesheets.MediaList(){
            public java.lang.String getMediaText() { return "s"; }
            public void setMediaText(java.lang.String p0) { }
            public int getLength() { return 3; }
            public java.lang.String item(int p0) { return "s"; }
            public void deleteMedium(java.lang.String p0) { }
            public void appendMedium(java.lang.String p0) { }
        };
        check(a != null, "anon impl of MediaList");
        check("s".equals(a.getMediaText()), "getMediaText() returns fixed default");
        a.setMediaText("s");
        check(a.getLength() == 3, "getLength() returns fixed default");
        check("s".equals(a.item(3)), "item() returns fixed default");
        a.deleteMedium("s");
        a.appendMedium("s");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: MediaList contract (behavioral mini-list) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("StyleSheets", "2.0") && impl.getFeature("StyleSheets", "2.0") == null, "StyleSheets module unimplemented");
        final java.util.List<String> media = new java.util.ArrayList<String>(java.util.Arrays.asList("screen", "print"));
        org.w3c.dom.stylesheets.MediaList ml = new org.w3c.dom.stylesheets.MediaList() {
            public String getMediaText() { return media.isEmpty() ? "" : media.toString().replace("[", "").replace("]", "").replace(", ", ","); }
            public void setMediaText(String t) { media.clear(); media.addAll(java.util.Arrays.asList(t.split(","))); }
            public int getLength() { return media.size(); }
            public String item(int i) { return i >= 0 && i < media.size() ? media.get(i) : null; }
            public void deleteMedium(String m) { media.remove(m); }
            public void appendMedium(String m) { media.add(m); }
        };
        check(ml.getLength() == 2 && "screen".equals(ml.item(0)) && "print".equals(ml.item(1)), "initial entries in order");
        ml.appendMedium("handheld");
        check(ml.getLength() == 3 && "handheld".equals(ml.item(2)), "appendMedium appends at the end");
        ml.deleteMedium("screen");
        check(ml.getLength() == 2 && "print".equals(ml.item(0)), "deleteMedium removes by value");
        check(ml.item(-1) == null && ml.item(9) == null, "out-of-range item -> null");
        check(ml.getMediaText().contains("print"), "media text joins the entries");
        ml.setMediaText("all");
        check(ml.getLength() == 1 && "all".equals(ml.item(0)), "setMediaText replaces the list");
    }
}
