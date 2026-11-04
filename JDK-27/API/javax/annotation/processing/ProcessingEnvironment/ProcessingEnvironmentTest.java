/*
 * @test
 * @summary javax.annotation.processing.ProcessingEnvironment conformance: white-box structure + black-box behavior.
 * @run main ProcessingEnvironmentTest
 */

import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.tools.*;

public class ProcessingEnvironmentTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.annotation.processing.ProcessingEnvironment");
        check(c != null, "javax.annotation.processing.ProcessingEnvironment loads");
        check(c.getName().equals("javax.annotation.processing.ProcessingEnvironment"), "binary name is javax.annotation.processing.ProcessingEnvironment");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.annotation.processing.ProcessingEnvironment is public");
        check(c.isInterface() && !c.isAnnotation(), "is a plain interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getOptions"), "declares getOptions()");
        check(declares(c, "getMessager"), "declares getMessager()");
        check(declares(c, "getFiler"), "declares getFiler()");
        check(declares(c, "getElementUtils"), "declares getElementUtils()");
        check(declares(c, "getTypeUtils"), "declares getTypeUtils()");
        check(declares(c, "getSourceVersion"), "declares getSourceVersion()");
        check(declares(c, "getLocale"), "declares getLocale()");
        check(declares(c, "isPreviewEnabled"), "declares isPreviewEnabled()");
    }

    static void testBlackBox() throws Exception {
        ProcessingEnvironment pe = new ProcessingEnvironment() {
            public Map<String,String> getOptions() { return Map.of(); }
            public Messager getMessager() { return null; }
            public Filer getFiler() { return null; }
            public javax.lang.model.util.Elements getElementUtils() { return null; }
            public javax.lang.model.util.Types getTypeUtils() { return null; }
            public SourceVersion getSourceVersion() { return SourceVersion.RELEASE_17; }
            public java.util.Locale getLocale() { return java.util.Locale.ROOT; }
        };
        check(!pe.isPreviewEnabled(), "default isPreviewEnabled false");
        check(pe.getOptions().isEmpty(), "getOptions empty");
        check(pe.getSourceVersion() == SourceVersion.RELEASE_17, "getSourceVersion");
        check(pe.getLocale().equals(java.util.Locale.ROOT), "getLocale ROOT");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
