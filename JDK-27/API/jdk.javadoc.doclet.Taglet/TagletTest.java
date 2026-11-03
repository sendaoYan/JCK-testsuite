/*
 * @test
 * @summary jdk.javadoc.doclet.Taglet conformance: white-box structure + black-box behavior.
 * @run main TagletTest
 */

import jdk.javadoc.doclet.*;
import javax.tools.*;
import javax.lang.model.*;
import javax.lang.model.element.Element;
import com.sun.source.doctree.*;
import com.sun.source.util.*;
import java.net.*;
import java.lang.reflect.*;
import java.util.*;
public class TagletTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.doclet.Taglet");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "getAllowedLocations"), "getAllowedLocations");
        check(hasMethod(c, "isInlineTag"), "isInlineTag");
        check(hasMethod(c, "isBlockTag"), "isBlockTag");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "init"), "init");
        check(hasMethod(c, "toString"), "toString");

    }

    static void testBlackBox() throws Exception {
        Taglet t = new Taglet() {
            public Set<Taglet.Location> getAllowedLocations() { return Collections.emptySet(); }
            public boolean isInlineTag() { return false; }
            public String getName() { return "mytag"; }
            public String toString(List<? extends DocTree> tags, Element el) { return "x"; }
        };
        check("mytag".equals(t.getName()), "getName");
        check(!t.isInlineTag(), "isInlineTag false");
        check(t.isBlockTag(), "default isBlockTag true");
        check(t.getAllowedLocations() != null, "locations");
        t.init(null, null);
        check(true, "default init callable");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
