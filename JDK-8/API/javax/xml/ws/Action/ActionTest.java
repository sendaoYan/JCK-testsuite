/*
 * @test
 * @summary javax.xml.ws.Action conformance: annotation metadata, member defaults, WS-Addressing action readback on service methods, fault sub-annotation matrix.
 * @run main ActionTest
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
import javax.xml.ws.Action;
import javax.xml.ws.FaultAction;


public class ActionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // ---- 辅助异常（FaultAction.className 实测载体） ----
    public static class FaultOneException extends Exception { }
    public static class FaultTwoException extends Exception { }

    // ---- 辅助 SEI：完整标注 / 裸标注 / 未标注三形态 ----
    @javax.jws.WebService(name = "EchoPort", targetNamespace = "urn:t")
    public interface EchoSei {
        @javax.jws.WebMethod(operationName = "echo")
        @Action(input = "urn:t:echo:in", output = "urn:t:echo:out",
                fault = { @FaultAction(className = FaultOneException.class, value = "urn:t:fault:one"),
                          @FaultAction(className = FaultTwoException.class, value = "urn:t:fault:two") })
        String echo(String s) throws FaultOneException, FaultTwoException;

        @Action
        String bare(String s);

        @Action(input = "urn:t:only:in")
        String onlyInput(String s);

        String plain(String s);
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testMetaAnnotations();
        testMemberContract();
        testDefaults();
        testUsageReadback();
        testNegativePlacement();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.Action");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("input"), "has method input");
        check(names.contains("output"), "has method output");
        check(names.contains("fault"), "has method fault");
        check(c.getName().equals("javax.xml.ws.Action"), "binary name javax.xml.ws.Action");
        check(c.getDeclaredClasses().length == 0, "no declared member classes");
        check(c.getDeclaredFields().length == 0, "no declared fields (annotation holds no constants)");
        check(c.getDeclaredConstructors().length == 0, "no declared constructors");
    }

    static void testMetaAnnotations() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.Action");
        Retention ret = c.getAnnotation(Retention.class);
        check(ret != null, "Retention meta-annotation present");
        check(ret != null && ret.value() == RetentionPolicy.RUNTIME, "retention is RUNTIME");
        Target tgt = c.getAnnotation(Target.class);
        check(tgt != null, "Target meta-annotation present");
        java.util.Set<ElementType> ts = new java.util.HashSet<ElementType>();
        for (ElementType e : tgt.value()) ts.add(e);
        check(ts.size() == 1, "Target has exactly one element: " + ts);
        check(ts.contains(ElementType.METHOD), "Target is METHOD only");
        check(c.isAnnotationPresent(Documented.class), "Documented present");
        check(!c.isAnnotationPresent(Inherited.class), "Inherited absent (regression)");
        check(!c.isAnnotationPresent(Repeatable.class), "Repeatable absent (regression)");
        // 声明面元注解恰好 3 个：Target + Retention + Documented
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
        Class<?> c = Class.forName("javax.xml.ws.Action");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 3, "exactly 3 declared members, got " + ms.length);
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Method m : ms) {
            names.add(m.getName());
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                  "member " + m.getName() + " is public abstract");
        }
        check(names.contains("input") && names.contains("output") && names.contains("fault"),
              "member name set = {input, output, fault}");
        check(c.getDeclaredMethod("input").getReturnType() == String.class, "input returns String");
        check(c.getDeclaredMethod("output").getReturnType() == String.class, "output returns String");
        check(c.getDeclaredMethod("fault").getReturnType() == javax.xml.ws.FaultAction[].class,
              "fault returns FaultAction[]");
        // 成员均带默认值（可裸用）
        for (Method m : ms) {
            check(m.getDefaultValue() != null, "member " + m.getName() + " carries a default value");
        }
    }

    static void testDefaults() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.Action");
        check("".equals(c.getDeclaredMethod("input").getDefaultValue()), "input default empty string");
        check("".equals(c.getDeclaredMethod("output").getDefaultValue()), "output default empty string");
        Object fd = c.getDeclaredMethod("fault").getDefaultValue();
        check(fd instanceof javax.xml.ws.FaultAction[], "fault default is FaultAction[]");
        check(((javax.xml.ws.FaultAction[]) fd).length == 0, "fault default is empty array");
    }

    static void testUsageReadback() throws Exception {
        Method echo = EchoSei.class.getMethod("echo", String.class);
        Action act = echo.getAnnotation(Action.class);
        check(act != null, "Action readback on annotated method non-null");
        check(act.annotationType() == Action.class, "annotationType identity is javax.xml.ws.Action");
        check("urn:t:echo:in".equals(act.input()), "input readback");
        check("urn:t:echo:out".equals(act.output()), "output readback");
        check(act.fault().length == 2, "fault array length 2");
        check(act.fault()[0].className() == FaultOneException.class, "fault[0].className readback");
        check("urn:t:fault:one".equals(act.fault()[0].value()), "fault[0].value readback");
        check(act.fault()[1].className() == FaultTwoException.class, "fault[1].className readback");
        check("urn:t:fault:two".equals(act.fault()[1].value()), "fault[1].value readback");
        check(act.fault()[0].annotationType() == FaultAction.class, "nested FaultAction annotationType");
        // 嵌套元素 equals 语义：同一成员重复访问返回等价实例
        check(act.fault()[0].equals(act.fault()[0]), "nested FaultAction equals is value-based");
        // 裸标注：成员经默认值通道回读
        Method bare = EchoSei.class.getMethod("bare", String.class);
        Action bareAct = bare.getAnnotation(Action.class);
        check(bareAct != null, "bare Action present");
        check("".equals(bareAct.input()), "bare input defaults to empty");
        check("".equals(bareAct.output()), "bare output defaults to empty");
        check(bareAct.fault().length == 0, "bare fault defaults to empty array");
        // 部分标注
        Action onlyIn = EchoSei.class.getMethod("onlyInput", String.class).getAnnotation(Action.class);
        check("urn:t:only:in".equals(onlyIn.input()), "partial input readback");
        check("".equals(onlyIn.output()), "partial output falls back to default");
        // Annotation 通用契约：等值实例 equals/hashCode、toString 前缀
        check(bareAct.equals(EchoSei.class.getMethod("bare", String.class).getAnnotation(Action.class)),
              "two bare instances value-equal");
        check(bareAct.hashCode() == EchoSei.class.getMethod("bare", String.class).getAnnotation(Action.class).hashCode(),
              "hashCode consistent with equals");
        check(bareAct.toString().startsWith("@javax.xml.ws.Action"), "toString prefix @javax.xml.ws.Action: " + bareAct);
        check(!bareAct.equals(act), "bare differs from fully-specified instance");
    }

    static void testNegativePlacement() throws Exception {
        // 未标注方法
        Method plain = EchoSei.class.getMethod("plain", String.class);
        check(plain.getAnnotation(Action.class) == null, "no Action on unannotated method");
        check(!plain.isAnnotationPresent(Action.class), "isAnnotationPresent false on unannotated method");
        // 非方法位置：类 / 字段上不可用（Target 仅 METHOD）
        check(ActionTest.class.getAnnotation(Action.class) == null, "no Action applicable to class");
        check(!ActionTest.class.isAnnotationPresent(Action.class), "class-level presence false");
    }
}
