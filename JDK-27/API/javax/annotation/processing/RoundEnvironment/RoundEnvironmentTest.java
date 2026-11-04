/*
 * @test
 * @summary javax.annotation.processing.RoundEnvironment conformance: white-box structure + black-box behavior.
 * @run main RoundEnvironmentTest
 */

import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.tools.*;

public class RoundEnvironmentTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.annotation.processing.RoundEnvironment");
        check(c != null, "javax.annotation.processing.RoundEnvironment loads");
        check(c.getName().equals("javax.annotation.processing.RoundEnvironment"), "binary name is javax.annotation.processing.RoundEnvironment");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.annotation.processing.RoundEnvironment is public");
        check(c.isInterface() && !c.isAnnotation(), "is a plain interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "processingOver"), "declares processingOver()");
        check(declares(c, "errorRaised"), "declares errorRaised()");
        check(declares(c, "getRootElements"), "declares getRootElements()");
        check(declares(c, "getElementsAnnotatedWith"), "declares getElementsAnnotatedWith()");
        check(declares(c, "getElementsAnnotatedWithAny"), "declares getElementsAnnotatedWithAny()");
    }

    static void testBlackBox() throws Exception {
        final Set<? extends Element> empty = Set.of();
        RoundEnvironment re = new RoundEnvironment() {
            public boolean processingOver() { return true; }
            public boolean errorRaised() { return false; }
            public Set<? extends Element> getRootElements() { return empty; }
            public Set<? extends Element> getElementsAnnotatedWith(TypeElement a) { return empty; }
            public Set<? extends Element> getElementsAnnotatedWith(Class<? extends java.lang.annotation.Annotation> a) { return empty; }
        };
        check(re.processingOver() && !re.errorRaised(), "processingOver/errorRaised");
        check(re.getRootElements().isEmpty(), "getRootElements empty");
        check(re.getElementsAnnotatedWithAny().isEmpty(), "getElementsAnnotatedWithAny empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
