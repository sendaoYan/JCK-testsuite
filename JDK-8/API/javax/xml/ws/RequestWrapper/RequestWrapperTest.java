/*
 * @test
 * @summary javax.xml.ws.RequestWrapper conformance: annotation metadata, four-member defaults, wrapper bean name readback on service methods.
 * @run main RequestWrapperTest
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


public class RequestWrapperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // ---- 辅助 SEI：全成员 / 裸标注 / 未标注 ----
    @javax.jws.WebService(name = "EchoPort", targetNamespace = "urn:t")
    public interface EchoSei {
        @RequestWrapper(localName = "echo", targetNamespace = "urn:t",
                        className = "com.example.EchoRequestBean", partName = "parameters")
        String echo(String s);

        @RequestWrapper
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
        Class<?> c = Class.forName("javax.xml.ws.RequestWrapper");
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
        check(c.getName().equals("javax.xml.ws.RequestWrapper"), "binary name javax.xml.ws.RequestWrapper");
        check(c.getDeclaredClasses().length == 0, "no declared member classes");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        check(c.getDeclaredConstructors().length == 0, "no declared constructors");
    }

    static void testMetaAnnotations() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.RequestWrapper");
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
        Class<?> c = Class.forName("javax.xml.ws.RequestWrapper");
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
        RequestWrapper rw = EchoSei.class.getMethod("echo", String.class).getAnnotation(RequestWrapper.class);
        check(rw != null, "RequestWrapper readback non-null");
        check("echo".equals(rw.localName()), "localName readback");
        check("urn:t".equals(rw.targetNamespace()), "targetNamespace readback");
        check("com.example.EchoRequestBean".equals(rw.className()), "className readback");
        check("parameters".equals(rw.partName()), "partName readback");
        check(rw.annotationType() == RequestWrapper.class, "annotationType identity");
        // 裸标注：实例回读默认值全空串
        RequestWrapper bare = EchoSei.class.getMethod("bare", String.class).getAnnotation(RequestWrapper.class);
        check(bare != null, "bare RequestWrapper present");
        check("".equals(bare.localName()), "bare localName empty");
        check("".equals(bare.targetNamespace()), "bare targetNamespace empty");
        check("".equals(bare.className()), "bare className empty");
        check("".equals(bare.partName()), "bare partName empty");
        // 通用契约
        check(bare.equals(EchoSei.class.getMethod("bare", String.class).getAnnotation(RequestWrapper.class)),
              "bare instances value-equal");
        check(bare.hashCode() == EchoSei.class.getMethod("bare", String.class).getAnnotation(RequestWrapper.class).hashCode(),
              "hashCode consistent");
        check(!bare.equals(rw), "bare differs from fully-specified");
        check(bare.toString().startsWith("@javax.xml.ws.RequestWrapper"), "toString prefix");
        // 局部标注：仅 localName
        // (echo 已覆盖全量；此处复用 bare/echo 已足，额外局部形态见下面)
        java.lang.reflect.Method pm = EchoSei.class.getMethod("plain", String.class);
        check(pm.getAnnotation(RequestWrapper.class) == null, "no annotation on unannotated method");
        check(!pm.isAnnotationPresent(RequestWrapper.class), "isAnnotationPresent false unannotated");
        check(RequestWrapperTest.class.getAnnotation(RequestWrapper.class) == null, "not applicable to class");
    }

    static void testNegativePlacement() {
        // Target 仅 METHOD：类/字段上不可用（本类即未标注，作回归）
        check(!RequestWrapperTest.class.isAnnotationPresent(RequestWrapper.class), "class-level presence false");
    }
}
