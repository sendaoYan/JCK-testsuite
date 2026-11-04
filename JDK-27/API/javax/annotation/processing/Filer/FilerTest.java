/*
 * @test
 * @summary javax.annotation.processing.Filer conformance: white-box structure + black-box behavior.
 * @run main FilerTest
 */

import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.tools.*;

public class FilerTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.annotation.processing.Filer");
        check(c != null, "javax.annotation.processing.Filer loads");
        check(c.getName().equals("javax.annotation.processing.Filer"), "binary name is javax.annotation.processing.Filer");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.annotation.processing.Filer is public");
        check(c.isInterface() && !c.isAnnotation(), "is a plain interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "createSourceFile"), "declares createSourceFile()");
        check(declares(c, "createClassFile"), "declares createClassFile()");
        check(declares(c, "createResource"), "declares createResource()");
        check(declares(c, "getResource"), "declares getResource()");
    }

    static void testBlackBox() throws Exception {
        final String[] name = new String[1];
        Filer f = new Filer() {
            public javax.tools.JavaFileObject createSourceFile(CharSequence n, Element... o) { name[0] = n.toString(); return null; }
            public javax.tools.JavaFileObject createClassFile(CharSequence n, Element... o) { return null; }
            public javax.tools.FileObject createResource(javax.tools.JavaFileManager.Location loc, CharSequence pkg, CharSequence rel, Element... o) { return null; }
            public javax.tools.FileObject getResource(javax.tools.JavaFileManager.Location loc, CharSequence pkg, CharSequence rel) { return null; }
        };
        try { f.createSourceFile("pkg.Foo"); } catch (java.io.IOException e) {}
        check("pkg.Foo".equals(name[0]), "createSourceFile records name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
