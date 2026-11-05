/*
 * @test
 * @summary javax.xml.ws.WebServiceProvider conformance: annotation metadata, four-member defaults, Provider endpoint create and publish lifecycle closure.
 * @run main WebServiceProviderTest
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
import javax.xml.transform.Source;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.http.HTTPBinding;


public class WebServiceProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // ---- 全成员 Provider（XML/Source PAYLOAD 形态） ----
    @WebServiceProvider(serviceName = "psvc", portName = "pport",
                        targetNamespace = "urn:p", wsdlLocation = "http://x/prov?wsdl")
    public static class FullProvider implements Provider<Source> {
        public Source invoke(Source request) { return request; }
    }

    // ---- 裸标注 Provider（默认值载体；无 wsdlLocation 才能发布） ----
    @WebServiceProvider
    public static class BareProvider implements Provider<Source> {
        public Source invoke(Source request) { return request; }
    }

    // ---- MESSAGE 模式 Provider ----
    @ServiceMode(value = javax.xml.ws.Service.Mode.MESSAGE)
    @WebServiceProvider(serviceName = "msgSvc", portName = "msgPort", targetNamespace = "urn:m")
    public static class MessageProvider implements Provider<Source> {
        public Source invoke(Source request) { return request; }
    }

    // ---- 坏 wsdlLocation 的 Provider（发布期负例） ----
    @WebServiceProvider(serviceName = "ghostSvc", portName = "ghostPort",
                        targetNamespace = "urn:g", wsdlLocation = "file:///nonexistent/ghost.wsdl")
    public static class GhostProvider implements Provider<Source> {
        public Source invoke(Source request) { return request; }
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testMetaAnnotations();
        testMemberContract();
        testUsageReadback();
        testEndpointChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.WebServiceProvider");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("wsdlLocation"), "has method wsdlLocation");
        check(names.contains("serviceName"), "has method serviceName");
        check(names.contains("targetNamespace"), "has method targetNamespace");
        check(names.contains("portName"), "has method portName");
        check(c.getName().equals("javax.xml.ws.WebServiceProvider"), "binary name javax.xml.ws.WebServiceProvider");
        check(c.getDeclaredClasses().length == 0, "no declared member classes");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        check(c.getDeclaredConstructors().length == 0, "no declared constructors");
    }

    static void testMetaAnnotations() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.WebServiceProvider");
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
        Class<?> c = Class.forName("javax.xml.ws.WebServiceProvider");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 4, "exactly 4 declared members, got " + ms.length);
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Method m : ms) {
            names.add(m.getName());
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                  "member " + m.getName() + " public abstract");
            check(m.getReturnType() == String.class, "member " + m.getName() + " returns String");
            check(m.getDefaultValue() != null, "member " + m.getName() + " carries default");
        }
        check(names.contains("wsdlLocation") && names.contains("serviceName")
              && names.contains("targetNamespace") && names.contains("portName"),
              "member name set = {wsdlLocation, serviceName, targetNamespace, portName}");
        check("".equals(c.getDeclaredMethod("wsdlLocation").getDefaultValue()), "wsdlLocation default empty");
        check("".equals(c.getDeclaredMethod("serviceName").getDefaultValue()), "serviceName default empty");
        check("".equals(c.getDeclaredMethod("targetNamespace").getDefaultValue()), "targetNamespace default empty");
        check("".equals(c.getDeclaredMethod("portName").getDefaultValue()), "portName default empty");
    }

    static void testUsageReadback() {
        WebServiceProvider wsp = FullProvider.class.getAnnotation(WebServiceProvider.class);
        check(wsp != null, "readback on Provider implementation non-null");
        check("psvc".equals(wsp.serviceName()), "serviceName readback");
        check("pport".equals(wsp.portName()), "portName readback");
        check("urn:p".equals(wsp.targetNamespace()), "targetNamespace readback");
        check("http://x/prov?wsdl".equals(wsp.wsdlLocation()), "wsdlLocation readback");
        check(wsp.annotationType() == WebServiceProvider.class, "annotationType identity");
        // 裸标注：四成员经默认值通道回读空串
        WebServiceProvider bare = BareProvider.class.getAnnotation(WebServiceProvider.class);
        check(bare != null, "bare WebServiceProvider present");
        check("".equals(bare.serviceName()), "bare serviceName empty");
        check("".equals(bare.portName()), "bare portName empty");
        check("".equals(bare.targetNamespace()), "bare targetNamespace empty");
        check("".equals(bare.wsdlLocation()), "bare wsdlLocation empty");
        // 通用契约
        check(bare.equals(BareProvider.class.getAnnotation(WebServiceProvider.class)), "bare instances value-equal");
        check(!bare.equals(wsp), "bare differs from fully-specified");
        check(bare.toString().startsWith("@javax.xml.ws.WebServiceProvider"), "toString prefix");
        // Provider 实现类接口契约
        check(Provider.class.isAssignableFrom(FullProvider.class), "carrier implements javax.xml.ws.Provider");
        check(!WebServiceProviderTest.class.isAnnotationPresent(WebServiceProvider.class),
              "holder (non-Provider) class unannotated");
    }

    // 运行时通道：Endpoint.create 接受 Provider 实现；无坏 WSDL 时 publish/stop 生命周期闭环
    static void testEndpointChannel() {
        Endpoint e = Endpoint.create(new FullProvider());
        check(e != null, "create Provider endpoint non-null");
        check(!e.isPublished(), "created Provider endpoint unpublished");
        check(e.getImplementor() instanceof FullProvider, "implementor is Provider instance");
        check(javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING.equals(e.getBinding().getBindingID()),
              "default SOAP 1.1 binding for Provider");
        // 裸标注 Provider：完整 publish/stop 闭环（localhost:0 ephemeral，立即 stop）
        Endpoint live = Endpoint.create(new BareProvider());
        live.publish("http://localhost:0/bare-prov");
        check(live.isPublished(), "bare Provider published");
        live.stop();
        check(!live.isPublished(), "bare Provider stopped");
        // MESSAGE 模式 Provider 同样可发布
        Endpoint msg = Endpoint.create(new MessageProvider());
        msg.publish("http://localhost:0/msg-prov");
        check(msg.isPublished(), "MESSAGE-mode Provider published");
        msg.stop();
        check(!msg.isPublished(), "MESSAGE-mode Provider stopped");
        // 坏 wsdlLocation：create 可过，publish 尝试加载 WSDL → WebServiceException
        try {
            Endpoint ghost = Endpoint.create(new GhostProvider());
            check(ghost != null, "ghost-WSDL provider created");
            ghost.publish("http://localhost:0/ghost-prov");
            check(false, "publish with ghost wsdlLocation must fail");
        } catch (WebServiceException ex) {
            check(true, "ghost wsdlLocation -> WebServiceException at publish");
        }
    }
}
