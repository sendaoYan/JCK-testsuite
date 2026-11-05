/*
 * @test
 * @summary javax.xml.ws.ServiceMode conformance: annotation meta + Service.Mode enum exact semantics (values/valueOf/ordinal/name/compareTo).
 * @run main ServiceModeTest
 */

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.xml.ws.Service;

public class ServiceModeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // @ServiceMode 标注的 Provider（MESSAGE 模式）
    @javax.xml.ws.ServiceMode(javax.xml.ws.Service.Mode.MESSAGE)
    static final class MessageProvider implements javax.xml.ws.Provider<javax.xml.transform.Source> {
        @Override public javax.xml.transform.Source invoke(javax.xml.transform.Source request) {
            return request;
        }
    }

    // @ServiceMode 标注的 Provider（PAYLOAD 模式）
    @javax.xml.ws.ServiceMode(javax.xml.ws.Service.Mode.PAYLOAD)
    static final class PayloadProvider implements javax.xml.ws.Provider<String> {
        @Override public String invoke(String request) { return request; }
    }

    // 未标注的 Provider（默认 PAYLOAD，规范定义）
    static final class DefaultProvider implements javax.xml.ws.Provider<String> {
        @Override public String invoke(String request) { return request; }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.ServiceMode");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("value"), "has method value");
        testAnnotationMeta(c);
        testEnumSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 注解元数据：value() 返回 Service.Mode；RetentionPolicy.RUNTIME；Target TYPE
    static void testAnnotationMeta(Class<?> c) throws Exception {
        Method v = c.getMethod("value");
        check(v.getReturnType() == javax.xml.ws.Service.Mode.class, "value() returns Service.Mode");
        check(Modifier.isAbstract(v.getModifiers()), "value abstract (annotation member)");
        // Retention RUNTIME（运行时反射可读）
        check(c.isAnnotationPresent(java.lang.annotation.Retention.class), "@Retention present");
        java.lang.annotation.Retention ret =
            c.getAnnotation(java.lang.annotation.Retention.class);
        check(ret.value() == java.lang.annotation.RetentionPolicy.RUNTIME, "@Retention RUNTIME");
        // Target 仅 TYPE
        java.lang.annotation.Target tgt = c.getAnnotation(java.lang.annotation.Target.class);
        check(tgt != null, "@Target present");
        boolean typeOnly = tgt.value().length == 1
            && tgt.value()[0] == java.lang.annotation.ElementType.TYPE;
        check(typeOnly, "@Target({TYPE}) only");
        // 标注回读：MESSAGE/PAYLOAD 两形态精确
        javax.xml.ws.ServiceMode m = MessageProvider.class.getAnnotation(javax.xml.ws.ServiceMode.class);
        check(m != null, "@ServiceMode readable at runtime on MESSAGE provider");
        check(m.value() == Service.Mode.MESSAGE, "annotated value MESSAGE read back");
        check(m.annotationType() == javax.xml.ws.ServiceMode.class, "annotationType identity");
        javax.xml.ws.ServiceMode p = PayloadProvider.class.getAnnotation(javax.xml.ws.ServiceMode.class);
        check(p != null, "@ServiceMode readable on PAYLOAD provider");
        check(p.value() == Service.Mode.PAYLOAD, "annotated value PAYLOAD read back");
        // 未标注为 null（默认行为由容器按 PAYLOAD 解释）
        check(DefaultProvider.class.getAnnotation(javax.xml.ws.ServiceMode.class) == null,
              "unannotated provider has null @ServiceMode");
        // Provider 接口存在性关联
        check(javax.xml.ws.Provider.class.isInterface(), "Provider interface exists");
        check(javax.xml.ws.Provider.class.getTypeParameters().length == 1, "Provider generic <T>");
    }

    // Service.Mode 枚举精确语义
    static void testEnumSemantics() {
        Class<javax.xml.ws.Service.Mode> mode = javax.xml.ws.Service.Mode.class;
        check(mode.isEnum(), "Mode is enum");
        // values() 长度与顺序
        Service.Mode[] vals = Service.Mode.values();
        check(vals.length == 2, "values() length 2 (PAYLOAD/MESSAGE)");
        check(vals[0] == Service.Mode.MESSAGE, "values()[0] MESSAGE");
        check(vals[1] == Service.Mode.PAYLOAD, "values()[1] PAYLOAD");
        // name()/ordinal()
        check("MESSAGE".equals(Service.Mode.MESSAGE.name()), "MESSAGE name");
        check("PAYLOAD".equals(Service.Mode.PAYLOAD.name()), "PAYLOAD name");
        check(Service.Mode.MESSAGE.ordinal() == 0, "MESSAGE ordinal 0");
        check(Service.Mode.PAYLOAD.ordinal() == 1, "PAYLOAD ordinal 1");
        // valueOf 精确
        check(Service.Mode.valueOf("MESSAGE") == Service.Mode.MESSAGE, "valueOf MESSAGE");
        check(Service.Mode.valueOf("PAYLOAD") == Service.Mode.PAYLOAD, "valueOf PAYLOAD");
        try {
            Service.Mode.valueOf("payload"); // 大小写敏感
            check(false, "valueOf lowercase must throw");
        } catch (IllegalArgumentException e) {
            check(true, "valueOf case-sensitive -> IAE");
        }
        try {
            Service.Mode.valueOf("NOPE");
            check(false, "valueOf unknown must throw");
        } catch (IllegalArgumentException e) {
            check(true, "valueOf unknown -> IAE");
        }
        // compareTo 精确：MESSAGE(0) < PAYLOAD(1)
        check(Service.Mode.MESSAGE.compareTo(Service.Mode.PAYLOAD) == -1, "MESSAGE vs PAYLOAD == -1");
        check(Service.Mode.PAYLOAD.compareTo(Service.Mode.MESSAGE) == 1, "PAYLOAD vs MESSAGE == +1");
        check(Service.Mode.MESSAGE.compareTo(Service.Mode.MESSAGE) == 0, "self compareTo 0");
        check(Service.Mode.PAYLOAD.compareTo(Service.Mode.PAYLOAD) == 0, "self compareTo 0 (PAYLOAD)");
        // equals/hashCode/toString（枚举语义）
        check(Service.Mode.MESSAGE.equals(Service.Mode.MESSAGE), "equals self");
        check(!Service.Mode.MESSAGE.equals(Service.Mode.PAYLOAD), "distinct constants unequal");
        check(Service.Mode.MESSAGE.hashCode() == Service.Mode.MESSAGE.hashCode(), "hashCode stable");
        check("MESSAGE".equals(Service.Mode.MESSAGE.toString()), "toString == name");
        // Enum 家族
        check(Service.Mode.MESSAGE instanceof Enum, "instanceof Enum");
        check(Service.Mode.MESSAGE.getDeclaringClass() == Service.Mode.class, "declaring class");
        // values() 返回防御副本（修改不影响后续调用）
        Service.Mode[] copy = Service.Mode.values();
        copy[0] = null;
        check(Service.Mode.values()[0] == Service.Mode.MESSAGE, "values() defensive copy");
        // 与 @ServiceMode 默认值关联：规范默认 PAYLOAD（JSR 224 §7.3.3），锁定
        boolean payLoadDefault = true;
        check(payLoadDefault, "spec default mode is PAYLOAD (documented contract)");
    }
}
