/*
 * @test
 * @summary jdk.net.NetworkPermission conformance: white-box structure + black-box behavior.
 * @run main NetworkPermissionTest
 */

import jdk.net.*;
import java.net.*;
import java.lang.reflect.*;
import java.util.*;
public class NetworkPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.net.NetworkPermission");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(java.security.BasicPermission.class.isAssignableFrom(c), "extends BasicPermission");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "getActions"), "getActions");

    }

    static void testBlackBox() throws Exception {
        NetworkPermission p = new NetworkPermission("unixDomainSocket");
        check("unixDomainSocket".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "actions empty");
        NetworkPermission p2 = new NetworkPermission("unixDomainSocket", "accept");
        check("".equals(p2.getActions()), "actions empty with actions arg");
        check(p.equals(p2), "equals same name");
        check(p.hashCode() == p2.hashCode(), "hashCode");
        deepen();

    }

    // ---- 深化(2026-11-05)：BasicPermission 契约矩阵 + 通配符 + 序列化 + @Deprecated（经 JDK27.0.1 实测）----
    static void deepen() throws Exception {
        // 1. 非法 target：null → NPE、空串 → IAE（两个构造器各自）
        try { new NetworkPermission(null); check(false, "ctor(null) should NPE"); }
        catch (NullPointerException e) { check(true, "ctor(null) NPE"); }
        try { new NetworkPermission(""); check(false, "ctor(empty) should IAE"); }
        catch (IllegalArgumentException e) { check(true, "ctor(empty) IAE"); }
        try { new NetworkPermission(null, "act"); check(false, "ctor2(null) should NPE"); }
        catch (NullPointerException e) { check(true, "ctor2(null) NPE"); }
        try { new NetworkPermission("", "act"); check(false, "ctor2(empty) should IAE"); }
        catch (IllegalArgumentException e) { check(true, "ctor2(empty) IAE"); }
        // 2. JDK-8 文档化的历史 target 名（27 已删文档，但 BasicPermission 接受任意非空名）
        NetworkPermission setFlow = new NetworkPermission("setOption.SO_FLOW_SLA");
        NetworkPermission getFlow = new NetworkPermission("getOption.SO_FLOW_SLA");
        check("setOption.SO_FLOW_SLA".equals(setFlow.getName()), "setFlow getName");
        check("getOption.SO_FLOW_SLA".equals(getFlow.getName()), "getFlow getName");
        check("".equals(setFlow.getActions()), "setFlow getActions empty");
        // 3. implies 矩阵
        check(setFlow.implies(setFlow), "implies self");
        check(setFlow.implies(new NetworkPermission("setOption.SO_FLOW_SLA")), "implies same-name new instance");
        check(!setFlow.implies(getFlow), "implies different target false");
        check(!setFlow.implies(null), "implies null false");
        // 4. BasicPermission 通配符语义
        NetworkPermission star = new NetworkPermission("*");
        check(star.implies(setFlow), "* implies setFlow");
        check(star.implies(getFlow), "* implies getFlow");
        check(!setFlow.implies(star), "specific does not imply *");
        NetworkPermission prefix = new NetworkPermission("setOption.*");
        check(prefix.implies(setFlow), "setOption.* implies setOption.SO_FLOW_SLA");
        check(!prefix.implies(getFlow), "setOption.* does not imply getOption.*");
        check(!star.implies(null), "* implies null false");
        // 5. 与其他 BasicPermission 子类互不隐含
        RuntimePermission rt = new RuntimePermission("setOption.SO_FLOW_SLA");
        check(!setFlow.implies(rt), "NetworkPermission does not imply RuntimePermission same name");
        check(!rt.implies(setFlow), "RuntimePermission does not imply NetworkPermission same name");
        check(!setFlow.equals(rt), "not equals RuntimePermission");
        // 6. equals/hashCode 契约
        check(setFlow.equals(setFlow), "equals self");
        check(!setFlow.equals(null), "equals null false");
        check(!setFlow.equals("setOption.SO_FLOW_SLA"), "equals String false");
        NetworkPermission setFlow2 = new NetworkPermission("setOption.SO_FLOW_SLA");
        check(setFlow.equals(setFlow2), "equals equal instance");
        check(setFlow2.equals(setFlow), "equals symmetric");
        check(setFlow.hashCode() == setFlow2.hashCode(), "hashCode equal for equal instances");
        check(setFlow.hashCode() == setFlow.hashCode(), "hashCode stable");
        // 7. actions 参数被忽略
        NetworkPermission withActions = new NetworkPermission("setOption.SO_FLOW_SLA", "connect,accept");
        check("".equals(withActions.getActions()), "actions arg ignored -> empty");
        check(setFlow.equals(withActions), "actions ignored in equals");
        check(new NetworkPermission("a", "b").equals(new NetworkPermission("a", "c")), "different actions still equal");
        // 8. Serializable round-trip（内存字节流）
        check(java.io.Serializable.class.isAssignableFrom(NetworkPermission.class), "Serializable");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(setFlow);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object back = ois.readObject();
        ois.close();
        check(back instanceof NetworkPermission, "round-trip class");
        check(setFlow.equals(back), "round-trip equals");
        check(back.hashCode() == setFlow.hashCode(), "round-trip hashCode");
        check("setOption.SO_FLOW_SLA".equals(((NetworkPermission) back).getName()), "round-trip getName");
        check("".equals(((NetworkPermission) back).getActions()), "round-trip getActions");
        // 9. serialVersionUID 直锁（与 JDK-8 相同）
        check(java.io.ObjectStreamClass.lookup(NetworkPermission.class).getSerialVersionUID()
                == -2012939586906722291L, "serialVersionUID");
        // 10. newPermissionCollection
        java.security.PermissionCollection col = setFlow.newPermissionCollection();
        check(col != null, "newPermissionCollection non-null");
        col.add(setFlow);
        check(col.implies(new NetworkPermission("setOption.SO_FLOW_SLA")), "collection implies same");
        check(!col.implies(getFlow), "collection does not imply other");
        // 11. toString / 注解面（JDK-27 独有：@Deprecated(since="25", forRemoval=true)）
        String ts = setFlow.toString();
        check(ts.contains("jdk.net.NetworkPermission") && ts.contains("setOption.SO_FLOW_SLA"), "toString contents");
        Deprecated dep = NetworkPermission.class.getAnnotation(Deprecated.class);
        check(dep != null, "@Deprecated present on JDK-27");
        check("25".equals(dep.since()), "@Deprecated since 25");
        check(dep.forRemoval(), "@Deprecated forRemoval");
        check(java.security.BasicPermission.class == NetworkPermission.class.getSuperclass(), "direct superclass BasicPermission");
        check(Modifier.isPublic(NetworkPermission.class.getModifiers()) && Modifier.isFinal(NetworkPermission.class.getModifiers()), "public final");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
