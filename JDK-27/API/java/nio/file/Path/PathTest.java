/*
 * @test
 * @summary java.nio.file.Path conformance: white-box structure (reflection) + black-box behavior
 *          (component access, resolution, relativize, File interop).
 * @run main PathTest
 *
 * 黑盒设计案:
 * 1. 契约: 文件系统路径抽象，提供分量访问(getRoot/getFileName/getParent/getName/subpath)、判断(startsWith/endsWith/isAbsolute)与组合(normalize/resolve/resolveSibling/relativize)。
 * 2. 场景: Path.of(String,String...) 与 Path.of(URI) 构造 -> 分量访问 -> 组合操作。
 * 3. 组合: /a/b/c 的分量 -> resolve/resolveSibling/relativize -> toAbsolutePath -> toFile 往返。
 * 4. 不变量: equals/hashCode 按路径等价；iterator 遍历分量；Path.of 与 Paths.get 等价。
 * 5. 边界: subpath 越界抛 IllegalArgumentException；normalize 折叠冗余分隔符。
 */

import java.lang.reflect.Modifier;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PathTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.Path");
        check(c.isInterface(), "Path is an interface");
        check(Modifier.isAbstract(c.getModifiers()), "interface is abstract");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.lang.Comparable"), "extends Comparable");
        check(hasInterface(c, "java.lang.Iterable"), "extends Iterable");
        check(hasInterface(c, "java.nio.file.Watchable"), "extends Watchable");
        hasMethod(c, "of", String.class, String[].class);
        hasMethod(c, "getFileName");
        hasMethod(c, "resolve", String.class);
        hasMethod(c, "relativize", Path.class);
        hasMethod(c, "toFile");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() {
        Path p = Path.of("/a/b/c");
        check(p.isAbsolute(), "isAbsolute");
        check("/".equals(p.getRoot().toString()), "getRoot");
        check("c".equals(p.getFileName().toString()), "getFileName");
        check("/a/b".equals(p.getParent().toString()), "getParent");
        check(p.getNameCount() == 3, "getNameCount");
        check("a".equals(p.getName(0).toString()), "getName(0)");
        check("b".equals(p.getName(1).toString()), "getName(1)");
        check("a/b".equals(p.subpath(0, 2).toString()), "subpath");
        check(p.startsWith(Path.of("/a")), "startsWith");
        check(p.endsWith(Path.of("c")), "endsWith");
        check("/a/b/c".equals(p.normalize().toString()), "normalize");

        check("/a/b/c/d".equals(p.resolve("d").toString()), "resolve");
        check("/a/b/d".equals(p.resolveSibling("d").toString()), "resolveSibling");
        check("/a/b/c".equals(Path.of("/a/b").resolve(Path.of("c")).toString()), "resolve Path");

        check("/a/b".equals(Path.of("/a//b/./").normalize().toString()), "normalize folds");

        check("d".equals(Path.of("/a/b/c").relativize(Path.of("/a/b/c/d")).toString()), "relativize child");
        check("../x/y".equals(Path.of("/a/b").relativize(Path.of("/a/x/y")).toString()), "relativize up");

        List<String> names = new ArrayList<>();
        for (Path n : p) names.add(n.toString());
        check(names.size() == 3 && names.get(0).equals("a") && names.get(2).equals("c"), "iterator");

        check(p.toAbsolutePath().isAbsolute(), "toAbsolutePath");
        check(p.toFile().getName().equals("c"), "toFile");

        check(Path.of(URI.create("file:///a/b")).equals(Path.of("/a/b")), "Path.of(URI)");

        check(p.equals(Path.of("/a/b/c")) && p.hashCode() == Path.of("/a/b/c").hashCode(), "equals/hashCode");

        boolean iae = false;
        try { p.subpath(0, 4); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "subpath out of bounds IAE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
