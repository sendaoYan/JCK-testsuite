/*
 * @test
 * @summary javax.annotation.processing.Processor conformance: white-box structure + black-box behavior.
 * @run main ProcessorTest
 */

import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.tools.*;

public class ProcessorTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.annotation.processing.Processor");
        check(c != null, "javax.annotation.processing.Processor loads");
        check(c.getName().equals("javax.annotation.processing.Processor"), "binary name is javax.annotation.processing.Processor");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.annotation.processing.Processor is public");
        check(c.isInterface() && !c.isAnnotation(), "is a plain interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getSupportedOptions"), "declares getSupportedOptions()");
        check(declares(c, "getSupportedAnnotationTypes"), "declares getSupportedAnnotationTypes()");
        check(declares(c, "getSupportedSourceVersion"), "declares getSupportedSourceVersion()");
        check(declares(c, "init"), "declares init()");
        check(declares(c, "process"), "declares process()");
        check(declares(c, "getCompletions"), "declares getCompletions()");
    }

    static void testBlackBox() throws Exception {
        final boolean[] processed = new boolean[1];
        Processor p = new Processor() {
            public Set<String> getSupportedOptions() { return Set.of("o"); }
            public Set<String> getSupportedAnnotationTypes() { return Set.of("a"); }
            public SourceVersion getSupportedSourceVersion() { return SourceVersion.RELEASE_8; }
            public void init(ProcessingEnvironment env) {}
            public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) { processed[0] = true; return true; }
            public Iterable<? extends Completion> getCompletions(Element e, AnnotationMirror a, ExecutableElement m, String s) { return List.of(); }
        };
        check(p.getSupportedOptions().equals(Set.of("o")), "getSupportedOptions");
        check(p.getSupportedAnnotationTypes().equals(Set.of("a")), "getSupportedAnnotationTypes");
        check(p.getSupportedSourceVersion() == SourceVersion.RELEASE_8, "getSupportedSourceVersion");
        check(p.process(Set.of(), null), "process returns true");
        check(processed[0], "process invoked");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
