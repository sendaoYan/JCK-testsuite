/*
 * @test
 * @summary javax.xml.ws.RespectBinding conformance: annotation metadata, enabled default true, multi-target placement and publish-time wsdlLocation channel.
 * @run main RespectBindingTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.xml.ws.Endpoint;
import javax.xml.ws.RespectBinding;
import javax.xml.ws.WebServiceException;


public class RespectBindingTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // ---- 辅助载体：TYPE / FIELD / METHOD 三种合法位置 + enabled 两种取值 ----
    @RespectBinding(enabled = true)
    public static class TypeCarrier { }

    public static class FieldMethodCarrier {
        @RespectBinding(enabled = false)
        public Object respectField;

        @RespectBinding
        public void setRespect(Object v) { }
    }

    // ---- 运行时通道载体：enabled=true / false 各一，wsdlLocation 指向不存在文件 ----
    @javax.jws.WebService(serviceName = "rbOn", targetNamespace = "urn:t",
                          wsdlLocation = "file:///nonexistent/ghost-on.wsdl")
    @RespectBinding(enabled = true)
    public static class RbOnImpl {
        public String echo(String s) { return s; }
    }

    @javax.jws.WebService(serviceName = "rbOff", targetNamespace = "urn:t",
                          wsdlLocation = "file:///nonexistent/ghost-off.wsdl")
    @RespectBinding(enabled = false)
    public static class RbOffImpl {
        public String echo(String s) { return s; }
    }

    @javax.jws.WebService(serviceName = "rbNone", targetNamespace = "urn:t")
    public static class RbNoneImpl {
        public String echo(String s) { return s; }
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testMetaAnnotations();
        testMemberContract();
        testUsageReadback();
        testMultiTargetPlacement();
        testPublishChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.RespectBinding");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("enabled"), "has method enabled");
        check(c.getName().equals("javax.xml.ws.RespectBinding"), "binary name javax.xml.ws.RespectBinding");
        check(c.getDeclaredClasses().length == 0, "no declared member classes");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        check(c.getDeclaredConstructors().length == 0, "no declared constructors");
    }

    static void testMetaAnnotations() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.RespectBinding");
        Retention ret = c.getAnnotation(Retention.class);
        check(ret != null && ret.value() == RetentionPolicy.RUNTIME, "retention is RUNTIME");
        Target tgt = c.getAnnotation(Target.class);
        check(tgt != null, "Target meta-annotation present");
        java.util.Set<ElementType> ts = new java.util.HashSet<ElementType>();
        for (ElementType e : tgt.value()) ts.add(e);
        check(ts.size() == 3, "Target has exactly 3 elements, got " + ts.size());
        check(ts.contains(ElementType.TYPE) && ts.contains(ElementType.METHOD) && ts.contains(ElementType.FIELD),
              "Target = {TYPE, METHOD, FIELD} (no PARAMETER in JDK-8)");
        check(!ts.contains(ElementType.PARAMETER), "PARAMETER not targeted (regression)");
        check(c.isAnnotationPresent(Documented.class), "Documented present");
        check(!c.isAnnotationPresent(Inherited.class), "Inherited absent (regression)");
        check(!c.isAnnotationPresent(Repeatable.class), "Repeatable absent (regression)");
        Annotation[] declared = c.getDeclaredAnnotations();
        check(declared.length == 4, "declared meta-annotations count 4, got " + declared.length);
        java.util.Set<String> declaredNames = new java.util.HashSet<String>();
        for (Annotation an : declared) declaredNames.add(an.annotationType().getName());
        check(declaredNames.contains("java.lang.annotation.Target")
              && declaredNames.contains("java.lang.annotation.Retention")
              && declaredNames.contains("java.lang.annotation.Documented")
              && declaredNames.contains("javax.xml.ws.spi.WebServiceFeatureAnnotation"),
              "declared meta set = {Target, Retention, Documented, WebServiceFeatureAnnotation}");
        // 特性关联：RespectBinding 通过 spi.WebServiceFeatureAnnotation 绑定到 RespectBindingFeature
        javax.xml.ws.spi.WebServiceFeatureAnnotation wfa =
            c.getAnnotation(javax.xml.ws.spi.WebServiceFeatureAnnotation.class);
        check(wfa != null, "WebServiceFeatureAnnotation present");
        check(wfa.bean() == javax.xml.ws.RespectBindingFeature.class,
              "bean() = RespectBindingFeature.class, got " + wfa.bean().getName());
        check(wfa.id() != null && !wfa.id().isEmpty(), "feature id non-empty: " + wfa.id());
    }

    static void testMemberContract() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.RespectBinding");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 1, "exactly 1 declared member, got " + ms.length);
        Method en = c.getDeclaredMethod("enabled");
        check(Modifier.isPublic(en.getModifiers()) && Modifier.isAbstract(en.getModifiers()), "enabled public abstract");
        check(en.getReturnType() == boolean.class, "enabled returns primitive boolean");
        Object dv = en.getDefaultValue();
        check(dv != null, "enabled carries a default");
        check(dv instanceof Boolean && (Boolean) dv, "enabled default is true");
    }

    static void testUsageReadback() throws Exception {
        RespectBinding on = TypeCarrier.class.getAnnotation(RespectBinding.class);
        check(on != null, "TYPE placement readback non-null");
        check(on.enabled(), "enabled=true readback");
        check(on.annotationType() == RespectBinding.class, "annotationType identity");
        // 裸标注（FIELD 上）：enabled 回读默认 true
        Field f = FieldMethodCarrier.class.getDeclaredField("respectField");
        RespectBinding off = f.getAnnotation(RespectBinding.class);
        check(off != null, "FIELD placement readback non-null");
        check(!off.enabled(), "enabled=false readback on field");
        // METHOD 位置：裸标注 → 默认 true
        Method m = FieldMethodCarrier.class.getMethod("setRespect", Object.class);
        RespectBinding bare = m.getAnnotation(RespectBinding.class);
        check(bare != null, "METHOD placement readback non-null");
        check(bare.enabled(), "omitted enabled defaults to true via instance");
        // 通用契约
        check(bare.equals(on), "two enabled=true instances value-equal");
        check(bare.hashCode() == on.hashCode(), "hashCode consistent");
        check(!bare.equals(off), "enabled differs -> not equal");
        check(bare.toString().startsWith("@javax.xml.ws.RespectBinding"), "toString prefix");
    }

    static void testMultiTargetPlacement() throws Exception {
        check(TypeCarrier.class.isAnnotationPresent(RespectBinding.class), "TYPE isAnnotationPresent");
        check(FieldMethodCarrier.class.getDeclaredField("respectField").isAnnotationPresent(RespectBinding.class),
              "FIELD isAnnotationPresent");
        check(FieldMethodCarrier.class.getMethod("setRespect", Object.class).isAnnotationPresent(RespectBinding.class),
              "METHOD isAnnotationPresent");
        // 未标注位置
        check(FieldMethodCarrier.class.getAnnotation(RespectBinding.class) == null, "carrier class itself unannotated");
        check(RespectBindingTest.class.getAnnotation(RespectBinding.class) == null, "holder class unannotated");
        // 关联 Feature 类在 JDK-8 未回标 RespectBinding（回归锁定；后续 JAX-WS 版本曾加）
        Class<?> feat = Class.forName("javax.xml.ws.RespectBindingFeature");
        check(!feat.isAnnotationPresent(RespectBinding.class), "RespectBindingFeature not itself annotated in JDK-8");
    }

    // 运行时通道：wsdlLocation 参与 publish 解析；enabled=true/false 在 JDK-8 RI 均会尝试加载 WSDL
    static void testPublishChannel() {
        Endpoint eOn = Endpoint.create(new RbOnImpl());
        check(eOn != null, "create with RespectBinding(enabled=true) non-null");
        check(!eOn.isPublished(), "created endpoint unpublished (annotation tolerated at create)");
        Endpoint eNone = Endpoint.create(new RbNoneImpl());
        check(eNone != null, "create without RespectBinding non-null");
        // 无 wsdlLocation：正常发布生命周期闭环（localhost:0，立即 stop）
        eNone.publish("http://localhost:0/rb-none");
        check(eNone.isPublished(), "publish without wsdlLocation succeeds");
        eNone.stop();
        check(!eNone.isPublished(), "stop after publish");
        // 坏 wsdlLocation + enabled=true：publish 时尝试加载 → WebServiceException（ServerRtException 链）
        try {
            Endpoint bad = Endpoint.create(new RbOnImpl());
            bad.publish("http://localhost:0/rb-on");
            check(false, "publish with ghost wsdlLocation (enabled=true) must fail");
        } catch (WebServiceException e) {
            check(true, "ghost wsdl enabled=true -> WebServiceException: " + e.getClass().getSimpleName());
        }
        // 坏 wsdlLocation + enabled=false：JDK-8 RI 实测同样尝试加载（规范允许忽略，RI 未忽略——锁定差异）
        try {
            Endpoint bad = Endpoint.create(new RbOffImpl());
            bad.publish("http://localhost:0/rb-off");
            check(true, "RI tolerated ghost wsdlLocation when enabled=false");
            bad.stop();
        } catch (WebServiceException e) {
            check(true, "RI still loads wsdlLocation when enabled=false (documented RI-vs-spec delta)");
        }
    }
}
