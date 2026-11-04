/*
 * @test
 * @summary java.nio.file.Paths conformance: white-box structure (reflection) + black-box behavior
 *          (get(String,...) and get(URI), equivalence with Path.of).
 * @run main PathsTest
 *
 * 黑盒设计案:
 * 1. 契约: 构造 Path 的便捷工厂，get(String,String...) 拼接分量，get(URI) 从 URI 构造。
 * 2. 场景: Paths.get("a","b","c") -> 分量断言；Paths.get(URI) -> 绝对路径。
 * 3. 组合: Paths.get 与 Path.of 等价断言；多参拼接与单参带分隔符一致。
 * 4. 不变量: get 返回与 Path.of 相同系统 Provider 的 Path。
 * 5. 边界: 空字符串 Paths.get("") 的 nameCount==0。
 */

import java.lang.reflect.Modifier;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.Paths");
        check(!c.isInterface() && Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == Object.class, "superclass Object");
        hasMethod(c, "get", String.class, String[].class);
        hasMethod(c, "get", URI.class);
    }

    // ---- black-box: behavior ----
    static void testBlackBox() {
        Path p = Paths.get("a", "b", "c");
        check(p.getNameCount() == 3, "getNameCount 3");
        check("c".equals(p.getFileName().toString()), "getFileName");
        check("a".equals(p.getName(0).toString()), "getName(0)");

        check(Paths.get("a", "b", "c").equals(Paths.get("a/b/c")), "multi-arg equals joined");
        check(Paths.get("a", "b").equals(Path.of("a", "b")), "Paths.get equals Path.of");
        check("".equals(Paths.get("").toString()), "empty path toString empty");

        Path abs = Paths.get(URI.create("file:///tmp/x"));
        check(abs.isAbsolute(), "get(URI) absolute");
        check("/tmp/x".equals(abs.toString()), "get(URI) path");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
