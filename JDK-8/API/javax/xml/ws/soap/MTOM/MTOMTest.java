/*
 * @test
 * @summary javax.xml.ws.soap.MTOM conformance: annotation defaults (enabled true, threshold 0), meta-annotations, feature-annotation linkage to MTOMFeature and placement readback.
 * @run main MTOMTest
 */

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
import javax.xml.ws.soap.MTOM;
import javax.xml.ws.soap.MTOMFeature;

public class MTOMTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @MTOM
    static class DefaultUse { }

    @MTOM(enabled = false, threshold = 4096)
    static class CustomUse { }

    static class PlacementUse {
        @MTOM(threshold = 1024)
        public void operation() { }

        @MTOM(enabled = false)
        public String field;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.soap.MTOM");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("enabled"), "has method enabled");
        check(names.contains("threshold"), "has method threshold");
        testMemberFace(c);
        testMetaAnnotations(c);
        testDefaultsAndReadback();
        testFeatureAnnotationLinkage(c);
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 成员面：enabled 默认 true，threshold 默认 0
    static void testMemberFace(Class<?> c) throws Exception {
        Method[] dm = c.getDeclaredMethods();
        check(dm.length == 2, "exactly 2 members: " + dm.length);
        Method en = c.getMethod("enabled");
        check(en.getReturnType() == boolean.class, "enabled returns boolean");
        check(((Boolean) en.getDefaultValue()).booleanValue() == true, "enabled default true");
        Method th = c.getMethod("threshold");
        check(th.getReturnType() == int.class, "threshold returns int");
        check(((Integer) th.getDefaultValue()).intValue() == 0, "threshold default 0");
        for (Method m : dm) {
            check(m.getParameterTypes().length == 0, m.getName() + " no args");
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                  m.getName() + " public abstract");
        }
    }

    // 元注解：与 Addressing 相同的 Target 三元 + RUNTIME + Documented
    static void testMetaAnnotations(Class<?> c) {
        Target t = c.getAnnotation(Target.class);
        check(t != null, "has @Target");
        Set<ElementType> targets = new HashSet<ElementType>(Arrays.asList(t.value()));
        check(targets.size() == 3, "exactly 3 targets: " + targets.size());
        check(targets.contains(ElementType.TYPE) && targets.contains(ElementType.METHOD)
              && targets.contains(ElementType.FIELD), "targets TYPE/METHOD/FIELD");
        check(!targets.contains(ElementType.PARAMETER), "no PARAMETER target");
        Retention r = c.getAnnotation(Retention.class);
        check(r != null && r.value() == RetentionPolicy.RUNTIME, "retention RUNTIME");
        check(c.isAnnotationPresent(Documented.class), "@Documented present");
        check(!c.isAnnotationPresent(java.lang.annotation.Inherited.class), "@Inherited absent");
    }

    // 读回：默认/自定义/位置
    static void testDefaultsAndReadback() throws Exception {
        MTOM d = DefaultUse.class.getAnnotation(MTOM.class);
        check(d != null, "default annotation readable");
        check(d.enabled() == true, "default enabled true");
        check(d.threshold() == 0, "default threshold 0");

        MTOM cu = CustomUse.class.getAnnotation(MTOM.class);
        check(cu.enabled() == false, "custom enabled false");
        check(cu.threshold() == 4096, "custom threshold 4096");

        Method m = PlacementUse.class.getMethod("operation");
        MTOM ma = m.getAnnotation(MTOM.class);
        check(ma != null && ma.threshold() == 1024 && ma.enabled() == true,
              "method-placed readback (threshold explicit, enabled default)");
        java.lang.reflect.Field f = PlacementUse.class.getField("field");
        MTOM fa = f.getAnnotation(MTOM.class);
        check(fa != null && fa.enabled() == false && fa.threshold() == 0,
              "field-placed readback (enabled explicit, threshold default)");
        // 同值注解 equals/hashCode
        check(d.equals(DefaultUse2.class.getAnnotation(MTOM.class)), "same values equal");
        check(d.hashCode() == DefaultUse2.class.getAnnotation(MTOM.class).hashCode(), "same hashCode");
        check(d.toString().contains("javax.xml.ws.soap.MTOM"), "toString mentions type");
        check(d.annotationType() == MTOM.class, "annotationType is MTOM");
    }

    @MTOM
    static class DefaultUse2 { }

    // 与 WebServiceFeatureAnnotation/MTOMFeature 的联动
    static void testFeatureAnnotationLinkage(Class<?> c) {
        javax.xml.ws.spi.WebServiceFeatureAnnotation wfa =
            c.getAnnotation(javax.xml.ws.spi.WebServiceFeatureAnnotation.class);
        check(wfa != null, "meta-annotated with WebServiceFeatureAnnotation");
        check("http://www.w3.org/2004/08/soap/features/http-optimization".equals(wfa.id()),
              "feature id is MTOM URI: " + wfa.id());
        check(wfa.bean() == MTOMFeature.class, "feature bean is MTOMFeature");
        check(MTOMFeature.ID.equals(wfa.id()), "id equals MTOMFeature.ID");
        // 注解默认与 Feature 默认一致
        check(new MTOMFeature().isEnabled() == DefaultUse.class.getAnnotation(MTOM.class).enabled(),
              "enabled defaults agree (both true)");
        check(new MTOMFeature().getThreshold() == DefaultUse.class.getAnnotation(MTOM.class).threshold(),
              "threshold defaults agree (both 0)");
    }
}
