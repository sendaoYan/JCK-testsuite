/*
 * @test
 * @summary javax.xml.ws.FaultAction conformance: annotation metadata, mandatory className (no default), fault action readback matrix nested under Action.
 * @run main FaultActionTest
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


public class FaultActionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // ---- 辅助异常（className 载体） ----
    public static class InsufficientFundsException extends Exception { }
    public static class TimeoutException extends Exception { }

    // ---- 辅助 SEI：双 fault 项 / 单 fault 项 / 裸 Action（fault 空数组） ----
    @javax.jws.WebService(name = "BankPort", targetNamespace = "urn:bank")
    public interface BankSei {
        @Action(input = "urn:bank:withdraw:in",
                fault = { @FaultAction(className = InsufficientFundsException.class, value = "urn:bank:fault:funds"),
                          @FaultAction(className = TimeoutException.class, value = "urn:bank:fault:timeout") })
        double withdraw(double amount) throws InsufficientFundsException, TimeoutException;

        @Action(fault = { @FaultAction(className = InsufficientFundsException.class) })
        double single(double amount) throws InsufficientFundsException;

        @Action
        double plain(double amount);
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
        Class<?> c = Class.forName("javax.xml.ws.FaultAction");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("className"), "has method className");
        check(names.contains("value"), "has method value");
        check(c.getName().equals("javax.xml.ws.FaultAction"), "binary name javax.xml.ws.FaultAction");
        check(c.getDeclaredClasses().length == 0, "no declared member classes");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        check(c.getDeclaredConstructors().length == 0, "no declared constructors");
    }

    static void testMetaAnnotations() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.FaultAction");
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
        Class<?> c = Class.forName("javax.xml.ws.FaultAction");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 2, "exactly 2 declared members, got " + ms.length);
        Method cn = c.getDeclaredMethod("className");
        Method val = c.getDeclaredMethod("value");
        check(Modifier.isPublic(cn.getModifiers()) && Modifier.isAbstract(cn.getModifiers()), "className public abstract");
        check(Modifier.isPublic(val.getModifiers()) && Modifier.isAbstract(val.getModifiers()), "value public abstract");
        check(cn.getReturnType() == Class.class, "className returns Class");
        check(val.getReturnType() == String.class, "value returns String");
        // 关键契约：className 无默认值（javadoc 曾写 FaultAction.class 哨兵；JDK-8 RI 实测无默认——省略即编译错误）
        check(cn.getDefaultValue() == null, "className has NO default value (mandatory member)");
        check("".equals(val.getDefaultValue()), "value default empty string");
        // className 泛型边界：Class<? extends Exception>
        check(cn.toGenericString().contains("java.lang.Class<? extends java.lang.Exception>"),
              "className generic bound: " + cn.toGenericString());
    }

    static void testUsageReadback() throws Exception {
        Method wd = BankSei.class.getMethod("withdraw", double.class);
        Action act = wd.getAnnotation(Action.class);
        check(act != null, "outer Action present");
        FaultAction[] fs = act.fault();
        check(fs.length == 2, "two nested FaultAction entries");
        check(fs[0].className() == InsufficientFundsException.class, "fault[0].className exact class");
        check("urn:bank:fault:funds".equals(fs[0].value()), "fault[0].value exact");
        check(fs[1].className() == TimeoutException.class, "fault[1].className exact class");
        check("urn:bank:fault:timeout".equals(fs[1].value()), "fault[1].value exact");
        check(fs[0].annotationType() == FaultAction.class, "annotationType identity javax.xml.ws.FaultAction");
        check(!fs[0].equals(fs[1]), "distinct fault entries not equal");
        check(fs[0].toString().contains("javax.xml.ws.FaultAction"), "toString mentions type");
        // 单 fault 项：value 省略 → 实例回读默认 ""
        Method sg = BankSei.class.getMethod("single", double.class);
        FaultAction[] one = sg.getAnnotation(Action.class).fault();
        check(one.length == 1, "single fault entry length 1");
        check(one[0].className() == InsufficientFundsException.class, "single className exact");
        check("".equals(one[0].value()), "omitted value falls back to empty string via instance");
        // 裸 Action：fault 为空数组
        FaultAction[] none = BankSei.class.getMethod("plain", double.class).getAnnotation(Action.class).fault();
        check(none.length == 0, "bare Action fault array empty");
    }

    static void testNegativePlacement() throws Exception {
        // FaultAction 只能作为 Action.fault 的嵌套元素出现；本测试自身类与方法上均不可见
        check(FaultActionTest.class.getAnnotation(FaultAction.class) == null, "not applicable to class");
        Method m = BankSei.class.getMethod("plain", double.class);
        check(m.getAnnotation(FaultAction.class) == null, "no standalone FaultAction on bare method");
        check(!m.isAnnotationPresent(FaultAction.class), "isAnnotationPresent false for standalone FaultAction");
    }
}
