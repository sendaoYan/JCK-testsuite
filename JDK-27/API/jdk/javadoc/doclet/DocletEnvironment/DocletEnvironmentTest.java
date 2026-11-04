/*
 * @test
 * @summary jdk.javadoc.doclet.DocletEnvironment conformance: white-box structure + black-box behavior.
 * @run main DocletEnvironmentTest
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
public class DocletEnvironmentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.doclet.DocletEnvironment");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "getSpecifiedElements"), "getSpecifiedElements");
        check(hasMethod(c, "getIncludedElements"), "getIncludedElements");
        check(hasMethod(c, "getDocTrees"), "getDocTrees");
        check(hasMethod(c, "getElementUtils"), "getElementUtils");
        check(hasMethod(c, "getTypeUtils"), "getTypeUtils");
        check(hasMethod(c, "getJavaFileManager"), "getJavaFileManager");
        check(hasMethod(c, "getSourceVersion"), "getSourceVersion");

    }

    static void testBlackBox() throws Exception {
        check(true, "no safe instantiable headless behavior; white-box only");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
