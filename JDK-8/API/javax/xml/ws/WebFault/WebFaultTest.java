/*
 * @test
 * @summary javax.xml.ws.WebFault conformance: annotation metadata, four-member defaults, custom fault exception readback with getFaultInfo shape, built-in exception regression.
 * @run main WebFaultTest
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
import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebFault;


public class WebFaultTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // ---- 生成代码形态的故障异常：标注全成员 + 携带 fault bean 与 getFaultInfo ----
    public static class EchoFaultBean {
        private String message;
        public String getMessage() { return message; }
        public void setMessage(String m) { this.message = m; }
    }

    @WebFault(name = "echoFault", targetNamespace = "urn:t",
              faultBean = "EchoFaultBean", messageName = "echoFaultMsg")
    public static class EchoFaultException extends Exception {
        private final EchoFaultBean faultInfo;
        public EchoFaultException(String message, EchoFaultBean faultInfo) {
            super(message);
            this.faultInfo = faultInfo;
        }
        public EchoFaultBean getFaultInfo() { return faultInfo; }
    }

    // ---- 裸标注形态 ----
    @WebFault
    public static class BareFaultException extends Exception { }

    // ---- 未标注形态 ----
    public static class PlainException extends Exception { }

    public static void main(String[] args) throws Exception {
        testStructure();
        testMetaAnnotations();
        testMemberContract();
        testUsageReadback();
        testGeneratedShape();
        testBuiltinRegression();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.WebFault");
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
        check(names.contains("faultBean"), "has method faultBean");
        check(names.contains("messageName"), "has method messageName");
        check(c.getName().equals("javax.xml.ws.WebFault"), "binary name javax.xml.ws.WebFault");
        check(c.getDeclaredClasses().length == 0, "no declared member classes");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        check(c.getDeclaredConstructors().length == 0, "no declared constructors");
    }

    static void testMetaAnnotations() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.WebFault");
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
        Class<?> c = Class.forName("javax.xml.ws.WebFault");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 4, "exactly 4 declared members, got " + ms.length);
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Method m : ms) {
            names.add(m.getName());
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                  "member " + m.getName() + " public abstract");
            check(m.getReturnType() == String.class, "member " + m.getName() + " returns String");
        }
        check(names.contains("name") && names.contains("targetNamespace")
              && names.contains("faultBean") && names.contains("messageName"),
              "member name set = {name, targetNamespace, faultBean, messageName}");
        check("".equals(c.getDeclaredMethod("name").getDefaultValue()), "name default empty");
        check("".equals(c.getDeclaredMethod("targetNamespace").getDefaultValue()), "targetNamespace default empty");
        check("".equals(c.getDeclaredMethod("faultBean").getDefaultValue()), "faultBean default empty");
        check("".equals(c.getDeclaredMethod("messageName").getDefaultValue()), "messageName default empty");
    }

    static void testUsageReadback() {
        WebFault wf = EchoFaultException.class.getAnnotation(WebFault.class);
        check(wf != null, "WebFault readback on custom exception non-null");
        check("echoFault".equals(wf.name()), "name readback");
        check("urn:t".equals(wf.targetNamespace()), "targetNamespace readback");
        check("EchoFaultBean".equals(wf.faultBean()), "faultBean readback");
        check("echoFaultMsg".equals(wf.messageName()), "messageName readback");
        check(wf.annotationType() == WebFault.class, "annotationType identity");
        // 裸标注：实例回读默认全空串
        WebFault bare = BareFaultException.class.getAnnotation(WebFault.class);
        check(bare != null, "bare WebFault present");
        check("".equals(bare.name()), "bare name empty");
        check("".equals(bare.targetNamespace()), "bare targetNamespace empty");
        check("".equals(bare.faultBean()), "bare faultBean empty");
        check("".equals(bare.messageName()), "bare messageName empty");
        // 通用契约
        check(bare.equals(BareFaultException.class.getAnnotation(WebFault.class)), "bare instances value-equal");
        check(!bare.equals(wf), "bare differs from fully-specified");
        check(bare.toString().startsWith("@javax.xml.ws.WebFault"), "toString prefix");
        // 未标注类
        check(PlainException.class.getAnnotation(WebFault.class) == null, "no annotation on plain exception");
        check(!PlainException.class.isAnnotationPresent(WebFault.class), "isAnnotationPresent false");
    }

    // 生成故障形态：getFaultInfo 返回 fault bean（结构契约）
    static void testGeneratedShape() throws Exception {
        EchoFaultBean bean = new EchoFaultBean();
        bean.setMessage("boom");
        EchoFaultException ex = new EchoFaultException("wrapped", bean);
        check("wrapped".equals(ex.getMessage()), "exception message preserved");
        check(ex.getFaultInfo() == bean, "getFaultInfo returns same fault bean instance");
        check("boom".equals(ex.getFaultInfo().getMessage()), "fault bean payload intact");
        Method gfi = EchoFaultException.class.getMethod("getFaultInfo");
        check(gfi.getReturnType() == EchoFaultBean.class, "getFaultInfo signature returns bean type");
    }

    // 内建异常回归：javax.xml.ws 自身的异常族不带 WebFault（规范仅用于生成的故障类）
    static void testBuiltinRegression() {
        check(ProtocolException.class.getAnnotation(WebFault.class) == null, "ProtocolException not annotated");
        check(javax.xml.ws.WebServiceException.class.getAnnotation(WebFault.class) == null,
              "WebServiceException not annotated");
        check(javax.xml.ws.soap.SOAPFaultException.class.getAnnotation(WebFault.class) == null,
              "SOAPFaultException not annotated");
        check(javax.xml.ws.http.HTTPException.class.getAnnotation(WebFault.class) == null,
              "HTTPException not annotated");
    }
}
