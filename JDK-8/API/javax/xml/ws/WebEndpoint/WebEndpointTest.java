/*
 * @test
 * @summary javax.xml.ws.WebEndpoint conformance: annotation metadata, name default, generated-service port method closure via temp WSDL.
 * @run main WebEndpointTest
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
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;


public class WebEndpointTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // 最小 document/literal wrapped WSDL：urn:t svc / EchoPort / echo
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

    // 生成代码形态：WebServiceClient 标注的 Service 子类 + WebEndpoint 标注的端口获取方法
    @WebServiceClient(name = "svc", targetNamespace = "urn:t", wsdlLocation = "http://x/svc?wsdl")
    public static class EchoService extends Service {
        protected EchoService(URL wsdlLocation) {
            super(wsdlLocation, new QName("urn:t", "svc"));
        }

        @WebEndpoint(name = "EchoPort")
        public Echo getEchoPort() {
            return super.getPort(new QName("urn:t", "EchoPort"), Echo.class);
        }

        @WebEndpoint
        public Echo getBarePort() {
            return super.getPort(new QName("urn:t", "EchoPort"), Echo.class);
        }

        public Echo getPlainPort() {
            return super.getPort(new QName("urn:t", "EchoPort"), Echo.class);
        }
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testMetaAnnotations();
        testMemberContract();
        testUsageReadback();
        testGeneratedServiceClosure();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.WebEndpoint");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("name"), "has method name");
        check(c.getName().equals("javax.xml.ws.WebEndpoint"), "binary name javax.xml.ws.WebEndpoint");
        check(c.getDeclaredClasses().length == 0, "no declared member classes");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        check(c.getDeclaredConstructors().length == 0, "no declared constructors");
    }

    static void testMetaAnnotations() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.WebEndpoint");
        Retention ret = c.getAnnotation(Retention.class);
        check(ret != null && ret.value() == RetentionPolicy.RUNTIME, "retention is RUNTIME");
        Target tgt = c.getAnnotation(Target.class);
        check(tgt != null, "Target meta-annotation present");
        java.util.Set<ElementType> ts = new java.util.HashSet<ElementType>();
        for (ElementType e : tgt.value()) ts.add(e);
        check(ts.size() == 1 && ts.contains(ElementType.METHOD), "Target is METHOD only");
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
        Class<?> c = Class.forName("javax.xml.ws.WebEndpoint");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 1, "exactly 1 declared member, got " + ms.length);
        Method n = c.getDeclaredMethod("name");
        check(Modifier.isPublic(n.getModifiers()) && Modifier.isAbstract(n.getModifiers()), "name public abstract");
        check(n.getReturnType() == String.class, "name returns String");
        Object dv = n.getDefaultValue();
        check(dv != null, "name carries a default");
        check("".equals(dv), "name default empty string");
    }

    static void testUsageReadback() throws Exception {
        Method port = EchoService.class.getMethod("getEchoPort");
        WebEndpoint we = port.getAnnotation(WebEndpoint.class);
        check(we != null, "WebEndpoint readback on port method non-null");
        check("EchoPort".equals(we.name()), "name readback EchoPort");
        check(we.annotationType() == WebEndpoint.class, "annotationType identity");
        // 裸标注：name 默认空串
        Method bare = EchoService.class.getMethod("getBarePort");
        WebEndpoint bw = bare.getAnnotation(WebEndpoint.class);
        check(bw != null, "bare WebEndpoint present");
        check("".equals(bw.name()), "bare name defaults to empty string");
        // 未标注方法 / 非法位置
        Method plain = EchoService.class.getMethod("getPlainPort");
        check(plain.getAnnotation(WebEndpoint.class) == null, "no annotation on unannotated method");
        check(EchoService.class.getAnnotation(WebEndpoint.class) == null, "not applicable to class");
        check(port.getDeclaredAnnotations().length == 1, "port method carries only WebEndpoint");
        // 通用契约
        check(bw.equals(EchoService.class.getMethod("getBarePort").getAnnotation(WebEndpoint.class)),
              "bare instances value-equal");
        check(!bw.equals(we), "bare differs from named instance");
        check(bw.toString().startsWith("@javax.xml.ws.WebEndpoint"), "toString prefix");
    }

    // 生成服务闭环：临时 WSDL → Service 子类实例 → 调用 WebEndpoint 标注方法取得 SEI 代理
    static void testGeneratedServiceClosure() throws Exception {
        Path f = Files.createTempFile("we-endpoint", ".wsdl");
        try (Writer w = Files.newBufferedWriter(f, StandardCharsets.UTF_8)) {
            w.write(WSDL);
        }
        try {
            URL url = f.toUri().toURL();
            EchoService svc = new EchoService(url);
            check(svc != null, "generated-shape service instantiated");
            check(new QName("urn:t", "svc").equals(svc.getServiceName()), "service name from explicit WSDL+QName");
            check(svc.getWSDLDocumentLocation().sameFile(url), "wsdl location echoed back");
            java.util.Iterator<QName> ports = svc.getPorts();
            boolean found = false;
            while (ports.hasNext()) {
                if (new QName("urn:t", "EchoPort").equals(ports.next())) found = true;
            }
            check(found, "port discovered from WSDL");
            // 调用 WebEndpoint 标注的端口方法（不发起网络调用，仅生成代理）
            Echo proxy = svc.getEchoPort();
            check(proxy != null, "port method returns non-null proxy");
            check(proxy instanceof Echo, "proxy implements SEI");
            check(proxy instanceof javax.xml.ws.BindingProvider, "proxy is a BindingProvider");
        } finally {
            Files.delete(f);
        }
    }
}
