/*
 * @test
 * @summary javax.xml.ws.WebServiceFeature conformance: abstract feature base, protected enabled field contract, subclass enablement + getID override.
 * @run main WebServiceFeatureTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.xml.ws.WebServiceFeature;

public class WebServiceFeatureTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // 最小子类：protected 构造器 + enabled 字段继承
    static final class BoolFeature extends WebServiceFeature {
        BoolFeature() { }                 // 默认 enabled=false
        BoolFeature(boolean e) { enabled = e; }
        void flip() { enabled = true; }   // protected 字段在子类内可写
        @Override public String getID() { return "jck.BoolFeature"; }
    }

    // 只读视图子类（外部不可变，isEnabled 语义）
    static final class AlwaysOnFeature extends WebServiceFeature {
        AlwaysOnFeature() { enabled = true; }
        @Override public String getID() { return "jck.AlwaysOn"; }
        @Override public boolean isEnabled() { return true; }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.WebServiceFeature");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getID"), "has method getID");
        check(names.contains("isEnabled"), "has method isEnabled");
        testStructure(c);
        testSubclassBehavior();
        testBuiltInFeatures();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure(Class<?> c) throws Exception {
        // getID 抽象（子类必须覆写），isEnabled 具体（读 enabled 字段）
        check(Modifier.isAbstract(c.getMethod("getID").getModifiers()), "getID abstract");
        check(!Modifier.isAbstract(c.getMethod("isEnabled").getModifiers()), "isEnabled concrete");
        check(c.getMethod("getID").getReturnType() == String.class, "getID returns String");
        check(c.getMethod("isEnabled").getReturnType() == boolean.class, "isEnabled returns boolean");
        // protected boolean enabled 字段（子类可写）
        java.lang.reflect.Field f = c.getDeclaredField("enabled");
        check(Modifier.isProtected(f.getModifiers()), "enabled protected");
        check(f.getType() == boolean.class, "enabled type boolean");
        check(!Modifier.isStatic(f.getModifiers()), "enabled instance field");
        // protected 构造器：无 public 构造
        check(c.getConstructors().length == 0, "no public constructors");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        boolean hasProtectedNoArg = false;
        for (Constructor<?> ct : ctors) {
            if (Modifier.isProtected(ct.getModifiers()) && ct.getParameterTypes().length == 0) hasProtectedNoArg = true;
        }
        check(hasProtectedNoArg, "protected no-arg constructor present");
        // 直接实例化失败
        try {
            c.newInstance();
            check(false, "abstract instantiation must fail");
        } catch (InstantiationException | IllegalAccessException e) {
            check(true, "not instantiable: " + e.getClass().getSimpleName());
        }
        // 内置子类族存在性（feature 家族）
        check(Class.forName("javax.xml.ws.RespectBindingFeature") != null, "RespectBindingFeature exists");
        check(Class.forName("javax.xml.ws.soap.AddressingFeature") != null, "soap.AddressingFeature exists");
        check(Class.forName("javax.xml.ws.soap.MTOMFeature") != null, "soap.MTOMFeature exists");
    }

    static void testSubclassBehavior() {
        // protected 默认构造：enabled=false
        BoolFeature off = new BoolFeature();
        check(!off.isEnabled(), "default enabled false");
        check("jck.BoolFeature".equals(off.getID()), "getID override read back");
        // protected 字段直写：enabled=true
        BoolFeature on = new BoolFeature(true);
        check(on.isEnabled(), "explicit enabled true");
        // 同一实例翻转（protected 字段在子类内写）
        BoolFeature toggle = new BoolFeature();
        check(!toggle.isEnabled(), "initially off");
        toggle.flip();
        check(toggle.isEnabled(), "field flip visible via isEnabled");
        // isEnabled 覆写优先
        WebServiceFeature always = new AlwaysOnFeature();
        check(always.isEnabled(), "overridden isEnabled true");
        // 多态赋值
        WebServiceFeature ref = off;
        check(ref.getID().equals("jck.BoolFeature"), "polymorphic getID");
        check(ref == off, "same instance via base reference");
        // 特征数组/变参使用面（getPort/create/createDispatch 的 features... 形态）
        WebServiceFeature[] feats = new WebServiceFeature[]{off, on, always};
        check(feats.length == 3, "feature array");
        check(feats[0].isEnabled() != feats[1].isEnabled(), "off/on distinct in array");
        // 空数组形态合法
        javax.xml.ws.WebServiceFeature[] empty = new javax.xml.ws.WebServiceFeature[0];
        check(empty.length == 0, "empty feature vararg legal");
        // getID 每次调用一致
        check(off.getID().equals(off.getID()), "getID stable");
    }

    // 内置特征子类共享基类契约（isEnabled/getID 面）
    static void testBuiltInFeatures() throws Exception {
        javax.xml.ws.RespectBindingFeature rb = new javax.xml.ws.RespectBindingFeature();
        check(rb instanceof WebServiceFeature, "RespectBindingFeature instanceof WebServiceFeature");
        check(rb.isEnabled(), "RespectBinding default enabled true");
        check("javax.xml.ws.RespectBindingFeature".equals(rb.getID()), "RespectBinding getID");
        javax.xml.ws.soap.AddressingFeature af = new javax.xml.ws.soap.AddressingFeature();
        check(af instanceof WebServiceFeature, "AddressingFeature instanceof WebServiceFeature");
        check(af.isEnabled(), "Addressing default enabled true");
        javax.xml.ws.soap.MTOMFeature mf = new javax.xml.ws.soap.MTOMFeature();
        check(mf instanceof WebServiceFeature, "MTOMFeature instanceof WebServiceFeature");
        check(mf.isEnabled() == true || mf.isEnabled() == false, "MTOM isEnabled callable");
        // false 形态
        javax.xml.ws.RespectBindingFeature rbOff = new javax.xml.ws.RespectBindingFeature(false);
        check(!rbOff.isEnabled(), "RespectBinding(false) disabled");
        check(rbOff.getID().equals(rb.getID()), "getID same regardless of enabled");
        // @WebServiceFeatureAnnotation 元注解关联（spi 包）：注解 id/bean 成员
        Class<? extends java.lang.annotation.Annotation> anno =
            (Class<? extends java.lang.annotation.Annotation>) Class.forName("javax.xml.ws.spi.WebServiceFeatureAnnotation");
        Method id = anno.getMethod("id");
        Method bean = anno.getMethod("bean");
        check(id.getReturnType() == String.class, "@WebServiceFeatureAnnotation.id() String");
        check(bean.getReturnType() == Class.class, "@WebServiceFeatureAnnotation.bean() Class");
        // 元注解标注于 @RespectBinding 上：id == 特征 getID
        java.lang.annotation.Annotation rbAnno = Class.forName("javax.xml.ws.RespectBinding")
            .getAnnotation(anno);
        check(rbAnno != null, "@RespectBinding carries @WebServiceFeatureAnnotation");
        check(((String) id.invoke(rbAnno)).equals(rb.getID()), "annotation id == feature getID");
        check(((Class<?>) bean.invoke(rbAnno)) == javax.xml.ws.RespectBindingFeature.class,
              "annotation bean == RespectBindingFeature.class");
    }
}
