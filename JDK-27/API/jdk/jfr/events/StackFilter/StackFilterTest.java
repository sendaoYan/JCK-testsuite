/*
 * @test
 * @summary jdk.jfr.events.StackFilter conformance: type shape.
 * @run main StackFilterTest
 */

import java.lang.reflect.Modifier;

public class StackFilterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.events.StackFilter");
        check(c != null, "loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) == true, "public");
        check(Modifier.isProtected(m) == false, "protected");
        check(c.isInterface() == true, "isInterface");
        check(c.isAnnotation() == true, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(Modifier.isAbstract(m) == true, "abstract");
        check(Modifier.isFinal(m) == false, "final");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        java.util.Set<String> got = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) got.add(i.getName());
        check(got.contains("java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");

        // ===================== 深化: 注解契约 + 使用点闭环 =====================
        // 元注解面 (源码 jdk.jfr.events/StackFilter.java, since JDK 22)
        java.lang.annotation.Target tgt = c.getAnnotation(java.lang.annotation.Target.class);
        check(tgt != null && java.util.Arrays.asList(tgt.value()).equals(java.util.Arrays.asList(java.lang.annotation.ElementType.TYPE)), "@Target == {TYPE}");
        check(c.isAnnotationPresent(java.lang.annotation.Inherited.class), "@Inherited 存在");
        java.lang.annotation.Retention ret = c.getAnnotation(java.lang.annotation.Retention.class);
        check(ret != null && ret.value() == java.lang.annotation.RetentionPolicy.RUNTIME, "@Retention == RUNTIME");
        check(c.isAnnotationPresent(jdk.jfr.MetadataDefinition.class), "@MetadataDefinition 存在");
        // 成员面: 唯一成员 String[] value(), 无默认值
        java.lang.reflect.Method[] mems = c.getDeclaredMethods();
        check(mems.length == 1 && "value".equals(mems[0].getName()), "唯一成员 value()");
        check(mems[0].getReturnType() == String[].class, "value() 返回 String[]");
        check(mems[0].getDefaultValue() == null, "value() 无默认值");
        // 使用点闭环: 内置事件类携带 @StackFilter (类注解)
        Class<?> sfFR = Class.forName("jdk.jfr.events.FileReadEvent", false, StackFilterTest.class.getClassLoader());
        @SuppressWarnings("unchecked")
        java.lang.annotation.Annotation sfOnFR = sfFR.getAnnotation((Class<? extends java.lang.annotation.Annotation>) c);
        check(sfOnFR != null, "FileReadEvent 携带 @jdk.jfr.events.StackFilter");
        Class<?> sfSO = Class.forName("jdk.jfr.events.SocketReadEvent", false, StackFilterTest.class.getClassLoader());
        check(sfSO.getAnnotation((Class<? extends java.lang.annotation.Annotation>) c) != null, "SocketReadEvent 携带 @StackFilter");
        // 公共 API 面: jdk.jfr.StackFilter 并不存在 (内部注解未导出); 栈迹相关公共注解是 jdk.jfr.StackTrace
        boolean sfApiMissing = false;
        try {
            Class.forName("jdk.jfr.StackFilter", false, StackFilterTest.class.getClassLoader());
        } catch (ClassNotFoundException expected) {
            sfApiMissing = true;
        }
        check(sfApiMissing, "公共 API 无 jdk.jfr.StackFilter (实测 CNFE, 内部注解未导出)");
        Class<?> sfST = Class.forName("jdk.jfr.StackTrace", false, StackFilterTest.class.getClassLoader());
        check(sfST != null && sfST.isAnnotation(), "公共 API jdk.jfr.StackTrace 存在且为注解");
        check(!sfST.getName().equals(c.getName()), "内部 StackFilter 与公共 StackTrace 注解类型不同");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
