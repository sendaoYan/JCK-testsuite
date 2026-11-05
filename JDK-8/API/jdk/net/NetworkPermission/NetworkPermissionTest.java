/*
 * @test
 * @summary jdk.net.NetworkPermission conformance: network permission.
 * @run main NetworkPermissionTest
 */

public class NetworkPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        jdk.net.NetworkPermission p = new jdk.net.NetworkPermission("a", "b");
        check("a".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "getActions is empty (BasicPermission ignores actions)");
        check(p.implies(new jdk.net.NetworkPermission("a", "b")), "implies");
        jdk.net.NetworkPermission p2 = new jdk.net.NetworkPermission("a", "b");
        check(p.equals(p2), "equals");
        check(p.hashCode() == p2.hashCode(), "hashCode");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- 深化(2026-11-05)：文档化 target 全集 + BasicPermission 契约矩阵 + 序列化（经 JDK8u372 实测）----
    static void deepen() throws Exception {
        // 1. JDK-8 文档化的两个 target：setOption.SO_FLOW_SLA / getOption.SO_FLOW_SLA
        jdk.net.NetworkPermission setFlow = new jdk.net.NetworkPermission("setOption.SO_FLOW_SLA");
        jdk.net.NetworkPermission getFlow = new jdk.net.NetworkPermission("getOption.SO_FLOW_SLA");
        check("setOption.SO_FLOW_SLA".equals(setFlow.getName()), "setFlow getName");
        check("getOption.SO_FLOW_SLA".equals(getFlow.getName()), "getFlow getName");
        check("".equals(setFlow.getActions()), "setFlow getActions empty");
        // 2. 非法 target：null → NPE、空串 → IllegalArgumentException（两个构造器各自）
        try { new jdk.net.NetworkPermission(null); check(false, "ctor(null) should NPE"); }
        catch (NullPointerException e) { check(true, "ctor(null) NPE"); }
        try { new jdk.net.NetworkPermission(""); check(false, "ctor(empty) should IAE"); }
        catch (IllegalArgumentException e) { check(true, "ctor(empty) IAE"); }
        try { new jdk.net.NetworkPermission(null, "act"); check(false, "ctor2(null) should NPE"); }
        catch (NullPointerException e) { check(true, "ctor2(null) NPE"); }
        try { new jdk.net.NetworkPermission("", "act"); check(false, "ctor2(empty) should IAE"); }
        catch (IllegalArgumentException e) { check(true, "ctor2(empty) IAE"); }
        // 3. implies 矩阵
        check(setFlow.implies(setFlow), "implies self");
        check(setFlow.implies(new jdk.net.NetworkPermission("setOption.SO_FLOW_SLA")), "implies same-name new instance");
        check(!setFlow.implies(getFlow), "implies different target false");
        check(!setFlow.implies(null), "implies null false");
        // 4. BasicPermission 通配符语义：* 隐含一切；前缀.* 隐含该前缀
        jdk.net.NetworkPermission star = new jdk.net.NetworkPermission("*");
        check(star.implies(setFlow), "* implies setFlow");
        check(star.implies(getFlow), "* implies getFlow");
        check(!setFlow.implies(star), "specific does not imply *");
        jdk.net.NetworkPermission prefix = new jdk.net.NetworkPermission("setOption.*");
        check(prefix.implies(setFlow), "setOption.* implies setOption.SO_FLOW_SLA");
        check(!prefix.implies(getFlow), "setOption.* does not imply getOption.*");
        check(!star.implies(null), "* implies null false");
        // 5. 与其他 BasicPermission 子类互不隐含（类不等即拒）
        RuntimePermission rt = new RuntimePermission("setOption.SO_FLOW_SLA");
        check(!setFlow.implies(rt), "NetworkPermission does not imply RuntimePermission same name");
        check(!rt.implies(setFlow), "RuntimePermission does not imply NetworkPermission same name");
        check(!setFlow.equals(rt), "not equals RuntimePermission");
        // 6. equals/hashCode 契约
        check(setFlow.equals(setFlow), "equals self");
        check(!setFlow.equals(null), "equals null false");
        check(!setFlow.equals("setOption.SO_FLOW_SLA"), "equals String false");
        jdk.net.NetworkPermission setFlow2 = new jdk.net.NetworkPermission("setOption.SO_FLOW_SLA");
        check(setFlow.equals(setFlow2), "equals equal instance");
        check(setFlow2.equals(setFlow), "equals symmetric");
        check(setFlow.hashCode() == setFlow2.hashCode(), "hashCode equal for equal instances");
        check(setFlow.hashCode() == setFlow.hashCode(), "hashCode stable");
        // 7. actions 参数被忽略（BasicPermission 契约）
        jdk.net.NetworkPermission withActions = new jdk.net.NetworkPermission("setOption.SO_FLOW_SLA", "connect,accept");
        check("".equals(withActions.getActions()), "actions arg ignored -> empty");
        check(setFlow.equals(withActions), "actions ignored in equals");
        check(new jdk.net.NetworkPermission("a", "b").equals(new jdk.net.NetworkPermission("a", "c")), "different actions still equal");
        // 8. Serializable round-trip（内存字节流，无临时文件）
        check(java.io.Serializable.class.isAssignableFrom(jdk.net.NetworkPermission.class), "Serializable");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(setFlow);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object back = ois.readObject();
        ois.close();
        check(back instanceof jdk.net.NetworkPermission, "round-trip class");
        check(setFlow.equals(back), "round-trip equals");
        check(back.hashCode() == setFlow.hashCode(), "round-trip hashCode");
        check("setOption.SO_FLOW_SLA".equals(((jdk.net.NetworkPermission) back).getName()), "round-trip getName");
        check("".equals(((jdk.net.NetworkPermission) back).getActions()), "round-trip getActions");
        // 9. serialVersionUID 直锁（OpenJDK 源码常量，两侧一致）
        check(java.io.ObjectStreamClass.lookup(jdk.net.NetworkPermission.class).getSerialVersionUID()
                == -2012939586906722291L, "serialVersionUID");
        // 10. newPermissionCollection
        java.security.PermissionCollection col = setFlow.newPermissionCollection();
        check(col != null, "newPermissionCollection non-null");
        col.add(setFlow);
        check(col.implies(new jdk.net.NetworkPermission("setOption.SO_FLOW_SLA")), "collection implies same");
        check(!col.implies(getFlow), "collection does not imply other");
        // 11. toString / 注解面
        String ts = setFlow.toString();
        check(ts.contains("jdk.net.NetworkPermission") && ts.contains("setOption.SO_FLOW_SLA"), "toString contents");
        check(!jdk.net.NetworkPermission.class.isAnnotationPresent(Deprecated.class), "not deprecated on JDK-8");
        check(java.security.BasicPermission.class == jdk.net.NetworkPermission.class.getSuperclass(), "direct superclass BasicPermission");
        check(jdk.net.NetworkPermission.class.getModifiers() == java.lang.reflect.Modifier.PUBLIC + java.lang.reflect.Modifier.FINAL, "public final");
    }
}
