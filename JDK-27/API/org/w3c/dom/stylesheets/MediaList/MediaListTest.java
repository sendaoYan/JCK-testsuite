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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
