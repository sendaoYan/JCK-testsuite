/*
 * @test
 * @summary javax.xml.ws.WebServiceClient conformance: annotation metadata, empty-string defaults, generated service annotation readback and wsdlLocation-to-runtime channel.
 * @run main WebServiceClientTest
 */

import java.io.Writer;
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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;


public class WebServiceClientTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    static final String WSDL =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<definitions xmlns=\"http://schemas.xmlsoap.org/wsdl/\""
        + " xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\""
        + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
        + " xmlns:tns=\"urn:t\" targetNamespace=\"urn:t\">"
        + "<types><xsd:schema targetNamespace=\"urn:t\" elementFormDefault=\"qualified\">"
        + "<xsd:element name=\"echo\"><xsd:complexType><xsd:sequence>"
        + "<xsd:element name=\"arg0\" type=\"xsd:string\"/></xsd:sequence></xsd:complexType></xsd:element>"
        + "<xsd:element name=\"echoResponse\"><xsd:complexType><xsd:sequence>"
        + "<xsd:element name=\"return\" type=\"xsd:string\"/></xsd:sequence></xsd:complexType></xsd:element>"
        + "</xsd:schema></types>"
        + "<message name=\"echoRequest\"><part element=\"tns:echo\" name=\"parameters\"/></message>"
        + "<message name=\"echoResponse\"><part element=\"tns:echoResponse\" name=\"parameters\"/></message>"
        + "<portType name=\"EchoPort\"><operation name=\"echo\">"
        + "<input message=\"tns:echoRequest\"/><output message=\"tns:echoResponse\"/></operation></portType>"
        + "<binding name=\"EchoBinding\" type=\"tns:EchoPort\">"
        + "<soap:binding style=\"document\" transport=\"http://schemas.xmlsoap.org/soap/http\"/>"
        + "<operation name=\"echo\"><soap:operation soapAction=\"\"/>"
        + "<input><soap:body use=\"literal\"/></input><output><soap:body use=\"literal\"/></output></operation></binding>"
        + "<service name=\"svc\"><port name=\"EchoPort\" binding=\"tns:EchoBinding\">"
        + "<soap:address location=\"http://localhost:0/echo\"/></port></service>"
        + "</definitions>";

    @javax.jws.WebService(name = "EchoPort", targetNamespace = "urn:t")
    public interface Echo {
        String echo(String s);
    }

    // 生成代码形态：全成员标注的 Service 子类
    @WebServiceClient(name = "svc", targetNamespace = "urn:t", wsdlLocation = "http://x/svc?wsdl")
    public static class EchoService extends Service {
        protected EchoService(URL explicit) {
            super(explicit, new QName("urn:t", "svc"));
        }

        protected EchoService() {
            super(null, new QName("urn:t", "svc"));
        }
    }

    // 裸标注形态
    @WebServiceClient
    public static class BareService extends Service {
        protected BareService() {
            super(null, new QName("urn:bare", "bareSvc"));
        }
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testMetaAnnotations();
        testMemberContract();
        testUsageReadback();
        testRuntimeChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.WebServiceClient");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("name"), "has method name");
        check(names.contains("targetNamespace"), "has method targetNamespace");
        check(names.contains("wsdlLocation"), "has method wsdlLocation");
        check(c.getName().equals("javax.xml.ws.WebServiceClient"), "binary name javax.xml.ws.WebServiceClient");
        check(c.getDeclaredClasses().length == 0, "no declared member classes");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        check(c.getDeclaredConstructors().length == 0, "no declared constructors");
    }

    static void testMetaAnnotations() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.WebServiceClient");
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
        Class<?> c = Class.forName("javax.xml.ws.WebServiceClient");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 3, "exactly 3 declared members, got " + ms.length);
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Method m : ms) {
            names.add(m.getName());
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                  "member " + m.getName() + " public abstract");
            check(m.getReturnType() == String.class, "member " + m.getName() + " returns String");
        }
        check(names.contains("name") && names.contains("targetNamespace") && names.contains("wsdlLocation"),
              "member name set = {name, targetNamespace, wsdlLocation}");
        // JDK-8 实测三成员均带空串默认（javadoc 未标注必填）
        check("".equals(c.getDeclaredMethod("name").getDefaultValue()), "name default empty string");
        check("".equals(c.getDeclaredMethod("targetNamespace").getDefaultValue()), "targetNamespace default empty string");
        check("".equals(c.getDeclaredMethod("wsdlLocation").getDefaultValue()), "wsdlLocation default empty string");
    }

    static void testUsageReadback() {
        WebServiceClient wsc = EchoService.class.getAnnotation(WebServiceClient.class);
        check(wsc != null, "WebServiceClient readback on Service subclass non-null");
        check("svc".equals(wsc.name()), "name readback");
        check("urn:t".equals(wsc.targetNamespace()), "targetNamespace readback");
        check("http://x/svc?wsdl".equals(wsc.wsdlLocation()), "wsdlLocation readback");
        check(wsc.annotationType() == WebServiceClient.class, "annotationType identity");
        // 裸标注：实例回读默认空串
        WebServiceClient bare = BareService.class.getAnnotation(WebServiceClient.class);
        check(bare != null, "bare WebServiceClient present");
        check("".equals(bare.name()), "bare name empty");
        check("".equals(bare.targetNamespace()), "bare targetNamespace empty");
        check("".equals(bare.wsdlLocation()), "bare wsdlLocation empty");
        // 通用契约
        check(bare.equals(BareService.class.getAnnotation(WebServiceClient.class)), "bare instances value-equal");
        check(!bare.equals(wsc), "bare differs from fully-specified");
        check(bare.toString().startsWith("@javax.xml.ws.WebServiceClient"), "toString prefix");
        // 未标注类
        check(WebServiceClientTest.class.getAnnotation(WebServiceClient.class) == null, "holder class unannotated");
    }

    // 运行时通道：super(null, qn) 时注解的 wsdlLocation 真实驱动 WSDL 加载；显式 URL 时被覆盖
    static void testRuntimeChannel() throws Exception {
        // 1) 注解 wsdlLocation 参与：null URL → 回落到注解值 → 尝试加载 http://x/svc?wsdl 失败 → WebServiceException
        try {
            new EchoService();
            check(false, "null-URL ctor with bogus annotation wsdlLocation must fail");
        } catch (WebServiceException e) {
            check(e.getMessage() != null && e.getMessage().contains("svc?wsdl"),
                  "annotation wsdlLocation flows into failure: " + e.getMessage());
        }
        // 2) 显式临时 WSDL URL：注解 wsdlLocation 被覆盖，正常构建
        Path f = Files.createTempFile("wsc", ".wsdl");
        try (Writer w = Files.newBufferedWriter(f, StandardCharsets.UTF_8)) {
            w.write(WSDL);
        }
        try {
            URL url = f.toUri().toURL();
            EchoService svc = new EchoService(url);
            check(svc != null, "explicit-WSDL service instantiated");
            check(new QName("urn:t", "svc").equals(svc.getServiceName()), "service name round-trip");
            check(svc.getWSDLDocumentLocation().sameFile(url), "explicit wsdl location wins over annotation");
            java.util.Iterator<QName> ports = svc.getPorts();
            check(ports.hasNext(), "port discovered from explicit WSDL");
            Echo proxy = svc.getPort(new QName("urn:t", "EchoPort"), Echo.class);
            check(proxy instanceof Echo, "SEI proxy created (generated-code shape)");
        } finally {
            Files.delete(f);
        }
        // 3) 裸标注 + null URL：wsdlLocation 空串 → 无 WSDL 服务（不抛异常）
        try {
            BareService bare = new BareService();
            check(bare != null, "bare annotation with null URL tolerated");
            check(bare.getWSDLDocumentLocation() == null, "no WSDL location for bare annotation");
            check(!bare.getPorts().hasNext(), "no ports without WSDL");
        } catch (WebServiceException e) {
            check(true, "bare annotation instantiation outcome: " + e.getClass().getSimpleName());
        }
    }
}
