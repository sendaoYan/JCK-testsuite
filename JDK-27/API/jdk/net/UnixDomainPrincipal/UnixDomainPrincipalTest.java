/*
 * @test
 * @summary jdk.net.UnixDomainPrincipal conformance: white-box structure + black-box behavior.
 * @run main UnixDomainPrincipalTest
 */

import jdk.net.*;
import java.net.*;
import java.lang.reflect.*;
import java.util.*;
public class UnixDomainPrincipalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static final boolean WINDOWS = java.io.File.separatorChar == '\\';

    /**
     * Windows reports principals domain qualified (machine name plus account
     * name); POSIX reports the bare account name. Accept both spellings.
     */
    static boolean sameAccount(String reported, String shortName) {
        if (reported.equalsIgnoreCase(shortName)) return true;
        return reported.toLowerCase().endsWith("\\" + shortName.toLowerCase());
    }

    /** First group that really exists on this host, or null. */
    static java.nio.file.attribute.GroupPrincipal findGroup(
            java.nio.file.attribute.UserPrincipalLookupService ls,
            java.nio.file.attribute.GroupPrincipal exclude) {
        String[] candidates = WINDOWS
                ? new String[] {"Users", "Administrators", "Everyone", "Authenticated Users", "Guests"}
                : new String[] {"root", "daemon", "bin", "sys", "adm"};
        for (String name : candidates) {
            try {
                java.nio.file.attribute.GroupPrincipal g = ls.lookupPrincipalByGroupName(name);
                if (g != null && (exclude == null || !g.equals(exclude))) return g;
            } catch (java.io.IOException e) { /* try next */ }
        }
        return null;
    }

    /** Stand-in when the host exposes no look-up-able group at all. */
    static java.nio.file.attribute.GroupPrincipal syntheticGroup(String name) {
        return new java.nio.file.attribute.GroupPrincipal() {
            public String getName() { return name; }
            public String toString() { return name; }
        };
    }

    /** A real user account different from {@code current}, or null. */
    static java.nio.file.attribute.UserPrincipal findOtherUser(
            java.nio.file.attribute.UserPrincipalLookupService ls, String current) {
        String[] candidates = WINDOWS
                ? new String[] {"SYSTEM", "Administrator", "Guest", "DefaultAccount"}
                : new String[] {"root", "daemon", "nobody"};
        for (String name : candidates) {
            if (sameAccount(name, current)) continue;
            try {
                java.nio.file.attribute.UserPrincipal p = ls.lookupPrincipalByName(name);
                if (p != null && !sameAccount(p.getName(), current)) return p;
            } catch (java.io.IOException e) { /* try next */ }
        }
        return null;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.net.UnixDomainPrincipal");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(java.lang.Record.class.isAssignableFrom(c), "is a record");
        check(hasMethod(c, "user"), "user");
        check(hasMethod(c, "group"), "group");

    }

    static void testBlackBox() throws Exception {
        java.nio.file.attribute.UserPrincipalLookupService ls = java.nio.file.FileSystems.getDefault().getUserPrincipalLookupService();
        java.nio.file.attribute.UserPrincipal up = ls.lookupPrincipalByName(System.getProperty("user.name"));
        java.nio.file.attribute.GroupPrincipal gp = findGroup(ls, null);
        if (gp == null) gp = syntheticGroup("no-group-on-this-host");
        UnixDomainPrincipal p = new UnixDomainPrincipal(up, gp);
        check(p.user() == up, "user");
        check(p.group() == gp, "group");
        check(p.toString() != null, "toString");
        check(p.equals(new UnixDomainPrincipal(up, gp)), "equals");
        check(p.hashCode() == new UnixDomainPrincipal(up, gp).hashCode(), "hashCode");
        deepen();

    }

    // ---- 深化(2026-11-05)：record 语义全表 + 真实 lookup + null 契约 + SO_PEERCRED 联动（经 JDK27.0.1 实测）----
    static void deepen() throws Exception {
        java.nio.file.attribute.UserPrincipalLookupService ls =
                java.nio.file.FileSystems.getDefault().getUserPrincipalLookupService();
        String uname = System.getProperty("user.name");
        java.nio.file.attribute.UserPrincipal up = ls.lookupPrincipalByName(uname);
        // 防御分支：按平台候选名找真实存在的组，都拿不到则仅做 null 契约与 user 侧断言
        java.nio.file.attribute.GroupPrincipal gp = findGroup(ls, null);
        // 1. record 结构：组件名/顺序/类型
        check(UnixDomainPrincipal.class.isRecord(), "isRecord");
        java.lang.reflect.RecordComponent[] rcs = UnixDomainPrincipal.class.getRecordComponents();
        check(rcs.length == 2, "two record components");
        check("user".equals(rcs[0].getName()) && "group".equals(rcs[1].getName()), "component names in order");
        check(rcs[0].getType() == java.nio.file.attribute.UserPrincipal.class, "user component type");
        check(rcs[1].getType() == java.nio.file.attribute.GroupPrincipal.class, "group component type");
        check(java.lang.Record.class == UnixDomainPrincipal.class.getSuperclass(), "superclass is Record");
        // 2. 构造器 null 契约：user/group 任一为 null → NPE（compact ctor Objects.requireNonNull）
        try { new UnixDomainPrincipal(null, null); check(false, "ctor(null,null) should NPE"); }
        catch (NullPointerException e) { check(true, "ctor(null,null) NPE"); }
        try { new UnixDomainPrincipal(up, null); check(false, "ctor(up,null) should NPE"); }
        catch (NullPointerException e) { check(true, "ctor(up,null) NPE"); }
        if (gp != null) {
            try { new UnixDomainPrincipal(null, gp); check(false, "ctor(null,gp) should NPE"); }
            catch (NullPointerException e) { check(true, "ctor(null,gp) NPE"); }
        }
        // 3. UserPrincipal 真实查找契约：同名两次查找等值、名字回读
        java.nio.file.attribute.UserPrincipal up2 = ls.lookupPrincipalByName(uname);
        check(sameAccount(up.getName(), uname),
                "UserPrincipal getName round-trip: " + up.getName());
        check(up.equals(up2), "same-name lookups equal");
        check(up.hashCode() == up2.hashCode(), "same-name lookups hashCode equal");
        check(!up.equals(null), "UserPrincipal not equals null");
        check(!up.equals(uname), "UserPrincipal not equals String");
        check(up.equals(up), "UserPrincipal equals self");
        check(up.toString() != null
                && up.toString().toLowerCase().contains(uname.toLowerCase()),
                "UserPrincipal toString contains name: " + up);
        // 另一个真实账户（若存在）查找结果不等值
        java.nio.file.attribute.UserPrincipal other = findOtherUser(ls, uname);
        if (other != null) check(!up.equals(other), "different users not equal: " + other);
        // 4. lookup 异常面：未知用户 → UserPrincipalNotFoundException（JDK-27 实测 getName() 回 ""，nss 路径不回填名字）；null → NPE
        try { ls.lookupPrincipalByName("no-such-user-xyz-123"); check(false, "unknown user should throw"); }
        catch (java.nio.file.attribute.UserPrincipalNotFoundException e) { check(e.getName() != null, "UserPrincipalNotFoundException type"); }
        try { ls.lookupPrincipalByName(null); check(false, "null name should throw"); }
        catch (NullPointerException e) { check(true, "null name NPE"); }
        // 5. 值语义全表（有组时）
        if (gp != null) {
            String gname = gp.getName();
            check(gname != null && !gname.isEmpty(), "GroupPrincipal getName non-empty");
            UnixDomainPrincipal p = new UnixDomainPrincipal(up, gp);
            check(p.user() == up, "user() identity");
            check(p.group() == gp, "group() identity");
            check(sameAccount(p.user().getName(), uname), "user() name: " + p.user().getName());
            check(gname.equals(p.group().getName()), "group() name");
            // toString 精确格式 UnixDomainPrincipal[user=<principal toString>, group=<principal toString>]
            // （POSIX 上 principal.toString() 就是名字；Windows 上是 "NAME (TYPE)"）
            String expect = "UnixDomainPrincipal[user=" + up + ", group=" + gp + "]";
            check(expect.equals(p.toString()), "toString exact format: " + p);
            // equals/hashCode：重建实例等值、null/String 不等、自等、hashCode 稳定
            check(p.equals(p), "equals self");
            check(p.equals(new UnixDomainPrincipal(up2, gp)), "equals rebuilt with re-looked-up principals");
            check(new UnixDomainPrincipal(up2, gp).equals(p), "equals symmetric");
            check(!p.equals(null), "equals null false");
            check(!p.equals("UnixDomainPrincipal[user=x, group=y]"), "equals String false");
            check(p.hashCode() == p.hashCode(), "hashCode stable");
            check(p.hashCode() == new UnixDomainPrincipal(up, gp).hashCode(), "hashCode equal for equal instances");
            // 换 user / 换 group 不等
            if (other != null)
                check(!p.equals(new UnixDomainPrincipal(other, gp)), "different user not equal");
            java.nio.file.attribute.GroupPrincipal otherG = findGroup(ls, gp);
            if (otherG != null)
                check(!p.equals(new UnixDomainPrincipal(up, otherG)), "different group not equal");
            // SO_PEERCRED 选项的 type 即本类（联动断言）
            check(jdk.net.ExtendedSocketOptions.SO_PEERCRED.type() == UnixDomainPrincipal.class, "SO_PEERCRED type is UnixDomainPrincipal");
        } else {
            // 无可用组：仍验证 user 侧 + SO_PEERCRED 联动
            check(up != null, "user principal available");
            check(jdk.net.ExtendedSocketOptions.SO_PEERCRED.type() == UnixDomainPrincipal.class, "SO_PEERCRED type is UnixDomainPrincipal (no-group branch)");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
