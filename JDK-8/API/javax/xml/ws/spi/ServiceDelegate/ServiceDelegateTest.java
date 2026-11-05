/*
 * @test
 * @summary javax.xml.ws.spi.ServiceDelegate conformance: fully abstract face with exact overload counts (getPort x5, createDispatch x6), protected constructor, live delegate behaviors via Provider lookup.
 * @run main ServiceDelegateTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.spi.Provider;
import javax.xml.ws.spi.ServiceDelegate;

public class ServiceDelegateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.spi.ServiceDelegate");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getPort"), "has method getPort");
        check(names.contains("addPort"), "has method addPort");
        check(names.contains("createDispatch"), "has method createDispatch");
        check(names.contains("getServiceName"), "has method getServiceName");
        check(names.contains("getPorts"), "has method getPorts");
        check(names.contains("getWSDLDocumentLocation"), "has method getWSDLDocumentLocation");
        check(names.contains("getHandlerResolver"), "has method getHandlerResolver");
        check(names.contains("setHandlerResolver"), "has method setHandlerResolver");
        check(names.contains("getExecutor"), "has method getExecutor");
        check(names.contains("setExecutor"), "has method setExecutor");
        testClassFace(c);
        testMethodSignatures(c);
        testLiveDelegate();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 类面：protected 无参构造器，19 个声明方法全部 public abstract
    static void testClassFace(Class<?> c) throws Exception {
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "exactly 1 declared ctor");
        check(Modifier.isProtected(ctors[0].getModifiers()), "ctor protected");
        check(ctors[0].getParameterTypes().length == 0, "ctor no-arg");
        Method[] dm = c.getDeclaredMethods();
        check(dm.length == 19, "exactly 19 declared methods: " + dm.length);
        for (Method m : dm) {
            check(Modifier.isAbstract(m.getModifiers()), m.getName() + " abstract");
            check(Modifier.isPublic(m.getModifiers()), m.getName() + " public");
        }
        // 无静态/具体方法（纯抽象契约）
        int concrete = 0;
        for (Method m : c.getDeclaredMethods()) {
            if (!Modifier.isAbstract(m.getModifiers())) concrete++;
        }
        check(concrete == 0, "no concrete methods");
        check(c.getFields().length == 0, "no public fields");
    }

    // 方法签名与重载数
    static void testMethodSignatures(Class<?> c) throws Exception {
        Map<String, Integer> counts = new HashMap<String, Integer>();
        for (Method m : c.getDeclaredMethods()) {
            Integer old = counts.get(m.getName());
            counts.put(m.getName(), old == null ? 1 : old + 1);
        }
        check(counts.get("getPort") == 5, "getPort x5: " + counts.get("getPort"));
        check(counts.get("createDispatch") == 6, "createDispatch x6: " + counts.get("createDispatch"));
        check(counts.get("addPort") == 1, "addPort x1");
        check(counts.get("getServiceName") == 1, "getServiceName x1");
        check(counts.get("getPorts") == 1, "getPorts x1");
        check(counts.get("getWSDLDocumentLocation") == 1, "getWSDLDocumentLocation x1");
        check(counts.get("getHandlerResolver") == 1, "getHandlerResolver x1");
        check(counts.get("setHandlerResolver") == 1, "setHandlerResolver x1");
        check(counts.get("getExecutor") == 1, "getExecutor x1");
        check(counts.get("setExecutor") == 1, "setExecutor x1");
        // 精确签名样例
        check(c.getMethod("getServiceName").getReturnType() == QName.class,
              "getServiceName returns QName");
        check(c.getMethod("getPorts").getReturnType() == java.util.Iterator.class,
              "getPorts returns Iterator<QName>");
        check(c.getMethod("getWSDLDocumentLocation").getReturnType() == java.net.URL.class,
              "getWSDLDocumentLocation returns URL");
        check(c.getMethod("addPort", QName.class, String.class, String.class).getReturnType() == void.class,
              "addPort(QName,String,String) void");
        check(c.getMethod("getExecutor").getReturnType() == java.util.concurrent.Executor.class,
              "getExecutor returns Executor");
        check(c.getMethod("setExecutor", java.util.concurrent.Executor.class).getReturnType() == void.class,
              "setExecutor void");
        check(c.getMethod("setHandlerResolver", javax.xml.ws.handler.HandlerResolver.class).getReturnType() == void.class,
              "setHandlerResolver void");
        check(c.getMethod("getHandlerResolver").getReturnType() == javax.xml.ws.handler.HandlerResolver.class,
              "getHandlerResolver returns HandlerResolver");
        // Dispatch 形态：Class 族三形态（QName±features、EPR）
        check(c.getMethod("createDispatch", QName.class, Class.class, Mode.class) != null
              && c.getMethod("createDispatch", QName.class, Class.class, Mode.class,
                    javax.xml.ws.WebServiceFeature[].class) != null
              && c.getMethod("createDispatch", EndpointReference.class, Class.class, Mode.class,
                    javax.xml.ws.WebServiceFeature[].class) != null,
              "createDispatch Class-family: (QName,Class,Mode), (+features), (EPR,...)");
        // Dispatch 形态：JAXBContext 族三形态
        check(c.getMethod("createDispatch", QName.class, javax.xml.bind.JAXBContext.class, Mode.class) != null
              && c.getMethod("createDispatch", QName.class, javax.xml.bind.JAXBContext.class,
                    Mode.class, javax.xml.ws.WebServiceFeature[].class) != null
              && c.getMethod("createDispatch", EndpointReference.class,
                    javax.xml.bind.JAXBContext.class, Mode.class,
                    javax.xml.ws.WebServiceFeature[].class) != null,
              "createDispatch JAXB-family: (QName,JAXB,Mode), (+features), (EPR,...)");
        // getPort 五形态
        check(c.getMethod("getPort", QName.class, Class.class) != null
              && c.getMethod("getPort", QName.class, Class.class,
                    javax.xml.ws.WebServiceFeature[].class) != null
              && c.getMethod("getPort", EndpointReference.class, Class.class,
                    javax.xml.ws.WebServiceFeature[].class) != null
              && c.getMethod("getPort", Class.class) != null
              && c.getMethod("getPort", Class.class, javax.xml.ws.WebServiceFeature[].class) != null,
              "getPort forms: (QName,Class), (+features), (EPR,Class,features), (Class), (Class,features)");
    }

    // 真实 delegate：Provider.createServiceDelegate 行为面
    static void testLiveDelegate() {
        Provider p = Provider.provider();
        QName svcName = new QName("urn:t", "svc");
        ServiceDelegate d = p.createServiceDelegate(null, svcName, Service.class);
        check(d != null, "live delegate non-null");
        check(svcName.equals(d.getServiceName()), "service name round-trip");
        check(d.getPorts() != null, "ports iterator non-null");
        check(!d.getPorts().hasNext(), "no ports without WSDL");
        check(d.getWSDLDocumentLocation() == null, "wsdl location null without WSDL");
        check(d.getHandlerResolver() == null, "default resolver null");
        // resolver 身份回读
        javax.xml.ws.handler.HandlerResolver r = new javax.xml.ws.handler.HandlerResolver() {
            public java.util.List<javax.xml.ws.handler.Handler> getHandlerChain(
                    javax.xml.ws.handler.PortInfo pi) {
                return new java.util.ArrayList<javax.xml.ws.handler.Handler>();
            }
        };
        d.setHandlerResolver(r);
        check(d.getHandlerResolver() == r, "resolver identity");
        // executor 回读与清空
        java.util.concurrent.Executor ex = java.util.concurrent.Executors.newSingleThreadExecutor();
        try {
            d.setExecutor(ex);
            check(d.getExecutor() == ex, "executor identity");
            d.setExecutor(null);
            check(d.getExecutor() == null, "setExecutor(null) -> null (tolerated)");
        } finally {
            ((java.util.concurrent.ExecutorService) ex).shutdown();
        }
        // addPort 后端口可见
        QName dyn = new QName("urn:t", "dyn");
        d.addPort(dyn, javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING, "http://localhost:60123/x");
        java.util.Iterator<javax.xml.namespace.QName> it = d.getPorts();
        boolean found = false;
        while (it.hasNext()) {
            if (dyn.equals(it.next())) found = true;
        }
        check(found, "addPort makes port discoverable via getPorts");
        // Service.create 复用同一 SPI（对外行为一致）
        Service svc = Service.create(svcName);
        check(svcName.equals(svc.getServiceName()), "Service facade mirrors delegate metadata");
    }
}
