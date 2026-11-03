/*
 * @test
 * @summary javax.annotation.processing.AbstractProcessor conformance: white-box structure + black-box behavior.
 * @run main AbstractProcessorTest
 */

import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.tools.*;

public class AbstractProcessorTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.annotation.processing.AbstractProcessor");
        check(c != null, "javax.annotation.processing.AbstractProcessor loads");
        check(c.getName().equals("javax.annotation.processing.AbstractProcessor"), "binary name is javax.annotation.processing.AbstractProcessor");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.annotation.processing.AbstractProcessor is public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(ifaceNames(c).contains("javax.annotation.processing.Processor"), "implements javax.annotation.processing.Processor");
        check(declares(c, "getSupportedOptions"), "declares getSupportedOptions()");
        check(declares(c, "getSupportedAnnotationTypes"), "declares getSupportedAnnotationTypes()");
        check(declares(c, "getSupportedSourceVersion"), "declares getSupportedSourceVersion()");
        check(declares(c, "init"), "declares init()");
        check(declares(c, "process"), "declares process()");
        check(declares(c, "getCompletions"), "declares getCompletions()");
        check(declares(c, "isInitialized"), "declares isInitialized()");
    }

    static void testBlackBox() throws Exception {
        @SupportedAnnotationTypes("example.*")
        @SupportedOptions("opt1")
        @SupportedSourceVersion(SourceVersion.RELEASE_17)
        class P extends AbstractProcessor {
            public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) { return true; }
            public boolean inited() { return isInitialized(); }
        }
        class P2 extends AbstractProcessor {
            public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) { return false; }
        }
        P p = new P();
        check(!p.inited(), "not initialized before init");
        check(p.getSupportedAnnotationTypes().equals(Set.of("example.*")), "annotation -> annotation types");
        check(p.getSupportedOptions().equals(Set.of("opt1")), "annotation -> options");
        check(p.getSupportedSourceVersion() == SourceVersion.RELEASE_17, "annotation -> source version");
        check(!p.getCompletions(null, null, null, "").iterator().hasNext(), "getCompletions empty");
        P2 p2 = new P2();
        check(p2.getSupportedAnnotationTypes().equals(Set.of()), "no annotation -> empty types");
        check(p2.getSupportedOptions().equals(Set.of()), "no annotation -> empty options");
        check(p2.getSupportedSourceVersion() == SourceVersion.RELEASE_6, "no annotation -> RELEASE_6");
        ProcessingEnvironment pe = new ProcessingEnvironment() {
            public Map<String,String> getOptions() { return Map.of(); }
            public Messager getMessager() { return null; }
            public Filer getFiler() { return null; }
            public javax.lang.model.util.Elements getElementUtils() { return null; }
            public javax.lang.model.util.Types getTypeUtils() { return null; }
            public SourceVersion getSourceVersion() { return SourceVersion.RELEASE_17; }
            public java.util.Locale getLocale() { return java.util.Locale.ROOT; }
        };
        p.init(pe);
        check(p.inited(), "initialized after init");
        boolean threw = false;
        try { p.init(pe); } catch (IllegalStateException e) { threw = true; }
        check(threw, "double init throws IllegalStateException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
