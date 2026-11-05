/*
 * @test
 * @summary java.lang.Compiler conformance: static native method surface (JDK-8 only).
 * @run main CompilerTest
 */

public class CompilerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCompiler() {
        Compiler.enable();
        Compiler.disable();
        check(true, "enable/disable callable");
        check(Compiler.compileClass(String.class) == false, "compileClass returns false (no JIT)");
        check(Compiler.compileClasses("java.lang.String") == false,
              "compileClasses returns false (no JIT)");
        check(Compiler.command("x") == null, "command returns null (no JIT)");
    }

    // ================= 契约深化（2026-11）追加 =================
    // The no-op JIT contract: with no compiler installed, every method is a
    // safe no-op. compileClass/compileClasses answer false ("compilation
    // failed") and command answers null, regardless of arguments and of
    // enable()/disable() state. All return values locked empirically on
    // temurin 8u372 (the only JDK of this suite that still ships Compiler).
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Compiler");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Compiler is final");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(c.getConstructors().length == 0, "no public constructors");
        check(c.getAnnotation(Deprecated.class) == null, "not @Deprecated on JDK-8");
        java.lang.reflect.Method[] ms = c.getMethods();
        java.util.Set<String> own = new java.util.HashSet<String>();
        int ownCount = 0;
        for (java.lang.reflect.Method m : ms) {
            if (m.getDeclaringClass() == c) { ownCount++; own.add(m.getName()); }
        }
        check(ownCount == 5, "exactly 5 own public methods");
        check(own.contains("compileClass") && own.contains("compileClasses")
              && own.contains("command") && own.contains("enable") && own.contains("disable"),
              "method names are the expected five");
        boolean allStaticNative = true;
        for (java.lang.reflect.Method m : ms) {
            if (m.getDeclaringClass() == c) {
                allStaticNative &= java.lang.reflect.Modifier.isStatic(m.getModifiers())
                        && java.lang.reflect.Modifier.isNative(m.getModifiers());
            }
        }
        check(allStaticNative, "all own public methods are static native");
        check(c.getMethod("compileClass", Class.class).getReturnType() == boolean.class,
              "compileClass returns boolean");
        check(c.getMethod("compileClasses", String.class).getReturnType() == boolean.class,
              "compileClasses returns boolean");
        check(c.getMethod("command", Object.class).getReturnType() == Object.class,
              "command returns Object");
    }

    static void testBehavior() {
        check(Compiler.compileClass(CompilerTest.class) == false, "compileClass(own class) false");
        check(Compiler.compileClass(int.class) == false, "compileClass(primitive class) false");
        check(Compiler.compileClass(null) == false, "compileClass(null) false, no throw");
        check(Compiler.compileClasses("CompilerTest") == false, "compileClasses(own name) false");
        check(Compiler.compileClasses("no.such.Package") == false, "compileClasses(unknown) false");
        check(Compiler.compileClasses(null) == false, "compileClasses(null) false, no throw");
        check(Compiler.compileClasses("") == false, "compileClasses(empty) false");
        check(Compiler.command("anything") == null, "command(String) null");
        check(Compiler.command(new Object()) == null, "command(Object) null");
        check(Compiler.command(null) == null, "command(null) null, no throw");
        // enable()/disable() toggling never changes the no-op outcomes.
        Compiler.enable();
        check(Compiler.compileClass(String.class) == false, "after enable: still false");
        check(Compiler.command("x") == null, "after enable: command still null");
        Compiler.disable();
        check(Compiler.compileClasses("java.lang.Object") == false, "after disable: still false");
        Compiler.enable();
        Compiler.disable();
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testCompiler();
        testWhiteBox();
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
