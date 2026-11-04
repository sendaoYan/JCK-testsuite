/*
 * @test
 * @summary com.sun.source.tree.InstanceOfTree conformance: white-box structure + black-box behavior.
 * @run main InstanceOfTreeTest
 */

import java.lang.reflect.*;

public class InstanceOfTreeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.source.tree.InstanceOfTree");
        check(c != null, "com.sun.source.tree.InstanceOfTree loads");
        check(c.getName().equals("com.sun.source.tree.InstanceOfTree"), "binary name is com.sun.source.tree.InstanceOfTree");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.tree.InstanceOfTree is public");
        check(c.isInterface(), "com.sun.source.tree.InstanceOfTree is an interface");
        check(ifaceNames(c).equals(java.util.Set.of("com.sun.source.tree.ExpressionTree")), "direct superinterfaces");
    }

    // ---- shared tree helpers ----
    static final String KITCHEN =
            "package p;\n" +
            "import java.util.*;\n" +
            "@interface TA {}\n" +
            "public class C<T extends Number & Comparable<T>> {\n" +
            "    int f = 0;\n" +
            "    int[] arr = new int[3];\n" +
            "    String s = \"x\";\n" +
            "    boolean b = true;\n" +
            "    double d = 1.5;\n" +
            "    char ch = 'c';\n" +
            "    long l = 1L;\n" +
            "    Object o = null;\n" +
            "    String @TA [] ta = null;\n" +
            "    int q = arr[0];\n" +
            "    <U> U id(U u) { return u; }\n" +
            "    void m(int x) {\n" +
            "        int y = x + 1;\n" +
            "        y += 2; y++;\n" +
            "        int z = (x > 0) ? 1 : 2;\n" +
            "        int[] aa = new int[]{1,2,3};\n" +
            "        String ss = \"a\" + \"b\";\n" +
            "        if (x == 0) { y = 1; } else { y = 2; }\n" +
            "        while (x < 10) { x++; if (x==5) break; if (x==6) continue; }\n" +
            "        do { x--; } while (x > 0);\n" +
            "        for (int i=0;i<3;i++) { y += i; }\n" +
            "        for (int v : arr) { y += v; }\n" +
            "        outer: for (;;) { break outer; }\n" +
            "        switch (y) { case 0: y=1; break; case 1: y=2; break; default: y=3; }\n" +
            "        int se = switch (y) { case 0 -> 1; case 1 -> 2; default -> { yield 3; } };\n" +
            "        try { throw new RuntimeException(); } catch (RuntimeException | Error e) { } finally { y = 0; }\n" +
            "        synchronized (this) { y++; }\n" +
            "        assert y >= 0;\n" +
            "        Runnable r = () -> { y++; };\n" +
            "        Runnable r2 = this::m2;\n" +
            "        Runnable r3 = (Runnable & java.io.Serializable) () -> { y++; };\n" +
            "        List<String> list = new ArrayList<String>();\n" +
            "        List<?> wc = null;\n" +
            "        Class<? extends Number> wce = null;\n" +
            "        Object o2 = (String) s;\n" +
            "        if (o2 instanceof String s2) { }\n" +
            "        Number n = (Number) x;\n" +
            "        y = Math.max(y, 1);\n" +
            "    }\n" +
            "    void m2() {}\n" +
            "    record Point(int x, int y) {}\n" +
            "    static void pat(Object o) {\n" +
            "        if (o instanceof Point(int px, int py)) { }\n" +
            "        switch (o) { case Point(int px, int py) -> {}; default -> {}; }\n" +
            "    }\n" +
            "}\n";
    static final String MODULE_SRC = "module m { requires java.base; exports p; opens p to q; uses java.util.List; provides java.util.List with java.util.ArrayList; }";
    static final String ERR_SRC = "package p; class E { void m(){ int bad = ; } }";
    static javax.tools.JavaCompiler compiler() { return javax.tools.ToolProvider.getSystemJavaCompiler(); }
    static class MemSrc extends javax.tools.SimpleJavaFileObject {
        final String code;
        MemSrc(String name, String code) { super(java.net.URI.create("string:///" + name + javax.tools.JavaFileObject.Kind.SOURCE.extension), javax.tools.JavaFileObject.Kind.SOURCE); this.code = code; }
        public CharSequence getCharContent(boolean ignore) { return code; }
    }
    static com.sun.source.tree.CompilationUnitTree parseCU(String name, String code) throws Exception {
        javax.tools.DiagnosticCollector<javax.tools.JavaFileObject> diags = new javax.tools.DiagnosticCollector<>();
        javax.tools.StandardJavaFileManager fm = compiler().getStandardFileManager(diags, java.util.Locale.ROOT, null);
        com.sun.source.util.JavacTask t = (com.sun.source.util.JavacTask) compiler().getTask(null, fm, diags, java.util.List.of("-proc:none"), null, java.util.List.of(new MemSrc(name, code)));
        return t.parse().iterator().next();
    }
    static void testBlackBox() throws Exception {
        com.sun.source.tree.CompilationUnitTree cu = parseCU("S.java", KITCHEN);
        final java.util.List<com.sun.source.tree.InstanceOfTree> found = new java.util.ArrayList<>();
        new com.sun.source.util.TreeScanner<Void,Void>() {
            @Override public Void visitInstanceOf(com.sun.source.tree.InstanceOfTree node, Void v) {
                found.add(node);
                check(node.getKind() != null, "getKind non-null");
                check(node.getKind().asInterface() == com.sun.source.tree.InstanceOfTree.class, "getKind().asInterface() == InstanceOfTree");
                return super.visitInstanceOf(node, v);
            }
        }.scan(cu, null);
        check(!found.isEmpty(), "com.sun.source.tree.InstanceOfTree present in parsed AST");
        com.sun.source.tree.InstanceOfTree n = found.get(0);
        check(n.getExpression() != null, "getExpression");
        check(n.getType() != null, "getType");
        final boolean[] accepted = new boolean[1];
        n.accept(new com.sun.source.util.SimpleTreeVisitor<Void,Void>() {
            @Override public Void visitInstanceOf(com.sun.source.tree.InstanceOfTree node, Void v) { accepted[0] = true; return null; }
        }, null);
        check(accepted[0], "accept dispatches to visitor");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
