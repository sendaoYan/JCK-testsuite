/*
 * @test
 * @summary javax.xml.ws.spi.WebServiceFeatureAnnotation conformance: member face (id/bean, no defaults), meta-annotations (Target ANNOTATION_TYPE, RUNTIME), linkage on Addressing/MTOM/RespectBinding and a custom feature annotation.
 * @run main WebServiceFeatureAnnotationTest
 */

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.xml.ws.RespectBinding;
import javax.xml.ws.RespectBindingFeature;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.spi.WebServiceFeatureAnnotation;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.MTOM;
import javax.xml.ws.soap.MTOMFeature;

public class WebServiceFeatureAnnotationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // 自造 feature 注解（元标注本注解）
    @WebServiceFeatureAnnotation(id = "urn:test:myfeature", bean = AddressingFeature.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface MyFeature { }

    @MyFeature
    static class Annotated { }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.spi.WebServiceFeatureAnnotation");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("id"), "has method id");
        check(names.contains("bean"), "has method bean");
        testMemberFace(c);
        testMetaAnnotations(c);
        testLinkage();
        testCustomAnnotation();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 成员面：id/bean 两成员，无默认值（必填）
    static void testMemberFace(Class<?> c) throws Exception {
        Method[] dm = c.getDeclaredMethods();
        check(dm.length == 2, "exactly 2 members: " + dm.length);
        Method id = c.getMethod("id");
        check(id.getReturnType() == String.class, "id returns String");
        check(id.getDefaultValue() == null, "id has no default (required member)");
        check(id.getParameterTypes().length == 0, "id no args");
        Method bean = c.getMethod("bean");
        check(bean.getReturnType() == Class.class, "bean returns Class");
        check(bean.getDefaultValue() == null, "bean has no default (required member)");
        check(bean.getParameterTypes().length == 0, "bean no args");
        // bean 的类型形参上界为 WebServiceFeature
        check(bean.getGenericReturnType().getTypeName()
              .equals("java.lang.Class<? extends javax.xml.ws.WebServiceFeature>"),
              "bean generic type bounded by WebServiceFeature: " + bean.getGenericReturnType().getTypeName());
        for (Method m : dm) {
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                  m.getName() + " public abstract");
        }
    }

    // 元注解：仅可标注注解类型；RUNTIME 保留
    static void testMetaAnnotations(Class<?> c) {
        Target t = c.getAnnotation(Target.class);
        check(t != null, "has @Target");
        check(t.value().length == 1 && t.value()[0] == ElementType.ANNOTATION_TYPE,
              "@Target is exactly ANNOTATION_TYPE (meta-annotation only): "
              + java.util.Arrays.toString(t.value()));
        Retention r = c.getAnnotation(Retention.class);
        check(r != null && r.value() == RetentionPolicy.RUNTIME, "@Retention RUNTIME");
        check(c.isAnnotationPresent(Documented.class), "@Documented present");
    }

    // 与标准 feature 注解的联动：id/bean 与对应 Feature 一致
    static void testLinkage() {
        WebServiceFeatureAnnotation a = Addressing.class.getAnnotation(WebServiceFeatureAnnotation.class);
        check(a != null, "@Addressing carries WebServiceFeatureAnnotation");
        check("http://www.w3.org/2005/08/addressing/module".equals(a.id()), "Addressing id");
        check(a.bean() == AddressingFeature.class, "Addressing bean");
        check(AddressingFeature.ID.equals(a.id()), "Addressing id == AddressingFeature.ID");

        WebServiceFeatureAnnotation m = MTOM.class.getAnnotation(WebServiceFeatureAnnotation.class);
        check(m != null, "@MTOM carries WebServiceFeatureAnnotation");
        check("http://www.w3.org/2004/08/soap/features/http-optimization".equals(m.id()), "MTOM id");
        check(m.bean() == MTOMFeature.class, "MTOM bean");
        check(MTOMFeature.ID.equals(m.id()), "MTOM id == MTOMFeature.ID");

        WebServiceFeatureAnnotation rb = RespectBinding.class.getAnnotation(WebServiceFeatureAnnotation.class);
        check(rb != null, "@RespectBinding carries WebServiceFeatureAnnotation");
        check("javax.xml.ws.RespectBindingFeature".equals(rb.id()), "RespectBinding id (simple form)");
        check(rb.bean() == RespectBindingFeature.class, "RespectBinding bean");

        // bean 一律是 WebServiceFeature 子类
        check(WebServiceFeature.class.isAssignableFrom(a.bean()), "Addressing bean is a WebServiceFeature");
        check(WebServiceFeature.class.isAssignableFrom(m.bean()), "MTOM bean is a WebServiceFeature");
        check(WebServiceFeature.class.isAssignableFrom(rb.bean()), "RespectBinding bean is a WebServiceFeature");
        // 非 feature 注解不携带
        check(javax.jws.WebService.class.getAnnotation(WebServiceFeatureAnnotation.class) == null,
              "@WebService is not a feature annotation");
    }

    // 自造 feature 注解：元标注读回
    static void testCustomAnnotation() {
        MyFeature mf = Annotated.class.getAnnotation(MyFeature.class);
        check(mf != null, "custom feature annotation readable at runtime");
        WebServiceFeatureAnnotation meta = MyFeature.class.getAnnotation(WebServiceFeatureAnnotation.class);
        check(meta != null, "custom annotation carries WebServiceFeatureAnnotation meta");
        check("urn:test:myfeature".equals(meta.id()), "custom id round-trip");
        check(meta.bean() == AddressingFeature.class, "custom bean round-trip");
        // 经元注解可发现 bean 的 Feature 实例默认 ID
        check(new AddressingFeature().getID().equals(AddressingFeature.ID),
              "bean feature reports its own ID");
    }
}
