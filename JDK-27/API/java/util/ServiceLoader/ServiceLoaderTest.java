/*
 * @test
 * @summary java.util.ServiceLoader conformance: white-box structure plus black-box behavior of
 *          empty iteration, findFirst, stream, and reload for a service with no registered
 *          providers (deterministic).
 * @run main ServiceLoaderTest
 */

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;
import java.util.ServiceLoader;

public class ServiceLoaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Svc {
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.ServiceLoader");
        check(!c.isInterface(), "ServiceLoader is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "ServiceLoader is final");
        check(Iterable.class.isAssignableFrom(c), "ServiceLoader implements Iterable");
        c.getMethod("iterator");
        c.getMethod("stream");
        c.getMethod("findFirst");
        c.getMethod("reload");
        c.getMethod("load", Class.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ServiceLoader<Svc> sl = ServiceLoader.load(Svc.class);
        check(!sl.iterator().hasNext(), "no providers for an unregistered service");
        Optional<Svc> first = sl.findFirst();
        check(!first.isPresent(), "findFirst empty when no providers");
        check(sl.stream().count() == 0, "stream empty when no providers");
        sl.reload();
        check(!sl.iterator().hasNext(), "reload still finds none");
    }


    // ===================== 契约深化：META-INF/services 闭环/配置顺序/新实例语义/SCE 矩阵 =====================
    static java.nio.file.Path slDir;
    static URLClassLoader slCl;
    static Class<?> svcClass;

    static void slCompile(String... sources) throws Exception {
        javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        java.util.List<String> opts = new java.util.ArrayList<String>();
        opts.add("-d");
        opts.add(slDir.toString());
        for (String src : sources) opts.add(src);
        int rc = jc.run(null, null, null, opts.toArray(new String[0]));
        if (rc != 0) throw new AssertionError("scratch compile failed rc=" + rc);
    }

    static void slWrite(String rel, String content) throws Exception {
        java.nio.file.Path p = slDir.resolve(rel);
        java.nio.file.Files.createDirectories(p.getParent());
        java.nio.file.Files.write(p, content.getBytes("UTF-8"));
    }

    static void setupSlFixtures() throws Exception {
        slDir = java.nio.file.Files.createTempDirectory("slclosed");
        java.nio.file.Path svc = slDir.resolve("mysvc/Svc.java");
        java.nio.file.Path a = slDir.resolve("mysvc/A.java");
        java.nio.file.Path b = slDir.resolve("mysvc/B.java");
        java.nio.file.Path p = slDir.resolve("mysvc/P.java");
        slWrite("mysvc/Svc.java",
              "package mysvc;\npublic interface Svc { String name(); }\n");
        slWrite("mysvc/A.java",
              "package mysvc;\npublic class A implements Svc { public String name() { return \"A\"; } }\n");
        slWrite("mysvc/B.java",
              "package mysvc;\npublic class B implements Svc { public String name() { return \"B\"; } }\n");
        slWrite("mysvc/P.java",
              "package mysvc;\npublic class P implements Svc { private P() {} public String name() { return \"P\"; } }\n");
        slCompile(svc.toString(), a.toString(), b.toString(), p.toString());
        slWrite("META-INF/services/mysvc.Svc", "mysvc.B\nmysvc.A\n");
        slCl = new URLClassLoader(new URL[]{ slDir.toUri().toURL() });
        svcClass = Class.forName("mysvc.Svc", true, slCl);
    }

    static void testClosedLoop() throws Exception {
        setupSlFixtures();
        java.util.List<Object> first = new java.util.ArrayList<Object>();
        java.util.List<Object> second = new java.util.ArrayList<Object>();
        java.util.List<String> names = new java.util.ArrayList<String>();
        iterateSl(first, names);
        check(names.size() == 2 && "B".equals(names.get(0)) && "A".equals(names.get(1)),
              "providers iterate in configuration-file order (B before A)");
        check(first.get(0).getClass().getSimpleName().equals("B")
              && first.get(1).getClass().getSimpleName().equals("A"),
              "instances have declared provider types");
        // reload 重新实例化：对象身份不同
        iterateSl(second, names);
        check(second.size() == 2 && second.get(0) != first.get(0) && second.get(1) != first.get(1),
              "each iteration creates fresh instances");
        check(second.get(0).getClass() == first.get(0).getClass(), "reload yields same classes");
        // toString
        Object sl = newSvcLoader();
        check(String.valueOf(sl).contains("mysvc.Svc"), "toString names the service interface");
        // iterator().remove 不支持
        java.util.Iterator<?> it = newSvcLoader().iterator();
        it.next();
        try {
            it.remove();
            check(false, "iterator().remove UnsupportedOperationException");
        } catch (UnsupportedOperationException uoe) {
            check(true, "iterator().remove UnsupportedOperationException");
        }
        slCleanup(new java.nio.file.Path[]{ slDir });
    }

    static void iterateSl(java.util.List<Object> into, java.util.List<String> names) throws Exception {
        Object sl = newSvcLoader();
        java.util.Iterator<?> it = ((Iterable<?>) sl).iterator();
        while (it.hasNext()) {
            Object o = it.next();
            into.add(o);
            java.lang.reflect.Method m = o.getClass().getMethod("name");
            if (names != null) names.add((String) m.invoke(o));
        }
    }

    static Iterable<?> newSvcLoader() throws Exception {
        return (Iterable<?>) Class.forName("java.util.ServiceLoader")
              .getMethod("load", Class.class, ClassLoader.class)
              .invoke(null, svcClass, slCl);
    }

    static void testConfigurationErrorMatrix() throws Exception {
        // 配置文件指向不存在的类 -> ServiceConfigurationError(cause ClassNotFoundException)
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("slmissing");
        slDir = dir;
        java.nio.file.Path svcM = dir.resolve("mysvc/Svc.java");
        slWrite("mysvc/Svc.java", "package mysvc;\npublic interface Svc { }\n");
        slCompile(svcM.toString());
        slWrite("META-INF/services/mysvc.Svc", "mysvc.Missing\n");
        slCl = new URLClassLoader(new URL[]{ dir.toUri().toURL() });
        svcClass = Class.forName("mysvc.Svc", false, slCl);
        java.util.Iterator<?> it = ((Iterable<?>) newSvcLoader()).iterator();
        try {
            it.next();
            check(false, "missing provider class SCE");
        } catch (java.util.ServiceConfigurationError sce) {
            check(sce.getCause() instanceof ClassNotFoundException,
                  "missing provider class SCE wraps ClassNotFoundException (JDK-9+)");
        }
        slCl.close();
        // 私有构造器 -> SCE（两版 JDK cause 不同：8=IllegalAccessException，9+=NoSuchMethodException）
        java.nio.file.Path dir2 = java.nio.file.Files.createTempDirectory("slprivate");
        slDir = dir2;
        java.nio.file.Path svc2 = dir2.resolve("mysvc2/Svc.java");
        java.nio.file.Path p2 = dir2.resolve("mysvc2/P.java");
        slWrite("mysvc2/Svc.java", "package mysvc2;\npublic interface Svc { }\n");
        slWrite("mysvc2/P.java",
              "package mysvc2;\npublic class P implements Svc { private P() {} }\n");
        slCompile(svc2.toString(), p2.toString());
        slWrite("META-INF/services/mysvc2.Svc", "mysvc2.P\n");
        slCl = new URLClassLoader(new URL[]{ dir2.toUri().toURL() });
        svcClass = Class.forName("mysvc2.Svc", false, slCl);
        java.util.Iterator<?> it2 = ((Iterable<?>) newSvcLoader()).iterator();
        try {
            it2.next();
            check(false, "non-public no-arg ctor SCE");
        } catch (java.util.ServiceConfigurationError sce) {
            check(true, "non-public no-arg ctor SCE");
        }
        slCl.close();
        slCleanup(new java.nio.file.Path[]{ dir, dir2 });
    }

    static void slCleanup(java.nio.file.Path[] dirs) throws Exception {
        for (java.nio.file.Path dir : dirs) {
            java.nio.file.Files.walk(dir).sorted(java.util.Collections.reverseOrder())
                  .forEach(pp -> { try { java.nio.file.Files.deleteIfExists(pp); } catch (Exception e) { } });
            java.nio.file.Files.deleteIfExists(dir);
        }
    }


    // ===================== JDK-9+ 契约：stream/findFirst/Provider 视图 =====================
    static void testStreamAndProviderApis() throws Exception {
        setupSlFixtures();
        @SuppressWarnings("rawtypes")
        java.util.ServiceLoader sl = java.util.ServiceLoader.load(
              (Class) svcClass, slCl);
        check(sl.stream().count() == 2, "stream() exposes each provider once");
        java.util.Optional<? extends java.util.ServiceLoader.Provider<?>> first =
              sl.stream().findFirst();
        check(first.isPresent() && first.get().type().getSimpleName().equals("B"),
              "findFirst returns first configured provider");
        check(first.get().get().getClass().getSimpleName().equals("B"),
              "Provider.get() instantiates the provider");
        java.util.List<String> types = new java.util.ArrayList<String>();
        for (Object pr : (Iterable<Object>) () -> sl.stream().map(x -> x).iterator()) {
            types.add(((java.util.ServiceLoader.Provider<?>) pr).type().getSimpleName());
        }
        check(types.size() == 2 && types.contains("A") && types.contains("B"),
              "stream().map(Provider::type) lists provider classes");
        check(sl.findFirst().isPresent(), "findFirst() convenience method");
        sl.forEach(p -> check(p != null, "forEach visits provider"));
        slCleanup(new java.nio.file.Path[]{ slDir });
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testClosedLoop();
        testConfigurationErrorMatrix();
        testStreamAndProviderApis();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
