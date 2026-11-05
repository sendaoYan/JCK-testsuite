/*
 * @test
 * @summary javax.xml.ws.WebServiceRefs conformance: container annotation metadata, mandatory value array, multi-reference readback on type.
 * @run main WebServiceRefsTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;


public class WebServiceRefsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @javax.jws.WebService(name = "APort", targetNamespace = "urn:a")
    public interface SeiA { String a(); }

    @javax.jws.WebService(name = "BPort", targetNamespace = "urn:b")
    public interface SeiB { String b(); }

    public static class SvcA extends Service {
        protected SvcA() { super(null, new javax.xml.namespace.QName("urn:a", "svcA")); }
    }

    public static class SvcB extends Service {
        protected SvcB() { super(null, new javax.xml.namespace.QName("urn:b", "svcB")); }
    }

    // ---- 容器标注：两项引用，逐成员精确 ----
    @WebServiceRefs({ @WebServiceRef(name = "refA", type = SeiA.class, value = SvcA.class,
                                     wsdlLocation = "http://x/a?wsdl", lookup = "java:comp/env/a"),
                     @WebServiceRef(name = "refB", type = SeiB.class) })
    public static class MultiCarrier { }

    // ---- 单项容器 ----
    @WebServiceRefs({ @WebServiceRef(name = "only", type = SeiA.class) })
    public static class SingleCarrier { }

    // ---- 裸容器：value 无默认值，裸用即编译错误（本测试以源码契约 + getDefaultValue 锁定） ----
    public static class PlainCarrier { }

    public static void main(String[] args) throws Exception {
        testStructure();
        testMetaAnnotations();
        testMemberContract();
        testUsageReadback();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.WebServiceRefs");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("value"), "has method value");
        check(c.getName().equals("javax.xml.ws.WebServiceRefs"), "binary name javax.xml.ws.WebServiceRefs");
        check(c.getDeclaredClasses().length == 0, "no declared member classes");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        check(c.getDeclaredConstructors().length == 0, "no declared constructors");
    }

    static void testMetaAnnotations() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.WebServiceRefs");
        Retention ret = c.getAnnotation(Retention.class);
        check(ret != null && ret.value() == RetentionPolicy.RUNTIME, "retention is RUNTIME");
        Target tgt = c.getAnnotation(Target.class);
        check(tgt != null, "Target meta-annotation present");
        java.util.Set<ElementType> ts = new java.util.HashSet<ElementType>();
        for (ElementType e : tgt.value()) ts.add(e);
        check(ts.size() == 1 && ts.contains(ElementType.TYPE), "Target is TYPE only");
        check(c.isAnnotationPresent(Documented.class), "Documented present");
        check(!c.isAnnotationPresent(Inherited.class), "Inherited absent (regression)");
        check(!c.isAnnotationPresent(Repeatable.class), "Repeatable absent (it IS the container, regression)");
        Annotation[] declared = c.getDeclaredAnnotations();
        check(declared.length == 3, "declared meta-annotations count 3, got " + declared.length);
        java.util.Set<String> declaredNames = new java.util.HashSet<String>();
        for (Annotation an : declared) declaredNames.add(an.annotationType().getName());
        check(declaredNames.contains("java.lang.annotation.Target")
              && declaredNames.contains("java.lang.annotation.Retention")
              && declaredNames.contains("java.lang.annotation.Documented"),
              "declared meta set = {Target, Retention, Documented}");
    }

    static void testMemberContract() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.WebServiceRefs");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 1, "exactly 1 declared member, got " + ms.length);
        Method v = c.getDeclaredMethod("value");
        check(Modifier.isPublic(v.getModifiers()) && Modifier.isAbstract(v.getModifiers()), "value public abstract");
        check(v.getReturnType() == WebServiceRef[].class, "value returns WebServiceRef[]");
        check(v.getDefaultValue() == null, "value has NO default (mandatory container content)");
        // 嵌套元素类型：与单引用注解同一 Class 对象
        check(v.getReturnType().getComponentType() == WebServiceRef.class, "component type identity WebServiceRef");
    }

    static void testUsageReadback() {
        WebServiceRefs container = MultiCarrier.class.getAnnotation(WebServiceRefs.class);
        check(container != null, "container readback non-null");
        check(container.annotationType() == WebServiceRefs.class, "annotationType identity");
        WebServiceRef[] refs = container.value();
        check(refs != null, "value array non-null");
        check(refs.length == 2, "two entries, got " + refs.length);
        check(refs[0].annotationType() == WebServiceRef.class, "entry[0] is WebServiceRef");
        check("refA".equals(refs[0].name()), "entry[0].name readback");
        check(refs[0].type() == SeiA.class, "entry[0].type readback");
        check(refs[0].value() == SvcA.class, "entry[0].value readback");
        check("http://x/a?wsdl".equals(refs[0].wsdlLocation()), "entry[0].wsdlLocation readback");
        check("java:comp/env/a".equals(refs[0].lookup()), "entry[0].lookup readback");
        check("refB".equals(refs[1].name()), "entry[1].name readback");
        check(refs[1].type() == SeiB.class, "entry[1].type readback");
        check(refs[1].value() == Service.class, "entry[1].value falls back to Service default");
        check("".equals(refs[1].lookup()), "entry[1].lookup falls back to empty");
        check(!refs[0].equals(refs[1]), "entries distinct");
        // 单项容器
        WebServiceRefs single = SingleCarrier.class.getAnnotation(WebServiceRefs.class);
        check(single != null, "single-entry container non-null");
        check(single.value().length == 1, "single-entry length 1");
        check("only".equals(single.value()[0].name()), "single entry name readback");
        // 通用契约
        check(container.equals(MultiCarrier.class.getAnnotation(WebServiceRefs.class)), "container value-equal");
        check(container.toString().startsWith("@javax.xml.ws.WebServiceRefs"), "toString prefix");
        // 未标注类与非法位置
        check(PlainCarrier.class.getAnnotation(WebServiceRefs.class) == null, "plain class unannotated");
        check(!WebServiceRefsTest.class.isAnnotationPresent(WebServiceRefs.class), "holder class presence false");
        // WebServiceRef 本身非可重复：直接标注两次无法通过编译——以容器存在性 + 单注解无 Repeatable 锁定
        check(!WebServiceRef.class.isAnnotationPresent(Repeatable.class),
              "WebServiceRef not Repeatable; multi-declaration must go through WebServiceRefs");
    }
}
