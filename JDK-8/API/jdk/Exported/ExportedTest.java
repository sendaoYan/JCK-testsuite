/*
 * @test
 * @summary jdk.Exported conformance: reflection shape checks.
 * @run main ExportedTest
 */

import java.lang.reflect.*;
import java.lang.annotation.*;
import java.util.*;

public class ExportedTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.Exported");
        check(c != null, "jdk.Exported loads");
        check(c.getName().equals("jdk.Exported"), "binary name is jdk.Exported");
        check(Modifier.isPublic(c.getModifiers()), "jdk.Exported is public");
        check(c.isInterface(), "jdk.Exported is an interface");
        check(c.isAnnotation(), "jdk.Exported is an annotation");
        check(ifaceNames(c).equals(set("java.lang.annotation.Annotation")), "direct superinterfaces");
        check(declares(c, "value"), "declares value");
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===== 深化 2026-11-05（子代理B：元注解契约+value默认值+应用回读，实测基线见 DESIGN.md）=====


    @jdk.Exported
    static class ExportedHolder {}
    @jdk.Exported(false)
    static class NonExportedHolder {}
    static class PlainHolder {}


    static void testDeep() throws Exception {
        Class<?> c = Class.forName("jdk.Exported");
        Retention r = c.getAnnotation(Retention.class);
        check(r != null && r.value() == RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        check(c.isAnnotationPresent(Documented.class), "@Documented present");
        Target t = c.getAnnotation(Target.class);
        check(t != null, "@Target present");
        Set<String> ts = new HashSet<String>();
        for (ElementType e : t.value()) ts.add(e.name());
        check(ts.size() == 2 && ts.contains("TYPE") && ts.contains("PACKAGE"), "@Target exactly {TYPE, PACKAGE}: " + ts);
        Method vm = c.getDeclaredMethod("value");
        check(vm.getReturnType() == boolean.class, "value() returns boolean");
        check(Boolean.TRUE.equals(vm.getDefaultValue()), "value() default true");
        check(c.getDeclaredMethods().length == 1, "single element annotation");
        // 应用闭环：标注自有类型后回读（@Exported 无运行时语义，仅元数据声明）
        check(ExportedHolder.class.isAnnotationPresent(c.asSubclass(Annotation.class)), "default applied -> present");
        jdk.Exported a = ExportedHolder.class.getAnnotation(jdk.Exported.class);
        check(a != null && a.value() == true, "default value() true round-trip");
        jdk.Exported b = NonExportedHolder.class.getAnnotation(jdk.Exported.class);
        check(b != null && b.value() == false, "explicit false round-trip");
        check(!PlainHolder.class.isAnnotationPresent(c.asSubclass(Annotation.class)), "unannotated class negative");
        // JDK 自带类/包在 8 侧运行时不携带可反射的 @Exported（实测 java.lang.String / java.lang 包均 false）
        check(!String.class.isAnnotationPresent(c.asSubclass(Annotation.class)), "java.lang.String not reflectively @Exported on this build");
        Package jl = Package.getPackage("java.lang");
        check(jl != null, "java.lang package resolvable");
        check(!jl.isAnnotationPresent(c.asSubclass(Annotation.class)), "java.lang package not reflectively @Exported on this build");
        // 27 侧对应事实：jdk.Exported 不存在（Class.forName -> ClassNotFoundException），由 JDK-27 侧不建目录体现
        check(c.getPackage() == null || "jdk".equals(c.getPackage().getName()), "declared in package jdk");
    }

}
