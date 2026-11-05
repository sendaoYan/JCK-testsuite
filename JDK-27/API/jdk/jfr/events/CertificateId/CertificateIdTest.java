/*
 * @test
 * @summary jdk.jfr.events.CertificateId conformance: type shape.
 * @run main CertificateIdTest
 */

import java.lang.reflect.Modifier;

public class CertificateIdTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.events.CertificateId");
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
        java.lang.annotation.Target tgt = c.getAnnotation(java.lang.annotation.Target.class);
        check(tgt != null && java.util.Arrays.asList(tgt.value()).equals(java.util.Arrays.asList(java.lang.annotation.ElementType.FIELD)), "@Target == {FIELD}");
        java.lang.annotation.Retention ret = c.getAnnotation(java.lang.annotation.Retention.class);
        check(ret != null && ret.value() == java.lang.annotation.RetentionPolicy.RUNTIME, "@Retention == RUNTIME");
        jdk.jfr.Label lbl = c.getAnnotation(jdk.jfr.Label.class);
        check(lbl != null && "X509 Certificate Id".equals(lbl.value()), "@Label == \"X509 Certificate Id\"");
        check(c.isAnnotationPresent(jdk.jfr.Relational.class), "@jdk.jfr.Relational 元注解存在");
        check(c.getDeclaredMethods().length == 0, "无成员");
        // 使用点闭环: X509 事件族的 certificateId 字段携带本注解
        Class<?> ciTLS = Class.forName("jdk.jfr.events.TLSHandshakeEvent", false, CertificateIdTest.class.getClassLoader());
        check(ciTLS.getDeclaredField("certificateId").getAnnotation((Class<? extends java.lang.annotation.Annotation>) c) != null, "TLSHandshakeEvent.certificateId 携带 @CertificateId");
        Class<?> ciX = Class.forName("jdk.jfr.events.X509CertificateEvent", false, CertificateIdTest.class.getClassLoader());
        check(ciX.getDeclaredField("certificateId").getAnnotation((Class<? extends java.lang.annotation.Annotation>) c) != null, "X509CertificateEvent.certificateId 携带 @CertificateId");
        // 关系注解语义: jdk.X509Certificate 与 jdk.X509Validation 的 certificateId 字段构成关联
        // (实测: @CertificateId 属关系注解, 不进运行时字段元数据 AnnotationElement; 元数据侧以 Label="Certificate Id" 呈现)
        int ciRel = 0;
        for (jdk.jfr.EventType t : jdk.jfr.FlightRecorder.getFlightRecorder().getEventTypes()) {
            if (!"jdk.X509Certificate".equals(t.getName()) && !"jdk.X509Validation".equals(t.getName())) continue;
            boolean hasField = false; boolean lblOk = false; boolean unsigned = false;
            for (jdk.jfr.ValueDescriptor vd : t.getFields()) {
                if (!"certificateId".equals(vd.getName())) continue;
                hasField = true;
                for (jdk.jfr.AnnotationElement ae : vd.getAnnotationElements()) {
                    if (ae.getTypeName().equals("jdk.jfr.Label") && "Certificate Id".equals(ae.getValue("value"))) lblOk = true;
                    if (ae.getTypeName().equals("jdk.jfr.Unsigned")) unsigned = true;
                }
            }
            check(hasField, t.getName() + ".certificateId 字段在册");
            check(lblOk, t.getName() + ".certificateId 字段 @Label == \"Certificate Id\"");
            check(unsigned, t.getName() + ".certificateId 字段 @Unsigned 在册");
            ciRel++;
        }
        check(ciRel == 2, "两个 X509 事件类型均在册, 实得 " + ciRel);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
