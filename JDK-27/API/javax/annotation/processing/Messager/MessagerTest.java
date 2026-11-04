/*
 * @test
 * @summary javax.annotation.processing.Messager conformance: white-box structure + black-box behavior.
 * @run main MessagerTest
 */

import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.tools.*;

public class MessagerTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.annotation.processing.Messager");
        check(c != null, "javax.annotation.processing.Messager loads");
        check(c.getName().equals("javax.annotation.processing.Messager"), "binary name is javax.annotation.processing.Messager");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.annotation.processing.Messager is public");
        check(c.isInterface() && !c.isAnnotation(), "is a plain interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "printMessage"), "declares printMessage()");
        check(declares(c, "printError"), "declares printError()");
        check(declares(c, "printWarning"), "declares printWarning()");
        check(declares(c, "printNote"), "declares printNote()");
    }

    static void testBlackBox() throws Exception {
        final List<Diagnostic.Kind> kinds = new ArrayList<>();
        Messager m = new Messager() {
            public void printMessage(Diagnostic.Kind kind, CharSequence msg) { kinds.add(kind); }
            public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) { kinds.add(kind); }
            public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a) { kinds.add(kind); }
            public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a, AnnotationValue v) { kinds.add(kind); }
        };
        m.printError("err");
        m.printWarning("warn");
        m.printNote("note");
        check(kinds.equals(List.of(Diagnostic.Kind.ERROR, Diagnostic.Kind.WARNING, Diagnostic.Kind.NOTE)), "default printError/Warning/Note delegate kinds");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
