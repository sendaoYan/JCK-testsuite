/*
 * @test
 * @summary javax.xml.ws.ResponseWrapper conformance: annotation metadata, four-member defaults, response wrapper bean readback and coexistence with RequestWrapper.
 * @run main ResponseWrapperTest
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
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


public class ResponseWrapperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // ---- 辅助 SEI：全成员并与 RequestWrapper 共存 / 裸标注 / 未标注 ----
    @javax.jws.WebService(name = "EchoPort", targetNamespace = "urn:t")
    public interface EchoSei {
        @RequestWrapper(localName = "echo", targetNamespace = "urn:t", className = "EchoReq")
        @ResponseWrapper(localName = "echoResponse", targetNamespace = "urn:t",
                         className = "com.example.EchoResponseBean", partName = "parameters")
        String echo(String s);

        @ResponseWrapper
        String bare(String s);

        String plain(String s);
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testMetaAnnotations();
        testMemberContract();
        testUsageReadback();
        testNegativePlacement();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.ResponseWrapper");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("localName"), "has method localName");
        check(names.contains("targetNamespace"), "has method targetNamespace");
        check(names.contains("className"), "has method className");
        check(names.contains("partName"), "has method partName");
        check(c.getName().equals("javax.xml.ws.ResponseWrapper"), "binary name javax.xml.ws.ResponseWrapper");
        check(c.getDeclaredClasses().length == 0, "no declared member classes");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        check(c.getDeclaredConstructors().length == 0, "no declared constructors");
    }

    static void testMetaAnnotations() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.ResponseWrapper");
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
        Class<?> c = Class.forName("javax.xml.ws.ResponseWrapper");
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
        check(names.contains("localName") && names.contains("targetNamespace")
              && names.contains("className") && names.contains("partName"),
              "member name set = {localName, targetNamespace, className, partName}");
        check("".equals(c.getDeclaredMethod("localName").getDefaultValue()), "localName default empty");
        check("".equals(c.getDeclaredMethod("targetNamespace").getDefaultValue()), "targetNamespace default empty");
        check("".equals(c.getDeclaredMethod("className").getDefaultValue()), "className default empty");
        check("".equals(c.getDeclaredMethod("partName").getDefaultValue()), "partName default empty");
    }

    static void testUsageReadback() throws Exception {
        Method echo = EchoSei.class.getMethod("echo", String.class);
        ResponseWrapper rw = echo.getAnnotation(ResponseWrapper.class);
        check(rw != null, "ResponseWrapper readback non-null");
        check("echoResponse".equals(rw.localName()), "localName readback");
        check("urn:t".equals(rw.targetNamespace()), "targetNamespace readback");
        check("com.example.EchoResponseBean".equals(rw.className()), "className readback");
        check("parameters".equals(rw.partName()), "partName readback");
        check(rw.annotationType() == ResponseWrapper.class, "annotationType identity");
        // 与 RequestWrapper 同方法共存：各回读各值（wrapper bean 请求/响应独立）
        RequestWrapper req = echo.getAnnotation(RequestWrapper.class);
        check(req != null, "RequestWrapper coexists on same method");
        check(!req.localName().equals(rw.localName()), "request vs response localName differ");
        check(req.targetNamespace().equals(rw.targetNamespace()), "namespaces may coincide");
        // 裸标注
        ResponseWrapper bare = EchoSei.class.getMethod("bare", String.class).getAnnotation(ResponseWrapper.class);
        check(bare != null, "bare ResponseWrapper present");
        check("".equals(bare.localName()), "bare localName empty");
        check("".equals(bare.targetNamespace()), "bare targetNamespace empty");
        check("".equals(bare.className()), "bare className empty");
        check("".equals(bare.partName()), "bare partName empty");
        // 通用契约
        check(bare.equals(EchoSei.class.getMethod("bare", String.class).getAnnotation(ResponseWrapper.class)),
              "bare instances value-equal");
        check(bare.hashCode() == EchoSei.class.getMethod("bare", String.class).getAnnotation(ResponseWrapper.class).hashCode(),
              "hashCode consistent");
        check(!bare.equals(rw), "bare differs from fully-specified");
        check(bare.toString().startsWith("@javax.xml.ws.ResponseWrapper"), "toString prefix");
        // 未标注
        Method plain = EchoSei.class.getMethod("plain", String.class);
        check(plain.getAnnotation(ResponseWrapper.class) == null, "no annotation on unannotated method");
        check(!plain.isAnnotationPresent(ResponseWrapper.class), "isAnnotationPresent false unannotated");
        check(!ResponseWrapperTest.class.isAnnotationPresent(ResponseWrapper.class), "not applicable to class");
    }

    static void testNegativePlacement() {
        check(!ResponseWrapperTest.class.isAnnotationPresent(ResponseWrapper.class), "class-level presence false");
    }
}
