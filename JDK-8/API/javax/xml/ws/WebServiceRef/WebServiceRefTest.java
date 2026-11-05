/*
 * @test
 * @summary javax.xml.ws.WebServiceRef conformance: annotation metadata, six-member defaults (Object type, Service value sentinel), TYPE/FIELD/METHOD placement readback.
 * @run main WebServiceRefTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceRef;


public class WebServiceRefTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // SEI / Service 载体类型
    @javax.jws.WebService(name = "EchoPort", targetNamespace = "urn:t")
    public interface Echo {
        String echo(String s);
    }

    public static class EchoLookupService extends Service {
        protected EchoLookupService() {
            super(null, new javax.xml.namespace.QName("urn:t", "svc"));
        }
    }

    // ---- FIELD 位置：全成员 / 裸标注 ----
    public static class FieldCarrier {
        @WebServiceRef(name = "echoSvc", type = Echo.class,
                       value = EchoLookupService.class,
                       mappedName = "jboss/echo", wsdlLocation = "http://x/echo?wsdl",
                       lookup = "java:comp/env/echo")
        public Object fullField;

        @WebServiceRef
        public Object bareField;
    }

    // ---- METHOD 位置（setter 注入形态） ----
    public static class MethodCarrier {
        private Object target;
        @WebServiceRef(type = Echo.class, lookup = "java:comp/env/echo2")
        public void setEcho(Object t) { this.target = t; }
    }

    // ---- TYPE 位置（类级声明） ----
    @WebServiceRef(type = Echo.class, name = "classEcho")
    public static class TypeCarrier { }

    public static void main(String[] args) throws Exception {
        testStructure();
        testMetaAnnotations();
        testMemberContract();
        testUsageReadback();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.WebServiceRef");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("name"), "has method name");
        check(names.contains("type"), "has method type");
        check(names.contains("mappedName"), "has method mappedName");
        check(names.contains("value"), "has method value");
        check(names.contains("wsdlLocation"), "has method wsdlLocation");
        check(names.contains("lookup"), "has method lookup");
        check(c.getName().equals("javax.xml.ws.WebServiceRef"), "binary name javax.xml.ws.WebServiceRef");
        check(c.getDeclaredClasses().length == 0, "no declared member classes");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        check(c.getDeclaredConstructors().length == 0, "no declared constructors");
    }

    static void testMetaAnnotations() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.WebServiceRef");
        Retention ret = c.getAnnotation(Retention.class);
        check(ret != null && ret.value() == RetentionPolicy.RUNTIME, "retention is RUNTIME");
        Target tgt = c.getAnnotation(Target.class);
        check(tgt != null, "Target meta-annotation present");
        java.util.Set<ElementType> ts = new java.util.HashSet<ElementType>();
        for (ElementType e : tgt.value()) ts.add(e);
        check(ts.size() == 3, "Target has exactly 3 elements, got " + ts.size());
        check(ts.contains(ElementType.TYPE) && ts.contains(ElementType.METHOD) && ts.contains(ElementType.FIELD),
              "Target = {TYPE, METHOD, FIELD}");
        check(!ts.contains(ElementType.PARAMETER), "PARAMETER not targeted in JDK-8 (regression)");
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
        Class<?> c = Class.forName("javax.xml.ws.WebServiceRef");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 6, "exactly 6 declared members, got " + ms.length);
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Method m : ms) {
            names.add(m.getName());
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                  "member " + m.getName() + " public abstract");
            check(m.getDefaultValue() != null, "member " + m.getName() + " carries default");
        }
        check(names.contains("name") && names.contains("type") && names.contains("mappedName")
              && names.contains("value") && names.contains("wsdlLocation") && names.contains("lookup"),
              "member name set = {name, type, mappedName, value, wsdlLocation, lookup}");
        // 逐成员默认值精确锁定
        check("".equals(c.getDeclaredMethod("name").getDefaultValue()), "name default empty string");
        check(c.getDeclaredMethod("type").getDefaultValue() == Object.class,
              "type default java.lang.Object (sentinel for derive-from-site)");
        check("".equals(c.getDeclaredMethod("mappedName").getDefaultValue()), "mappedName default empty string");
        check(c.getDeclaredMethod("value").getDefaultValue() == Service.class,
              "value default javax.xml.ws.Service itself (generic service sentinel)");
        check("".equals(c.getDeclaredMethod("wsdlLocation").getDefaultValue()), "wsdlLocation default empty string");
        check("".equals(c.getDeclaredMethod("lookup").getDefaultValue()), "lookup default empty string");
        check(c.getDeclaredMethod("type").getReturnType() == Class.class, "type returns Class");
        check(c.getDeclaredMethod("value").getGenericReturnType().getTypeName()
              .startsWith("java.lang.Class<? extends javax.xml.ws.Service"),
              "value generic bound Service: " + c.getDeclaredMethod("value").getGenericReturnType().getTypeName());
    }

    static void testUsageReadback() throws Exception {
        // FIELD 全成员
        Field full = FieldCarrier.class.getDeclaredField("fullField");
        WebServiceRef r = full.getAnnotation(WebServiceRef.class);
        check(r != null, "FIELD full readback non-null");
        check("echoSvc".equals(r.name()), "name readback");
        check(r.type() == Echo.class, "type readback SEI class");
        check(r.value() == EchoLookupService.class, "value readback Service subclass");
        check("jboss/echo".equals(r.mappedName()), "mappedName readback");
        check("http://x/echo?wsdl".equals(r.wsdlLocation()), "wsdlLocation readback");
        check("java:comp/env/echo".equals(r.lookup()), "lookup readback");
        check(r.annotationType() == WebServiceRef.class, "annotationType identity");
        // FIELD 裸标注：六成员经默认值通道回读
        Field bareF = FieldCarrier.class.getDeclaredField("bareField");
        WebServiceRef bare = bareF.getAnnotation(WebServiceRef.class);
        check(bare != null, "FIELD bare readback non-null");
        check("".equals(bare.name()), "bare name empty");
        check(bare.type() == Object.class, "bare type default Object");
        check(bare.value() == Service.class, "bare value default Service");
        check("".equals(bare.mappedName()), "bare mappedName empty");
        check("".equals(bare.wsdlLocation()), "bare wsdlLocation empty");
        check("".equals(bare.lookup()), "bare lookup empty");
        // METHOD 位置
        Method setter = MethodCarrier.class.getMethod("setEcho", Object.class);
        WebServiceRef m = setter.getAnnotation(WebServiceRef.class);
        check(m != null, "METHOD placement readback non-null");
        check(m.type() == Echo.class, "method type readback");
        check("java:comp/env/echo2".equals(m.lookup()), "method lookup readback");
        check("".equals(m.name()), "method name falls back to default");
        // TYPE 位置
        WebServiceRef t = TypeCarrier.class.getAnnotation(WebServiceRef.class);
        check(t != null, "TYPE placement readback non-null");
        check(t.type() == Echo.class, "type-level type readback");
        check("classEcho".equals(t.name()), "type-level name readback");
        // 通用契约
        check(bare.equals(bareF.getAnnotation(WebServiceRef.class)), "bare instances value-equal");
        check(!bare.equals(r), "bare differs from fully-specified");
        check(bare.toString().startsWith("@javax.xml.ws.WebServiceRef"), "toString prefix");
        // 未标注位置
        check(FieldCarrier.class.getAnnotation(WebServiceRef.class) == null, "carrier class unannotated");
        check(WebServiceRefTest.class.getAnnotation(WebServiceRef.class) == null, "holder class unannotated");
    }
}
