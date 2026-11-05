/*
 * @test
 * @summary javax.xml.ws.soap.Addressing conformance: WS-Addressing annotation defaults (enabled/required/responses), meta-annotations (Target/Retention), feature-annotation linkage to AddressingFeature.
 * @run main AddressingTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.AddressingFeature;

public class AddressingTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // 默认值标注样例
    @Addressing
    static class DefaultUse { }

    // 自定义值标注样例
    @Addressing(enabled = false, required = true, responses = AddressingFeature.Responses.ANONYMOUS)
    static class CustomUse { }

    // 方法与字段位置标注样例
    static class PlacementUse {
        @Addressing(enabled = true)
        public void operation() { }

        @Addressing(required = true)
        public String field;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.soap.Addressing");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("enabled"), "has method enabled");
        check(names.contains("required"), "has method required");
        check(names.contains("responses"), "has method responses");
        testMemberFace(c);
        testMetaAnnotations(c);
        testDefaultsAndReadback();
        testFeatureAnnotationLinkage(c);
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 成员面：三个属性及默认值
    static void testMemberFace(Class<?> c) throws Exception {
        Method[] dm = c.getDeclaredMethods();
        check(dm.length == 3, "exactly 3 members: " + dm.length);
        Method en = c.getMethod("enabled");
        check(en.getReturnType() == boolean.class, "enabled returns boolean");
        check(en.getDefaultValue() != null && ((Boolean) en.getDefaultValue()).booleanValue() == true,
              "enabled default true");
        Method rq = c.getMethod("required");
        check(rq.getReturnType() == boolean.class, "required returns boolean");
        check(rq.getDefaultValue() != null && ((Boolean) rq.getDefaultValue()).booleanValue() == false,
              "required default false");
        Method rs = c.getMethod("responses");
        check(rs.getReturnType() == AddressingFeature.Responses.class, "responses returns Responses enum");
        check(rs.getDefaultValue() == AddressingFeature.Responses.ALL, "responses default ALL");
        for (Method m : dm) {
            check(m.getParameterTypes().length == 0, m.getName() + " no args");
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                  m.getName() + " public abstract");
        }
    }

    // 元注解：Target/Retention/Documented
    static void testMetaAnnotations(Class<?> c) {
        Target t = c.getAnnotation(Target.class);
        check(t != null, "has @Target");
        Set<ElementType> targets = new HashSet<ElementType>(Arrays.asList(t.value()));
        check(targets.size() == 3, "exactly 3 targets: " + targets.size());
        check(targets.contains(ElementType.TYPE), "target TYPE");
        check(targets.contains(ElementType.METHOD), "target METHOD");
        check(targets.contains(ElementType.FIELD), "target FIELD");
        check(!targets.contains(ElementType.PARAMETER), "no PARAMETER target");
        Retention r = c.getAnnotation(Retention.class);
        check(r != null && r.value() == RetentionPolicy.RUNTIME, "retention RUNTIME");
        check(c.isAnnotationPresent(Documented.class), "@Documented present");
        check(!c.isAnnotationPresent(java.lang.annotation.Inherited.class), "@Inherited absent");
    }

    // 标注读回：默认/自定义/位置
    static void testDefaultsAndReadback() throws Exception {
        Addressing d = DefaultUse.class.getAnnotation(Addressing.class);
        check(d != null, "default annotation readable");
        check(d.enabled() == true, "default enabled readback true");
        check(d.required() == false, "default required readback false");
        check(d.responses() == AddressingFeature.Responses.ALL, "default responses readback ALL");

        Addressing cu = CustomUse.class.getAnnotation(Addressing.class);
        check(cu.enabled() == false, "custom enabled readback false");
        check(cu.required() == true, "custom required readback true");
        check(cu.responses() == AddressingFeature.Responses.ANONYMOUS, "custom responses readback ANONYMOUS");

        // 部分指定：其余取默认
        Addressing partial = CustomUse.class.getAnnotation(Addressing.class);
        check(partial.responses() != AddressingFeature.Responses.ALL || partial.enabled() == false,
              "mixed values do not bleed defaults");

        Method m = PlacementUse.class.getMethod("operation");
        Addressing ma = m.getAnnotation(Addressing.class);
        check(ma != null && ma.enabled() == true && ma.required() == false,
              "method-placed annotation readback with defaults");
        java.lang.reflect.Field f = PlacementUse.class.getField("field");
        Addressing fa = f.getAnnotation(Addressing.class);
        check(fa != null && fa.required() == true && fa.enabled() == true,
              "field-placed annotation readback with defaults");
        // toString/equals/hashCode 由注解代理提供
        check(d.toString().contains("javax.xml.ws.soap.Addressing"), "toString mentions annotation type");
        check(!d.equals(cu), "different values not equal");
        check(d.equals(DefaultUse2.class.getAnnotation(Addressing.class)), "same values equal");
        check(d.hashCode() == DefaultUse2.class.getAnnotation(Addressing.class).hashCode(),
              "same values same hashCode");
        check(d.annotationType() == Addressing.class, "annotationType is Addressing");
    }

    @Addressing
    static class DefaultUse2 { }

    // 与 WebServiceFeatureAnnotation 的联动（元注解）
    static void testFeatureAnnotationLinkage(Class<?> c) throws Exception {
        javax.xml.ws.spi.WebServiceFeatureAnnotation wfa =
            c.getAnnotation(javax.xml.ws.spi.WebServiceFeatureAnnotation.class);
        check(wfa != null, "meta-annotated with WebServiceFeatureAnnotation");
        check("http://www.w3.org/2005/08/addressing/module".equals(wfa.id()),
              "feature id is addressing module URI: " + wfa.id());
        check(wfa.bean() == AddressingFeature.class, "feature bean is AddressingFeature");
        // id 与 AddressingFeature.ID 一致
        check(AddressingFeature.ID.equals(wfa.id()), "id equals AddressingFeature.ID");
        // bean 反映默认 responses 与注解默认一致
        check(new AddressingFeature().getResponses() == Addressing.class.getMethod("responses").getDefaultValue(),
              "feature default responses matches annotation default");
    }
}
