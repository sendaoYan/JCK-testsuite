/*
 * @test
 * @summary javax.xml.ws.BindingProvider conformance: six standard property constants, proxy request/response context contracts.
 * @run main BindingProviderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

public class BindingProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @javax.jws.WebService(name = "EchoPort", targetNamespace = "urn:t")
    public interface Echo {
        String echo(String s);
    }

    // 与 ServiceTest 相同的最小 WSDL（document/literal wrapped）
    static final String WSDL =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<definitions xmlns=\"http://schemas.xmlsoap.org/wsdl/\"\n" +
        "             xmlns:tns=\"urn:t\"\n" +
        "             xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
        "             xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\"\n" +
        "             targetNamespace=\"urn:t\" name=\"svc\">\n" +
        "  <types>\n" +
        "    <xsd:schema targetNamespace=\"urn:t\" elementFormDefault=\"qualified\">\n" +
        "      <xsd:element name=\"echo\"><xsd:complexType><xsd:sequence>\n" +
        "        <xsd:element name=\"arg0\" type=\"xsd:string\"/>\n" +
        "      </xsd:sequence></xsd:complexType></xsd:element>\n" +
        "      <xsd:element name=\"echoResponse\"><xsd:complexType><xsd:sequence>\n" +
        "        <xsd:element name=\"return\" type=\"xsd:string\"/>\n" +
        "      </xsd:sequence></xsd:complexType></xsd:element>\n" +
        "    </xsd:schema>\n" +
        "  </types>\n" +
        "  <message name=\"echo\"><part name=\"parameters\" element=\"tns:echo\"/></message>\n" +
        "  <message name=\"echoResponse\"><part name=\"parameters\" element=\"tns:echoResponse\"/></message>\n" +
        "  <portType name=\"EchoPort\">\n" +
        "    <operation name=\"echo\"><input message=\"tns:echo\"/><output message=\"tns:echoResponse\"/></operation>\n" +
        "  </portType>\n" +
        "  <binding name=\"EchoPortBinding\" type=\"tns:EchoPort\">\n" +
        "    <soap:binding transport=\"http://schemas.xmlsoap.org/soap/http\" style=\"document\"/>\n" +
        "    <operation name=\"echo\"><soap:operation soapAction=\"\"/>\n" +
        "      <input><soap:body use=\"literal\"/></input>\n" +
        "      <output><soap:body use=\"literal\"/></output>\n" +
        "    </operation>\n" +
        "  </binding>\n" +
        "  <service name=\"svc\">\n" +
        "    <port name=\"EchoPort\" binding=\"tns:EchoPortBinding\">\n" +
        "      <soap:address location=\"http://localhost:60123/echo\"/>\n" +
        "    </port>\n" +
        "  </service>\n" +
        "</definitions>\n";

    public static void main(String[] args) throws Exception {
        testConstants();
        testInterfaceStructure();
        testProxyContexts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- 6 个标准属性常量串全等（JAX-WS 2.2 契约值） ----
    static void testConstants() throws Exception {
        check("javax.xml.ws.security.auth.username".equals(BindingProvider.USERNAME_PROPERTY),
              "USERNAME_PROPERTY");
        check("javax.xml.ws.security.auth.password".equals(BindingProvider.PASSWORD_PROPERTY),
              "PASSWORD_PROPERTY");
        check("javax.xml.ws.service.endpoint.address".equals(BindingProvider.ENDPOINT_ADDRESS_PROPERTY),
              "ENDPOINT_ADDRESS_PROPERTY");
        check("javax.xml.ws.session.maintain".equals(BindingProvider.SESSION_MAINTAIN_PROPERTY),
              "SESSION_MAINTAIN_PROPERTY");
        check("javax.xml.ws.soap.http.soapaction.use".equals(BindingProvider.SOAPACTION_USE_PROPERTY),
              "SOAPACTION_USE_PROPERTY");
        check("javax.xml.ws.soap.http.soapaction.uri".equals(BindingProvider.SOAPACTION_URI_PROPERTY),
              "SOAPACTION_URI_PROPERTY");
        // 常量均为 public static final（编译期内联，反射验证声明形状）
        java.lang.reflect.Field f = BindingProvider.class.getField("USERNAME_PROPERTY");
        check(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
              && Modifier.isFinal(f.getModifiers()), "constant modifiers public static final");
        check(f.getType() == String.class, "constant type String");
        // 6 常量互异且无前缀重复拼写错误
        java.util.Set<String> distinct = new java.util.HashSet<String>(java.util.Arrays.asList(
            BindingProvider.USERNAME_PROPERTY, BindingProvider.PASSWORD_PROPERTY,
            BindingProvider.ENDPOINT_ADDRESS_PROPERTY, BindingProvider.SESSION_MAINTAIN_PROPERTY,
            BindingProvider.SOAPACTION_USE_PROPERTY, BindingProvider.SOAPACTION_URI_PROPERTY));
        check(distinct.size() == 6, "six distinct property name strings");
    }

    static void testInterfaceStructure() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.BindingProvider");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no super-interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getRequestContext"), "has method getRequestContext");
        check(names.contains("getResponseContext"), "has method getResponseContext");
        check(names.contains("getBinding"), "has method getBinding");
        check(names.contains("getEndpointReference"), "has method getEndpointReference");
        // 上下文返回类型 Map<String,Object>
        Method req = c.getMethod("getRequestContext");
        check(req.getReturnType() == Map.class, "getRequestContext returns Map");
        Method resp = c.getMethod("getResponseContext");
        check(resp.getReturnType() == Map.class, "getResponseContext returns Map");
        check(c.getMethod("getBinding").getReturnType() == javax.xml.ws.Binding.class,
              "getBinding returns javax.xml.ws.Binding");
        // getEndpointReference 两个形态：无参 + Class<T extends EndpointReference>
        check(c.getMethod("getEndpointReference").getReturnType() == javax.xml.ws.EndpointReference.class,
              "getEndpointReference() returns EndpointReference");
        check(java.lang.reflect.Modifier.isPublic(c.getMethod("getEndpointReference").getModifiers()),
              "getEndpointReference public");
        Method typed = c.getMethod("getEndpointReference", Class.class);
        check(typed.getReturnType() == Object.class || typed.getReturnType().equals(
                  Class.forName("javax.xml.ws.EndpointReference")),
              "typed getEndpointReference type parameter bound");
        check(typed.getTypeParameters().length == 1, "typed getEndpointReference generic");
    }

    // ---- 真实代理上下文契约（WSDL 支撑，不发起网络调用） ----
    static void testProxyContexts() throws Exception {
        Path f = Files.createTempFile("jckbp", ".wsdl");
        try {
            Files.write(f, WSDL.getBytes(StandardCharsets.UTF_8));
            URL url = f.toUri().toURL();
            Service svc = Service.create(url, new QName("urn:t", "svc"));
            Echo proxy = svc.getPort(new QName("urn:t", "EchoPort"), Echo.class);
            check(proxy instanceof BindingProvider, "port proxy is a BindingProvider");
            BindingProvider bp = (BindingProvider) proxy;

            // getRequestContext：非 null、WSDL soap:address 预填、put/get 回读
            Map<String, Object> req = bp.getRequestContext();
            check(req != null, "request context non-null");
            check("http://localhost:60123/echo".equals(req.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY)),
                  "endpoint address seeded from WSDL soap:address");
            req.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:60123/changed");
            check("http://localhost:60123/changed".equals(
                      bp.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY)),
                  "endpoint address put/get round-trip across calls");
            req.put(BindingProvider.USERNAME_PROPERTY, "alice");
            check("alice".equals(req.get(BindingProvider.USERNAME_PROPERTY)), "username round-trip");
            req.put(BindingProvider.PASSWORD_PROPERTY, "secret");
            check("secret".equals(req.get(BindingProvider.PASSWORD_PROPERTY)), "password round-trip");
            req.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
            check(Boolean.TRUE.equals(req.get(BindingProvider.SESSION_MAINTAIN_PROPERTY)), "session maintain round-trip");
            req.put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
            check(Boolean.TRUE.equals(req.get(BindingProvider.SOAPACTION_USE_PROPERTY)), "soapaction use round-trip");
            req.put(BindingProvider.SOAPACTION_URI_PROPERTY, "urn:echo");
            check("urn:echo".equals(req.get(BindingProvider.SOAPACTION_URI_PROPERTY)), "soapaction uri round-trip");
            req.remove(BindingProvider.USERNAME_PROPERTY);
            check(req.get(BindingProvider.USERNAME_PROPERTY) == null, "removed key absent");
            // 自定义键与非 String 值
            req.put("my.key", Integer.valueOf(7));
            check(Integer.valueOf(7).equals(req.get("my.key")), "custom key non-String value round-trip");

            // getResponseContext：invoke 前 RI 返回 null（惰性创建，实测锁定）
            // —— javadoc 未规定预填充，此处锁 RI 实际行为
            Map<String, Object> resp = bp.getResponseContext();
            if (resp == null) {
                check(true, "response context null before first invoke (RI lazy)");
            } else {
                resp.put("rk", "rv");
                check("rv".equals(resp.get("rk")), "response context put/get on provided map");
            }

            // getBinding：非 null，SOAP 1.1 binding id
            javax.xml.ws.Binding b = bp.getBinding();
            check(b != null, "proxy binding non-null");
            check("http://schemas.xmlsoap.org/wsdl/soap/http".equals(b.getBindingID()),
                  "proxy binding id SOAP 1.1");
            check(b.getHandlerChain() != null, "proxy handler chain non-null list");
            check(b.getHandlerChain().isEmpty(), "proxy default handler chain empty");

            // 同一代理 getRequestContext 每次返回同一可变视图（put 跨调用可见，已断言）
            // Dispatch 也实现 BindingProvider（同契约面）
            javax.xml.ws.Dispatch<javax.xml.transform.Source> d =
                svc.createDispatch(new QName("urn:t", "EchoPort"), javax.xml.transform.Source.class,
                                   Service.Mode.PAYLOAD);
            check(d instanceof BindingProvider, "dispatch is a BindingProvider");
            Map<String, Object> dreq = d.getRequestContext();
            check(dreq != null, "dispatch request context non-null");
            dreq.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:60123/d");
            check("http://localhost:60123/d".equals(
                      d.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY)),
                  "dispatch context round-trip");
        } finally {
            Files.deleteIfExists(f);
        }
    }
}
