/*
 * @test
 * @summary javax.xml.ws.BindingType conformance: annotation metadata, empty-string default, binding id constants and Endpoint.create binding resolution channel.
 * @run main BindingTypeTest
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
import javax.xml.ws.BindingType;
import javax.xml.ws.Endpoint;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;


public class BindingTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // ---- 辅助实现类：显式 HTTP / SOAP 1.2 / 裸标注 / 未标注 四形态 ----
    @javax.jws.WebService(serviceName = "httpSvc", targetNamespace = "urn:t")
    @BindingType(HTTPBinding.HTTP_BINDING)
    public static class HttpImpl {
        public String echo(String s) { return s; }
    }

    @javax.jws.WebService(serviceName = "soap12Svc", targetNamespace = "urn:t")
    @BindingType(SOAPBinding.SOAP12HTTP_BINDING)
    public static class Soap12Impl {
        public String echo(String s) { return s; }
    }

    @javax.jws.WebService(serviceName = "soap11Svc", targetNamespace = "urn:t")
    @BindingType(SOAPBinding.SOAP11HTTP_BINDING)
    public static class Soap11Impl {
        public String echo(String s) { return s; }
    }

    @javax.jws.WebService(serviceName = "bareSvc", targetNamespace = "urn:t")
    @BindingType  // 裸标注：value 取默认 ""
    public static class BareImpl {
        public String echo(String s) { return s; }
    }

    @javax.jws.WebService(serviceName = "plainSvc", targetNamespace = "urn:t")
    public static class PlainImpl {  // 未标注，走默认绑定
        public String echo(String s) { return s; }
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testMetaAnnotations();
        testMemberContract();
        testConstants();
        testUsageReadback();
        testEndpointBindingChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.BindingType");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("value"), "has method value");
        check(c.getName().equals("javax.xml.ws.BindingType"), "binary name javax.xml.ws.BindingType");
        check(c.getDeclaredClasses().length == 0, "no declared member classes");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        check(c.getDeclaredConstructors().length == 0, "no declared constructors");
    }

    static void testMetaAnnotations() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.BindingType");
        Retention ret = c.getAnnotation(Retention.class);
        check(ret != null && ret.value() == RetentionPolicy.RUNTIME, "retention is RUNTIME");
        Target tgt = c.getAnnotation(Target.class);
        check(tgt != null, "Target meta-annotation present");
        java.util.Set<ElementType> ts = new java.util.HashSet<ElementType>();
        for (ElementType e : tgt.value()) ts.add(e);
        check(ts.size() == 1 && ts.contains(ElementType.TYPE), "Target is TYPE only");
        check(c.isAnnotationPresent(Documented.class), "Documented present");
        check(!c.isAnnotationPresent(Inherited.class), "Inherited absent (regression)");
        check(!c.isAnnotationPresent(Repeatable.class), "Repeatable absent (regression)");
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
        Class<?> c = Class.forName("javax.xml.ws.BindingType");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 1, "exactly 1 declared member, got " + ms.length);
        Method v = c.getDeclaredMethod("value");
        check(Modifier.isPublic(v.getModifiers()) && Modifier.isAbstract(v.getModifiers()), "value is public abstract");
        check(v.getReturnType() == String.class, "value returns String");
        // 关键默认值契约：JDK-8 源码 default ""（空串 = 让运行时用默认 SOAP1.1/HTTP 绑定）
        Object dv = v.getDefaultValue();
        check(dv != null, "value carries a default");
        check("".equals(dv), "value default is empty string (NOT a binding URI constant)");
    }

    static void testConstants() {
        check("http://schemas.xmlsoap.org/wsdl/soap/http".equals(SOAPBinding.SOAP11HTTP_BINDING),
              "SOAP11HTTP_BINDING URI");
        check("http://www.w3.org/2003/05/soap/bindings/HTTP/".equals(SOAPBinding.SOAP12HTTP_BINDING),
              "SOAP12HTTP_BINDING URI");
        check("http://www.w3.org/2004/08/wsdl/http".equals(HTTPBinding.HTTP_BINDING),
              "HTTP_BINDING URI");
        check(SOAPBinding.SOAP11HTTP_BINDING != null && !SOAPBinding.SOAP11HTTP_BINDING.equals(SOAPBinding.SOAP12HTTP_BINDING),
              "SOAP 1.1 / 1.2 binding URIs distinct");
        check(!HTTPBinding.HTTP_BINDING.equals(SOAPBinding.SOAP11HTTP_BINDING),
              "HTTP binding URI distinct from SOAP 1.1");
    }

    static void testUsageReadback() {
        BindingType http = HttpImpl.class.getAnnotation(BindingType.class);
        check(http != null, "HTTP binding readback non-null");
        check(HTTPBinding.HTTP_BINDING.equals(http.value()), "HTTP value readback equals constant");
        check(http.annotationType() == BindingType.class, "annotationType identity");
        BindingType s12 = Soap12Impl.class.getAnnotation(BindingType.class);
        check(SOAPBinding.SOAP12HTTP_BINDING.equals(s12.value()), "SOAP12 value readback");
        BindingType s11 = Soap11Impl.class.getAnnotation(BindingType.class);
        check(SOAPBinding.SOAP11HTTP_BINDING.equals(s11.value()), "SOAP11 value readback");
        // 裸标注：经实例回读默认 ""
        BindingType bare = BareImpl.class.getAnnotation(BindingType.class);
        check(bare != null, "bare BindingType present");
        check("".equals(bare.value()), "bare value defaults to empty string");
        // 未标注类
        check(PlainImpl.class.getAnnotation(BindingType.class) == null, "no BindingType on unannotated class");
        check(!PlainImpl.class.isAnnotationPresent(BindingType.class), "isAnnotationPresent false unannotated");
        // equals/hashCode/toString 通用契约
        check(bare.equals(BareImpl.class.getAnnotation(BindingType.class)), "bare instance value-equal");
        check(bare.hashCode() == BareImpl.class.getAnnotation(BindingType.class).hashCode(), "hashCode consistent");
        check(!bare.equals(http), "bare differs from HTTP-specified");
        check(bare.toString().startsWith("@javax.xml.ws.BindingType"), "toString prefix");
        // 方法级不可用（Target 仅 TYPE）
        check(BindingTypeTest.class.getAnnotation(BindingType.class) == null, "not applicable on holder class");
    }

    // 运行时通道：注解值驱动 Endpoint 绑定解析（仅 create 不 publish——HTTP 绑定无法生成 WSDL）
    static void testEndpointBindingChannel() {
        Endpoint e = Endpoint.create(new HttpImpl());
        check(e != null, "create HTTP-bound impl non-null");
        check(!e.isPublished(), "fresh endpoint unpublished");
        check(HTTPBinding.HTTP_BINDING.equals(e.getBinding().getBindingID()),
              "HTTP annotation drives bindingID");
        check(e.getImplementor() instanceof HttpImpl, "implementor preserved");
        Endpoint e12 = Endpoint.create(new Soap12Impl());
        check(SOAPBinding.SOAP12HTTP_BINDING.equals(e12.getBinding().getBindingID()),
              "SOAP12 annotation drives bindingID");
        Endpoint e11 = Endpoint.create(new Soap11Impl());
        check(SOAPBinding.SOAP11HTTP_BINDING.equals(e11.getBinding().getBindingID()),
              "SOAP11 annotation drives bindingID");
        // 裸 "" 与缺失注解均回落默认 SOAP1.1/HTTP
        Endpoint eBare = Endpoint.create(new BareImpl());
        check(SOAPBinding.SOAP11HTTP_BINDING.equals(eBare.getBinding().getBindingID()),
              "bare empty value falls back to default SOAP 1.1 binding");
        Endpoint ePlain = Endpoint.create(new PlainImpl());
        check(SOAPBinding.SOAP11HTTP_BINDING.equals(ePlain.getBinding().getBindingID()),
              "absent annotation falls back to default SOAP 1.1 binding");
        // SOAP 1.2 真实发布生命周期（localhost:0 ephemeral，立即 stop）
        Endpoint live = Endpoint.create(new Soap12Impl());
        live.publish("http://localhost:0/bt-s12");
        check(live.isPublished(), "SOAP12 endpoint published");
        check(SOAPBinding.SOAP12HTTP_BINDING.equals(live.getBinding().getBindingID()),
              "bindingID stable across publish");
        live.stop();
        check(!live.isPublished(), "SOAP12 endpoint stopped");
        // 显式 bindingId 参数可覆盖注解（显式 SOAP11 + 注解 HTTP → 参数优先）
        Endpoint override = Endpoint.create(SOAPBinding.SOAP11HTTP_BINDING, new HttpImpl());
        check(SOAPBinding.SOAP11HTTP_BINDING.equals(override.getBinding().getBindingID()),
              "explicit bindingId argument overrides annotation");
    }
}
