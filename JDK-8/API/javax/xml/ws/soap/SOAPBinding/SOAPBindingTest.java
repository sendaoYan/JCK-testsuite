/*
 * @test
 * @summary javax.xml.ws.soap.SOAPBinding conformance: four binding URI constants exact, Binding inheritance and method signatures, live roles/MTOM/factories behavior on a real endpoint binding.
 * @run main SOAPBindingTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;
import javax.xml.ws.soap.SOAPBinding;

public class SOAPBindingTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @javax.jws.WebService(serviceName = "s", targetNamespace = "urn:t")
    public static class Impl {
        public String m() { return "x"; }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.soap.SOAPBinding");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.ws.Binding"), "implements javax.xml.ws.Binding");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getRoles"), "has method getRoles");
        check(names.contains("setRoles"), "has method setRoles");
        check(names.contains("isMTOMEnabled"), "has method isMTOMEnabled");
        check(names.contains("setMTOMEnabled"), "has method setMTOMEnabled");
        check(names.contains("getSOAPFactory"), "has method getSOAPFactory");
        check(names.contains("getMessageFactory"), "has method getMessageFactory");
        testConstants(c);
        testInterfaceFace(c);
        testLiveBinding();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 常量表：4 个绑定 URI 全等（含 1.2 的尾斜杠与 MTOM 后缀）
    static void testConstants(Class<?> c) throws Exception {
        check(SOAPBinding.SOAP11HTTP_BINDING.equals("http://schemas.xmlsoap.org/wsdl/soap/http"),
              "SOAP11HTTP_BINDING: " + SOAPBinding.SOAP11HTTP_BINDING);
        check(SOAPBinding.SOAP12HTTP_BINDING.equals("http://www.w3.org/2003/05/soap/bindings/HTTP/"),
              "SOAP12HTTP_BINDING (trailing slash): " + SOAPBinding.SOAP12HTTP_BINDING);
        check(SOAPBinding.SOAP11HTTP_MTOM_BINDING.equals("http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true"),
              "SOAP11HTTP_MTOM_BINDING: " + SOAPBinding.SOAP11HTTP_MTOM_BINDING);
        check(SOAPBinding.SOAP12HTTP_MTOM_BINDING.equals("http://www.w3.org/2003/05/soap/bindings/HTTP/?mtom=true"),
              "SOAP12HTTP_MTOM_BINDING: " + SOAPBinding.SOAP12HTTP_MTOM_BINDING);
        // 四者互异
        Set<String> distinct = new HashSet<String>();
        distinct.add(SOAPBinding.SOAP11HTTP_BINDING);
        distinct.add(SOAPBinding.SOAP12HTTP_BINDING);
        distinct.add(SOAPBinding.SOAP11HTTP_MTOM_BINDING);
        distinct.add(SOAPBinding.SOAP12HTTP_MTOM_BINDING);
        check(distinct.size() == 4, "4 binding ids pairwise distinct");
        // MTOM 变体 = 基础 + ?mtom=true
        check(SOAPBinding.SOAP11HTTP_MTOM_BINDING.equals(SOAPBinding.SOAP11HTTP_BINDING + "?mtom=true"),
              "1.1 MTOM variant is base + ?mtom=true");
        check(SOAPBinding.SOAP12HTTP_MTOM_BINDING.equals(SOAPBinding.SOAP12HTTP_BINDING + "?mtom=true"),
              "1.2 MTOM variant is base + ?mtom=true");
        // 与 HTTPBinding 区分
        check(!SOAPBinding.SOAP11HTTP_BINDING.equals(javax.xml.ws.http.HTTPBinding.HTTP_BINDING),
              "distinct from HTTP binding id");
        // 字段修饰符
        for (java.lang.reflect.Field f : c.getFields()) {
            check(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
                  && Modifier.isFinal(f.getModifiers()), f.getName() + " public static final");
        }
        check(c.getDeclaredFields().length == 4, "exactly 4 declared fields");
    }

    // 接口面：6 个自有方法精确签名 + Binding 继承
    static void testInterfaceFace(Class<?> c) throws Exception {
        Method[] dm = c.getDeclaredMethods();
        check(dm.length == 6, "exactly 6 declared methods: " + dm.length);
        for (Method m : dm) {
            check(Modifier.isAbstract(m.getModifiers()), m.getName() + " abstract");
            check(Modifier.isPublic(m.getModifiers()), m.getName() + " public");
        }
        Method gr = c.getMethod("getRoles");
        check(gr.getReturnType() == java.util.Set.class, "getRoles returns Set");
        Method sr = c.getMethod("setRoles", java.util.Set.class);
        check(sr.getReturnType() == void.class, "setRoles void, takes Set");
        Method im = c.getMethod("isMTOMEnabled");
        check(im.getReturnType() == boolean.class, "isMTOMEnabled returns boolean");
        Method sm = c.getMethod("setMTOMEnabled", boolean.class);
        check(sm.getReturnType() == void.class, "setMTOMEnabled void");
        Method sf = c.getMethod("getSOAPFactory");
        check(sf.getReturnType() == javax.xml.soap.SOAPFactory.class, "getSOAPFactory returns SOAPFactory");
        Method mf = c.getMethod("getMessageFactory");
        check(mf.getReturnType() == javax.xml.soap.MessageFactory.class, "getMessageFactory returns MessageFactory");
        // 继承 Binding 三方法
        check(Binding.class.getMethod("getBindingID").getReturnType() == String.class,
              "inherited getBindingID");
        check(Binding.class.getMethod("getHandlerChain").getReturnType() == java.util.List.class,
              "inherited getHandlerChain");
        check(Binding.class.getMethod("setHandlerChain", java.util.List.class).getReturnType() == void.class,
              "inherited setHandlerChain");
    }

    // 真实通道：Endpoint.create 的绑定是 SOAPBinding（1.1）
    static void testLiveBinding() throws Exception {
        Endpoint ep = Endpoint.create(SOAPBinding.SOAP11HTTP_BINDING, new Impl());
        try {
            Binding b = ep.getBinding();
            check(b instanceof SOAPBinding, "endpoint binding is SOAPBinding: " + b.getClass().getName());
            SOAPBinding sb = (SOAPBinding) b;
            check(!sb.isMTOMEnabled(), "MTOM disabled by default");
            sb.setMTOMEnabled(true);
            check(sb.isMTOMEnabled(), "setMTOMEnabled(true) round-trip");
            sb.setMTOMEnabled(false);
            check(!sb.isMTOMEnabled(), "setMTOMEnabled(false) round-trip");
            check(sb.getSOAPFactory() != null, "SOAPFactory available");
            check(sb.getMessageFactory() != null, "MessageFactory available");
            // 默认角色含 SOAP 1.1 next actor
            Set<String> roles = sb.getRoles();
            check(roles != null, "roles non-null");
            check(roles.contains("http://schemas.xmlsoap.org/soap/actor/next"),
                  "default roles include SOAP 1.1 next actor: " + roles);
            // 自定义角色（合法 URI）回读
            Set<String> custom = new HashSet<String>();
            custom.add("http://www.w3.org/2003/05/soap-envelope/role/next");
            custom.add("urn:my:role");
            sb.setRoles(custom);
            Set<String> now = sb.getRoles();
            check(now.contains("urn:my:role"), "custom role round-trips");
            check(now.contains("http://www.w3.org/2003/05/soap-envelope/role/next"),
                  "1.2 next role round-trips");
            // 空集：RI 规格化为仅 next actor
            sb.setRoles(new HashSet<String>());
            check(sb.getRoles().contains("http://schemas.xmlsoap.org/soap/actor/next"),
                  "empty roles normalized to include next actor: " + sb.getRoles());
            // setRoles(null) 容忍不抛（实测记录）
            boolean threw = false;
            try {
                sb.setRoles(null);
            } catch (Exception e) {
                threw = true;
            }
            check(!threw, "setRoles(null) tolerated by this RI (recorded)");
            // 非法角色串 -> UnsupportedOperationException（实测记录）
            try {
                sb.setRoles(java.util.Collections.singleton("bogus-role"));
                check(false, "non-URI role must throw");
            } catch (UnsupportedOperationException e) {
                check(true, "non-URI role -> UnsupportedOperationException");
            }
            check(b.getHandlerChain() != null, "handler chain list available");
        } finally {
            ep.stop();
        }
    }
}
