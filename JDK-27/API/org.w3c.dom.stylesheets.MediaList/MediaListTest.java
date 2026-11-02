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

    public static void main(String[] args) throws Exception {
        check(MediaList.class.isInterface(), "MediaList is an interface");
        check(MediaList.class.getMethod("getMediaText") != null, "getMediaText()");
        check(MediaList.class.getMethod("getLength").getReturnType() == int.class,
              "getLength() returns int");
        check(MediaList.class.getMethod("item", int.class)
                .getReturnType() == String.class, "item(int) returns String");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
