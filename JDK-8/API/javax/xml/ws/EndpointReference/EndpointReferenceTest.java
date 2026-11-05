/*
 * @test
 * @summary javax.xml.ws.EndpointReference conformance: abstract EPR structure, static readFrom factory, W3C subclass relation, writeTo closure.
 * @run main EndpointReferenceTest
 */

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.EndpointReference;

public class EndpointReferenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    static final String EPR_XML =
        "<EndpointReference xmlns=\"http://www.w3.org/2005/08/addressing\">"
        + "<Address>http://a</Address></EndpointReference>";

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.EndpointReference");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("readFrom"), "has method readFrom");
        check(names.contains("writeTo"), "has method writeTo");
        check(names.contains("getPort"), "has method getPort");
        check(names.contains("toString"), "has method toString");
        testStructure(c);
        testReadFrom();
        testW3CSubclass();
        testWriteTo();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure(Class<?> c) throws Exception {
        // readFrom(Source) 为 public static 工厂
        Method rf = c.getMethod("readFrom", Source.class);
        check(Modifier.isStatic(rf.getModifiers()), "readFrom is static");
        check(Modifier.isPublic(rf.getModifiers()), "readFrom is public");
        check(rf.getReturnType() == EndpointReference.class, "readFrom returns EndpointReference");
        // writeTo(Result) 抽象
        Method wt = c.getMethod("writeTo", Result.class);
        check(Modifier.isAbstract(wt.getModifiers()), "writeTo abstract");
        check(wt.getReturnType() == void.class, "writeTo void");
        // getPort 泛型方法（T getPort(Class<T>, WebServiceFeature...)）
        Method gp = c.getMethod("getPort", Class.class, javax.xml.ws.WebServiceFeature[].class);
        check(gp.getReturnType() == Object.class, "getPort returns T (erasure Object)");
        check(gp.getTypeParameters().length == 1, "getPort generic <T>");
        check(Modifier.isPublic(gp.getModifiers()), "getPort public");
        // toString 具体覆写
        Method ts = c.getMethod("toString");
        check(!Modifier.isAbstract(ts.getModifiers()), "toString concrete");
        // 构造器 protected（抽象基类不能直接 new）
        check(c.getConstructors().length == 0, "no public constructors");
        try {
            c.newInstance();
            check(false, "direct instantiation must fail");
        } catch (IllegalAccessException | InstantiationException e) {
            check(true, "abstract class not instantiable: " + e.getClass().getSimpleName());
        }
    }

    // readFrom 真实闭环：合法 EPR XML → W3CEndpointReference
    static void testReadFrom() throws Exception {
        EndpointReference epr = EndpointReference.readFrom(new StreamSource(new StringReader(EPR_XML)));
        check(epr != null, "readFrom non-null");
        check(epr instanceof javax.xml.ws.wsaddressing.W3CEndpointReference,
              "runtime type is W3CEndpointReference");
        check(epr.toString().contains("http://a"), "toString contains Address content");
        check(epr.toString().contains("EndpointReference"), "toString contains root element");
        // 第二次 readFrom 独立实例
        EndpointReference epr2 = EndpointReference.readFrom(new StreamSource(new StringReader(EPR_XML)));
        check(epr2 != epr, "readFrom yields new instance each call");
        check(epr2.toString().contains("http://a"), "second instance same content");
        // null Source 负例
        try {
            EndpointReference.readFrom(null);
            check(false, "readFrom(null) must throw");
        } catch (RuntimeException e) {
            check(true, "readFrom(null) rejected: " + e.getClass().getSimpleName());
        }
        // 坏 XML 负例（RI -> WebServiceException）
        try {
            EndpointReference.readFrom(new StreamSource(new StringReader("not-xml")));
            check(false, "readFrom(bad xml) must throw");
        } catch (javax.xml.ws.WebServiceException e) {
            check(e.getMessage() == null || !e.getMessage().isEmpty(), "bad xml message shape");
        }
        // 非法根元素（非 EndpointReference）负例
        try {
            EndpointReference.readFrom(new StreamSource(new StringReader("<wrong/>")));
            check(false, "readFrom(wrong root) must throw");
        } catch (javax.xml.ws.WebServiceException e) {
            check(true, "wrong root -> WebServiceException");
        }
    }

    static void testW3CSubclass() throws Exception {
        Class<?> w3c = Class.forName("javax.xml.ws.wsaddressing.W3CEndpointReference");
        check(w3c.getSuperclass() == EndpointReference.class, "W3CEndpointReference extends EndpointReference");
        check(EndpointReference.class.isAssignableFrom(w3c), "assignable");
        check(!Modifier.isAbstract(w3c.getModifiers()), "W3C concrete");
        // W3C 构造 (Source)
        check(w3c.getConstructor(Source.class) != null, "W3C public ctor (Source)");
        // toString 继承可用（写 XML）
        javax.xml.ws.wsaddressing.W3CEndpointReference w =
            new javax.xml.ws.wsaddressing.W3CEndpointReference(new StreamSource(new StringReader(EPR_XML)));
        check(w.toString().contains("http://a"), "W3C toString via base contract");
    }

    // writeTo：EPR 写到 Result（StreamResult 捕获 XML）
    static void testWriteTo() throws Exception {
        EndpointReference epr = EndpointReference.readFrom(new StreamSource(new StringReader(EPR_XML)));
        StringWriter sw = new StringWriter();
        Result res = new StreamResult(sw);
        epr.writeTo(res);
        String out = sw.toString();
        check(out.contains("EndpointReference"), "writeTo root element");
        check(out.contains("http://a"), "writeTo address content");
        check(out.contains("http://www.w3.org/2005/08/addressing"), "writeTo addressing namespace");
        // round-trip：写出→再 readFrom
        EndpointReference again = EndpointReference.readFrom(new StreamSource(new StringReader(out)));
        check(again != null, "writeTo output re-readable by readFrom (round-trip)");
        check(again.toString().contains("http://a"), "round-tripped content kept");
        // writeTo(null) 负例
        try {
            epr.writeTo(null);
            check(false, "writeTo(null) must throw");
        } catch (RuntimeException e) {
            check(true, "writeTo(null) rejected: " + e.getClass().getSimpleName());
        }
    }
}
