/*
 * @test
 * @summary javax.xml.ws.LogicalMessage conformance: logical message payload access (Source and JAXB forms), custom implementation round-trip.
 * @run main LogicalMessageTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.xml.bind.JAXBContext;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.LogicalMessage;

public class LogicalMessageTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "person")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    public static class Person {
        @javax.xml.bind.annotation.XmlElement
        public String name;
        @javax.xml.bind.annotation.XmlElement
        public int age;
    }

    // 记录型实现：Source/JAXB 双形态回读
    static final class SimpleLogicalMessage implements LogicalMessage {
        Source payload;
        Object jaxbPayload;
        JAXBContext jaxbCtx;
        SimpleLogicalMessage(Source s) { payload = s; }
        @Override public Source getPayload() { return payload; }
        @Override public void setPayload(Source s) { payload = s; }
        @Override public Object getPayload(JAXBContext ctx) {
            jaxbCtx = ctx;
            return jaxbPayload;
        }
        @Override public void setPayload(Object o, JAXBContext ctx) {
            jaxbPayload = o;
            jaxbCtx = ctx;
        }
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testSourceForm();
        testJaxbForm();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.LogicalMessage");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no super-interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getPayload"), "has method getPayload");
        check(names.contains("setPayload"), "has method setPayload");
        // 精确 4 个声明方法（Source 双形态 + JAXB 双形态）
        Method[] own = c.getDeclaredMethods();
        check(own.length == 4, "exactly 4 declared methods: " + own.length);
        // getPayload() 返回 Source
        Method g0 = c.getMethod("getPayload");
        check(g0.getReturnType() == Source.class, "getPayload() returns Source");
        check(g0.getParameterTypes().length == 0, "getPayload() no args");
        // getPayload(JAXBContext) 返回 Object
        Method g1 = c.getMethod("getPayload", JAXBContext.class);
        check(g1.getReturnType() == Object.class, "getPayload(JAXBContext) returns Object");
        // setPayload(Source) void；setPayload(Object,JAXBContext) void
        Method s0 = c.getMethod("setPayload", Source.class);
        check(s0.getReturnType() == void.class, "setPayload(Source) void");
        Method s1 = c.getMethod("setPayload", Object.class, JAXBContext.class);
        check(s1.getReturnType() == void.class, "setPayload(Object,JAXBContext) void");
        for (Method m : own) {
            check(Modifier.isAbstract(m.getModifiers()), "abstract: " + m.getName());
            check(Modifier.isPublic(m.getModifiers()), "public: " + m.getName());
        }
        // 与 LogicalMessageContext 关联（handler 族接口存在）
        check(Class.forName("javax.xml.ws.handler.LogicalMessageContext").isInterface(),
              "LogicalMessageContext interface exists");
        check(LogicalMessage.class.isAssignableFrom(Class.forName("javax.xml.ws.handler.LogicalMessageContext")) == false,
              "LogicalMessageContext is not a LogicalMessage (distinct types)");
    }

    // Source 形态：get/set 回读 + 写出验证（Transformer 把 Source 写到 Result）
    static void testSourceForm() throws Exception {
        StreamSource src = new StreamSource(new java.io.StringReader("<echo><arg0>hi</arg0></echo>"));
        SimpleLogicalMessage m = new SimpleLogicalMessage(src);
        check(m.getPayload() == src, "getPayload same Source reference");
        // setPayload(Source) 替换
        StreamSource other = new StreamSource(new java.io.StringReader("<swap/>"));
        m.setPayload(other);
        check(m.getPayload() == other, "setPayload(Source) replaces reference");
        // Source 可被 Transformer 消费（写出文本含 hi —— 真实可读载荷）
        StreamSource readable = new StreamSource(new java.io.StringReader("<r>v9</r>"));
        m.setPayload(readable);
        java.io.StringWriter sw = new java.io.StringWriter();
        Result res = new StreamResult(sw);
        javax.xml.transform.TransformerFactory.newInstance().newTransformer()
            .transform(m.getPayload(), res);
        check(sw.toString().contains("v9"), "payload Source consumable: " + sw);
        check(sw.toString().contains("<r>"), "payload root element present");
        // null 载荷
        m.setPayload((Source) null);
        check(m.getPayload() == null, "null payload set/get");
    }

    // JAXB 形态：JAXBContext 载荷 round-trip（Person 对象）
    static void testJaxbForm() throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(Person.class);
        Person p = new Person();
        p.name = "alice";
        p.age = 30;
        SimpleLogicalMessage m = new SimpleLogicalMessage(null);
        m.setPayload(p, ctx);
        check(m.jaxbPayload == p, "setPayload(Object,ctx) stores reference");
        check(m.jaxbCtx == ctx, "setPayload records context");
        check(m.getPayload(ctx) == p, "getPayload(ctx) same object back");
        // 真实 JAXB 闭环旁证：Person marshal/unmarshal round-trip（LogicalMessage 载荷类型自证）
        java.io.StringWriter w = new java.io.StringWriter();
        ctx.createMarshaller().marshal(p, w);
        String xml = w.toString();
        check(xml.contains("<person>") && xml.contains("<name>alice</name>") && xml.contains("<age>30</age>"),
              "jaxb payload marshals: " + xml);
        Person back = (Person) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(xml));
        check("alice".equals(back.name) && back.age == 30, "jaxb payload round-trip fields");
        m.setPayload(back, ctx);
        check(m.getPayload(ctx) == back, "unmarshalled object set/get");
        // Source <-> JAXB 双形态互不干扰
        StreamSource s = new StreamSource(new java.io.StringReader("<z/>"));
        m.setPayload(s);
        check(m.getPayload() == s, "source form after jaxb form");
        check(m.getPayload(ctx) == back, "jaxb form still intact");
    }
}
