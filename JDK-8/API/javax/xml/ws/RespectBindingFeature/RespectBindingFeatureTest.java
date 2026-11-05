/*
 * @test
 * @summary javax.xml.ws.RespectBindingFeature conformance: constructor matrix, enabled read-back, ID constant, RespectBinding annotation linkage.
 * @run main RespectBindingFeatureTest
 */

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import javax.xml.ws.RespectBindingFeature;
import javax.xml.ws.WebServiceFeature;

public class RespectBindingFeatureTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // 标注 @RespectBinding 的辅助类（enabled 两种形态）
    @javax.xml.ws.RespectBinding
    static final class DefaultAnno { }

    @javax.xml.ws.RespectBinding(enabled = true)
    static final class EnabledAnno { }

    @javax.xml.ws.RespectBinding(enabled = false)
    static final class DisabledAnno { }

    public static void main(String[] args) throws Exception {
        javax.xml.ws.RespectBindingFeature f = new javax.xml.ws.RespectBindingFeature();
        check("javax.xml.ws.RespectBindingFeature".equals(f.getID()), "id");
        check(f.isEnabled(), "enabled");
        testConstructorMatrix();
        testConstantsAndStructure();
        testAnnotationLinkage();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testConstructorMatrix() {
        // ()：默认 enabled=true
        RespectBindingFeature dflt = new RespectBindingFeature();
        check(dflt.isEnabled(), "() default enabled true");
        // (true)
        RespectBindingFeature on = new RespectBindingFeature(true);
        check(on.isEnabled(), "(true) enabled");
        // (false)
        RespectBindingFeature off = new RespectBindingFeature(false);
        check(!off.isEnabled(), "(false) disabled");
        // getID 与 enabled 无关（同一 ID）
        check(on.getID().equals(off.getID()), "getID same across enabled states");
        check(on.getID().equals(dflt.getID()), "getID same as default ctor");
        // 多态赋值基类
        WebServiceFeature wf = dflt;
        check(wf instanceof RespectBindingFeature, "instanceof via base type");
        check(wf.isEnabled(), "isEnabled via base reference");
        check(wf.getID().equals("javax.xml.ws.RespectBindingFeature"), "getID via base reference");
        // 特征数组使用面
        WebServiceFeature[] arr = new WebServiceFeature[]{on, off};
        check(arr.length == 2 && arr[0].isEnabled() && !arr[1].isEnabled(), "feature array states");
    }

    static void testConstantsAndStructure() throws Exception {
        // ID 常量 == getID()
        check("javax.xml.ws.RespectBindingFeature".equals(RespectBindingFeature.ID),
              "ID constant exact string");
        check(RespectBindingFeature.ID.equals(new RespectBindingFeature().getID()),
              "ID constant == getID()");
        Class<?> c = RespectBindingFeature.class;
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == WebServiceFeature.class, "extends WebServiceFeature");
        java.lang.reflect.Field idf = c.getField("ID");
        check(Modifier.isStatic(idf.getModifiers()) && Modifier.isFinal(idf.getModifiers())
              && Modifier.isPublic(idf.getModifiers()), "ID public static final");
        check(idf.getType() == String.class, "ID type String");
        // getID 覆写
        check(c.getMethod("getID").getDeclaringClass() == RespectBindingFeature.class,
              "getID overridden here");
        // 恰两个 public 构造器
        Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 2, "exactly 2 public constructors: " + ctors.length);
        boolean hasNoArg = false, hasBool = false;
        for (Constructor<?> ct : ctors) {
            if (ct.getParameterTypes().length == 0) hasNoArg = true;
            if (ct.getParameterTypes().length == 1 && ct.getParameterTypes()[0] == boolean.class) hasBool = true;
        }
        check(hasNoArg, "no-arg constructor");
        check(hasBool, "(boolean) constructor");
        // isEnabled 未覆写（继承基类 enabled 字段语义）
        check(c.getMethod("isEnabled").getDeclaringClass() == WebServiceFeature.class,
              "isEnabled inherited from base");
    }

    // 与 @RespectBinding 注解的关联：@WebServiceFeatureAnnotation 元注解 id/bean
    static void testAnnotationLinkage() throws Exception {
        Class<? extends Annotation> annoType =
            (Class<? extends Annotation>) Class.forName("javax.xml.ws.RespectBinding");
        check(annoType.isAnnotation(), "@RespectBinding is annotation");
        // 元注解关联：id == RespectBindingFeature.ID，bean == RespectBindingFeature.class
        Class<? extends Annotation> wsfAnno =
            (Class<? extends Annotation>) Class.forName("javax.xml.ws.spi.WebServiceFeatureAnnotation");
        Annotation meta = annoType.getAnnotation(wsfAnno);
        check(meta != null, "@RespectBinding meta-annotated with @WebServiceFeatureAnnotation");
        java.lang.reflect.Method id = wsfAnno.getMethod("id");
        java.lang.reflect.Method bean = wsfAnno.getMethod("bean");
        check(RespectBindingFeature.ID.equals(id.invoke(meta)), "meta id == feature ID");
        check(RespectBindingFeature.class == bean.invoke(meta), "meta bean == feature class");
        // @RespectBinding 默认 enabled true
        javax.xml.ws.RespectBinding d = DefaultAnno.class.getAnnotation(javax.xml.ws.RespectBinding.class);
        check(d != null, "@RespectBinding readable at runtime");
        check(d.enabled(), "default enabled true");
        javax.xml.ws.RespectBinding t = EnabledAnno.class.getAnnotation(javax.xml.ws.RespectBinding.class);
        check(t.enabled(), "enabled=true read back");
        javax.xml.ws.RespectBinding f = DisabledAnno.class.getAnnotation(javax.xml.ws.RespectBinding.class);
        check(!f.enabled(), "enabled=false read back");
        check(t.annotationType() == annoType, "annotationType identity");
        // 未标注类为 null
        check(Unannotated.class.getAnnotation(javax.xml.ws.RespectBinding.class) == null,
              "unannotated class has null @RespectBinding");
        // Retention RUNTIME
        java.lang.annotation.Retention ret = annoType.getAnnotation(java.lang.annotation.Retention.class);
        check(ret != null && ret.value() == java.lang.annotation.RetentionPolicy.RUNTIME,
              "@Retention RUNTIME");
        // @Target 含 TYPE（实测：{TYPE, METHOD, FIELD}）
        java.lang.annotation.Target tgt = annoType.getAnnotation(java.lang.annotation.Target.class);
        check(tgt != null, "@Target present");
        boolean hasType = false;
        for (java.lang.annotation.ElementType et : tgt.value()) if (et == java.lang.annotation.ElementType.TYPE) hasType = true;
        check(hasType, "@Target includes TYPE");
        check(tgt.value().length == 3, "@Target({TYPE,METHOD,FIELD}) length 3 (locked)");
        // 注解 enabled() 方法签名
        java.lang.reflect.Method en = annoType.getMethod("enabled");
        check(en.getReturnType() == boolean.class, "enabled() returns boolean");
        check(en.getDefaultValue() != null && Boolean.TRUE.equals(en.getDefaultValue()),
              "enabled() default TRUE");
    }

    static final class Unannotated { }
}
