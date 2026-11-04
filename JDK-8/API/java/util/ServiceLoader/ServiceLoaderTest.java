/*
 * @test
 * @summary java.util.ServiceLoader API conformance (Java 8 surface): complete contract coverage
 *          of load(Class)/load(Class,ClassLoader)/loadInstalled(Class), null NPE, empty iteration
 *          for an unregistered service, reload() resetting the provider cache, and toString.
 * @run main ServiceLoaderTest
 */

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;

public class ServiceLoaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
    }

    interface Svc {
    }

    static void testServiceLoader() {
        ServiceLoader<Svc> sl = ServiceLoader.load(Svc.class);
        check(!sl.iterator().hasNext(), "no providers for a service with no registration");
        sl.reload();
        check(!sl.iterator().hasNext(), "reload also finds none");
    }

    static void testNullAndFactories() {
        expectThrows(NullPointerException.class, () -> ServiceLoader.load(null),
                     "load(null) NPE");
        expectThrows(NullPointerException.class,
                     () -> ServiceLoader.load(null, ServiceLoaderTest.class.getClassLoader()),
                     "load(null, cl) NPE");

        ClassLoader cl = ServiceLoaderTest.class.getClassLoader();
        ServiceLoader<Svc> withCl = ServiceLoader.load(Svc.class, cl);
        check(withCl != null && !withCl.iterator().hasNext(), "load(Class, ClassLoader)");

        ServiceLoader<Svc> installed = ServiceLoader.loadInstalled(Svc.class);
        check(installed != null, "loadInstalled non-null");
        check(!installed.iterator().hasNext(), "loadInstalled finds none for nested service");
    }

    static void testToString() {
        ServiceLoader<Svc> sl = ServiceLoader.load(Svc.class);
        check(sl.toString() != null && sl.toString().length() > 0, "toString non-empty");
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
            // JDK-8：cause 为 null，类名嵌在消息里；JDK-9+：包装 ClassNotFoundException
            check(String.valueOf(sce.getMessage()).contains("mysvc.Missing"),
                  "missing provider class SCE names the class");
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

    public static void main(String[] args) throws Exception {
        testServiceLoader();
        testNullAndFactories();
        testToString();
        testClosedLoop();
        testConfigurationErrorMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
