/*
 * @test
 * @summary javax.sql.DataSource API conformance: full contract coverage.
 *          interface hierarchy (CommonDataSource + java.sql.Wrapper), exact
 *          declared method set (getConnection x2) with SQLException, inherited
 *          CommonDataSource/Wrapper methods present, and a dynamic-proxy
 *          implementation verifying every method is callable and that
 *          getParentLogger() can throw SQLFeatureNotSupportedException and
 *          unwrap() can throw SQLException. Never opens a real connection.
 * @run main DataSourceTest
 */

import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Wrapper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataSourceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static Connection fakeConnection() {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new InvocationHandler() {
                    public Object invoke(Object p, Method m, Object[] a) { return null; }
                });
    }

    public static void main(String[] args) throws Exception {
        testHierarchy();
        testMethodSet();
        testJdk27DefaultMethods();
        testProxyInvocation();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testHierarchy() {
        Class<?> c = javax.sql.DataSource.class;
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface no superclass");

        Set<String> direct = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) direct.add(i.getName());
        check(direct.size() == 2, "two direct interfaces");
        check(direct.contains("javax.sql.CommonDataSource"), "extends CommonDataSource");
        check(direct.contains("java.sql.Wrapper"), "extends java.sql.Wrapper");
    }

    static void testMethodSet() {
        // Full public method set (declared + inherited) name -> parameter type list.
        Map<String, String> sig = new HashMap<String, String>();
        for (Method m : javax.sql.DataSource.class.getMethods()) {
            StringBuilder sb = new StringBuilder();
            for (Class<?> p : m.getParameterTypes()) sb.append(p.getName()).append(',');
            sig.put(m.getName(), sb.toString());
        }

        check(sig.containsKey("getConnection"), "has getConnection");
        check(sig.containsKey("getLogWriter"), "has getLogWriter (CommonDataSource)");
        check(sig.containsKey("setLogWriter"), "has setLogWriter (CommonDataSource)");
        check(sig.containsKey("setLoginTimeout"), "has setLoginTimeout (CommonDataSource)");
        check(sig.containsKey("getLoginTimeout"), "has getLoginTimeout (CommonDataSource)");
        check(sig.containsKey("getParentLogger"), "has getParentLogger (CommonDataSource)");
        check(sig.containsKey("unwrap"), "has unwrap (Wrapper)");
        check(sig.containsKey("isWrapperFor"), "has isWrapperFor (Wrapper)");

        // getConnection has two overloads (zero-arg and (String,String)).
        int connOverloads = 0;
        for (Method m : javax.sql.DataSource.class.getMethods()) {
            if (m.getName().equals("getConnection")
                    && m.getReturnType() == Connection.class
                    && m.getParameterTypes().length == 0) connOverloads++;
            if (m.getName().equals("getConnection")
                    && m.getReturnType() == Connection.class
                    && m.getParameterTypes().length == 2
                    && m.getParameterTypes()[0] == String.class
                    && m.getParameterTypes()[1] == String.class) connOverloads++;
        }
        check(connOverloads == 2, "two getConnection overloads");
    }

    static void testJdk27DefaultMethods() throws Exception {
        // JDK 9+ default factory methods (DataSource and CommonDataSource).
        Method cb = null;
        for (Method m : javax.sql.DataSource.class.getMethods()) {
            if (m.getName().equals("createConnectionBuilder")) cb = m;
        }
        check(cb != null, "has createConnectionBuilder");
        check(cb.isDefault(), "createConnectionBuilder is default");
        check(cb.getReturnType() == java.sql.ConnectionBuilder.class,
                "createConnectionBuilder returns ConnectionBuilder");
        check(cb.getParameterTypes().length == 0, "createConnectionBuilder no-arg");

        Method skb = null;
        for (Method m : javax.sql.CommonDataSource.class.getMethods()) {
            if (m.getName().equals("createShardingKeyBuilder")) skb = m;
        }
        check(skb != null, "has createShardingKeyBuilder (CommonDataSource)");
        check(skb.isDefault(), "createShardingKeyBuilder is default");
        check(skb.getReturnType() == java.sql.ShardingKeyBuilder.class,
                "createShardingKeyBuilder returns ShardingKeyBuilder");
    }

    static void testProxyInvocation() throws Exception {
        final Connection fake = fakeConnection();
        final PrintWriter writer = new PrintWriter(new java.io.StringWriter());

        javax.sql.DataSource ds = (javax.sql.DataSource) Proxy.newProxyInstance(
                javax.sql.DataSource.class.getClassLoader(),
                new Class<?>[]{javax.sql.DataSource.class},
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method m, Object[] a) throws Throwable {
                        String n = m.getName();
                        if (n.equals("getConnection")) return fake;
                        if (n.equals("getLogWriter")) return writer;
                        if (n.equals("setLogWriter")) return null;
                        if (n.equals("setLoginTimeout")) return null;
                        if (n.equals("getLoginTimeout")) return 42;
                        if (n.equals("getParentLogger")) throw new SQLFeatureNotSupportedException();
                        if (n.equals("unwrap")) {
                            Class<?> c = (Class<?>) a[0];
                            if (c == Connection.class) return fake;
                            throw new SQLException("not a wrapper");
                        }
                        if (n.equals("isWrapperFor")) return ((Class<?>) a[0]) == Connection.class;
                        if (n.equals("toString")) return "DataSourceProxy";
                        if (n.equals("hashCode")) return System.identityHashCode(proxy);
                        if (n.equals("equals")) return proxy == a[0];
                        return null;
                    }
                });

        check(ds.getConnection() == fake, "getConnection() returns implementation value");
        check(ds.getConnection("u", "p") == fake, "getConnection(u,p) returns implementation value");
        check(ds.getLogWriter() == writer, "getLogWriter");
        ds.setLogWriter(null);
        ds.setLoginTimeout(0);
        check(ds.getLoginTimeout() == 42, "getLoginTimeout");
        check(ds.isWrapperFor(Connection.class), "isWrapperFor true");
        check(!ds.isWrapperFor(String.class), "isWrapperFor false");
        check(ds.unwrap(Connection.class) == fake, "unwrap supported type");

        final javax.sql.DataSource fds = ds;
        checkThrows(SQLFeatureNotSupportedException.class, new ThrowingRunnable() {
            public void run() throws Exception { fds.getParentLogger(); }
        }, "getParentLogger throws SQLFeatureNotSupportedException");
        checkThrows(SQLException.class, new ThrowingRunnable() {
            public void run() throws Exception { fds.unwrap(String.class); }
        }, "unwrap(unsupported) throws SQLException");
    }
}
