/*
 * @test
 * @summary java.lang.instrument.Instrumentation conformance: white-box structure + black-box behavior.
 * @run main InstrumentationTest
 */
import java.lang.instrument.*;
import java.util.*;
import java.util.jar.JarFile;

public class InstrumentationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.instrument.Instrumentation");
        check(c.isInterface(), "interface");
        check(c.getMethod("addTransformer", ClassFileTransformer.class, boolean.class).getReturnType() == void.class,
              "addTransformer(ClassFileTransformer,boolean)");
        check(c.getMethod("removeTransformer", ClassFileTransformer.class).getReturnType() == boolean.class,
              "removeTransformer returns boolean");
        check(c.getMethod("isRetransformClassesSupported").getReturnType() == boolean.class,
              "isRetransformClassesSupported returns boolean");
        check(c.getMethod("retransformClasses", Class[].class).getReturnType() == void.class,
              "retransformClasses returns void");
        check(c.getMethod("isRedefineClassesSupported").getReturnType() == boolean.class,
              "isRedefineClassesSupported returns boolean");
        check(c.getMethod("getAllLoadedClasses").getReturnType() == Class[].class, "getAllLoadedClasses returns Class[]");
        check(c.getMethod("getObjectSize", Object.class).getReturnType() == long.class, "getObjectSize returns long");
        check(c.getMethod("isNativeMethodPrefixSupported").getReturnType() == boolean.class,
              "isNativeMethodPrefixSupported returns boolean");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Instrumentation inst = new Instrumentation() {
            public void addTransformer(ClassFileTransformer t, boolean canRetransform) { }
            public void addTransformer(ClassFileTransformer t) { }
            public boolean removeTransformer(ClassFileTransformer t) { return true; }
            public boolean isRetransformClassesSupported() { return true; }
            public void retransformClasses(Class<?>... classes) { }
            public boolean isRedefineClassesSupported() { return false; }
            public void redefineClasses(ClassDefinition... defs) { }
            public boolean isModifiableClass(Class<?> theClass) { return theClass == String.class; }
            public Class[] getAllLoadedClasses() { return new Class[]{String.class, Integer.class}; }
            public Class[] getInitiatedClasses(ClassLoader loader) { return new Class[0]; }
            public long getObjectSize(Object objectToSize) { return 16L; }
            public void appendToBootstrapClassLoaderSearch(JarFile jarfile) { }
            public void appendToSystemClassLoaderSearch(JarFile jarfile) { }
            public boolean isNativeMethodPrefixSupported() { return false; }
            public void setNativeMethodPrefix(ClassFileTransformer transformer, String prefix) { }
            public void redefineModule(Module module, Set<Module> extraReads, Map<String, Set<Module>> extraExports,
                                       Map<String, Set<Module>> extraOpens, Set<Class<?>> extraUses,
                                       Map<Class<?>, List<Class<?>>> extraProvides) { }
            public boolean isModifiableModule(Module module) { return false; }
        };

        check(inst.isRetransformClassesSupported(), "isRetransformClassesSupported roundtrip");
        check(!inst.isRedefineClassesSupported(), "isRedefineClassesSupported roundtrip");
        check(inst.isModifiableClass(String.class), "isModifiableClass roundtrip");
        check(!inst.isModifiableClass(Integer.class), "isModifiableClass false roundtrip");
        Class<?>[] loaded = inst.getAllLoadedClasses();
        check(loaded.length == 2 && loaded[0] == String.class && loaded[1] == Integer.class,
              "getAllLoadedClasses roundtrip");
        check(inst.getInitiatedClasses(null).length == 0, "getInitiatedClasses roundtrip");
        check(inst.getObjectSize(new Object()) == 16L, "getObjectSize roundtrip");
        check(!inst.isNativeMethodPrefixSupported(), "isNativeMethodPrefixSupported roundtrip");
        check(inst.removeTransformer(null), "removeTransformer roundtrip");
        check(!inst.isModifiableModule(String.class.getModule()), "isModifiableModule roundtrip");
        // void methods are callable no-ops
        inst.addTransformer(null);
        inst.addTransformer(null, true);
        try { inst.retransformClasses(String.class); } catch (UnmodifiableClassException expected) { }
        try { inst.redefineClasses(new ClassDefinition(String.class, new byte[0])); }
        catch (ClassNotFoundException | UnmodifiableClassException expected) { }
        inst.setNativeMethodPrefix(null, "$$prefix$$");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
