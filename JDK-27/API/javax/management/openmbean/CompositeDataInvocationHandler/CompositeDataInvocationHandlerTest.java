/*
 * @test
 * @summary javax.management.openmbean.CompositeDataInvocationHandler conformance: white-box structure + black-box behavior.
 * @run main CompositeDataInvocationHandlerTest
 */

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataInvocationHandler;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

public class CompositeDataInvocationHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void checkThrows(java.lang.Class<? extends Throwable> type, String exactMessage, Runnable r, String label) {
        try {
            r.run();
            throw new AssertionError(label + ": expected " + type.getSimpleName());
        } catch (Throwable e) {
            if (e instanceof AssertionError) throw (AssertionError) e;
            check(type.isInstance(e), label + ": type " + e.getClass().getName());
            if (exactMessage != null) check(exactMessage.equals(e.getMessage()),
                label + ": message <" + e.getMessage() + "> != <" + exactMessage + ">");
        }
    }

    // Getter-only interface, including a boolean isX getter.
    public interface NamedNumber {
        int getNumber();
        String getName();
        boolean isActive();
    }
    public interface NotAGetter {
        void doSomething(int x);
    }
    public interface Extra {
        String getMissing();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.CompositeDataInvocationHandler");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(InvocationHandler.class.isAssignableFrom(c), "implements InvocationHandler");
        check(c.getMethod("getCompositeData") != null, "has getCompositeData");
        check(c.getMethod("invoke", Object.class, java.lang.reflect.Method.class, Object[].class) != null, "has invoke");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        CompositeType ct = new CompositeType("NamedNumber", "desc",
            new String[]{"number", "name", "active"},
            new String[]{"number desc", "name desc", "active desc"},
            new OpenType[]{SimpleType.INTEGER, SimpleType.STRING, SimpleType.BOOLEAN});
        CompositeData cd = new CompositeDataSupport(ct,
            new String[]{"number", "name", "active"}, new Object[]{5, "five", Boolean.TRUE});

        CompositeDataInvocationHandler handler = new CompositeDataInvocationHandler(cd);
        check(handler.getCompositeData().equals(cd), "getCompositeData");

        NamedNumber nn = (NamedNumber) Proxy.newProxyInstance(
            NamedNumber.class.getClassLoader(), new Class[]{NamedNumber.class}, handler);
        check(nn.getNumber() == 5, "getNumber getter");
        check(nn.getName().equals("five"), "getName getter");
        check(nn.isActive() == true, "isActive boolean getter");

        // getter name maps to item key (capital -> lowercase fallback)
        CompositeData cd2 = new CompositeDataSupport(ct,
            new String[]{"number", "name", "active"}, new Object[]{7, "seven", Boolean.FALSE});
        NamedNumber nn2 = (NamedNumber) Proxy.newProxyInstance(
            NamedNumber.class.getClassLoader(), new Class[]{NamedNumber.class},
            new CompositeDataInvocationHandler(cd2));
        check(nn2.getNumber() == 7, "second proxy number");
        check(nn2.isActive() == false, "second proxy isActive false");

        // Object method handling: toString, equals, hashCode
        check(nn.toString().equals("Proxy[" + cd + "]"), "proxy toString");
        NamedNumber nnSame = (NamedNumber) Proxy.newProxyInstance(
            NamedNumber.class.getClassLoader(), new Class[]{NamedNumber.class},
            new CompositeDataInvocationHandler(new CompositeDataSupport(ct,
                new String[]{"number", "name", "active"}, new Object[]{5, "five", Boolean.TRUE})));
        check(nn.equals(nnSame), "proxy equals via same composite data");
        check(nn.hashCode() == nnSame.hashCode(), "proxy hashCode");
        check(!nn.equals(nn2), "proxy not equals different data");
        check(!nn.equals(null), "proxy equals null false");

        // exceptions (exact messages verified against JDK-27)
        checkThrows(IllegalArgumentException.class, "compositeData",
            () -> new CompositeDataInvocationHandler(null), "null ctor");

        // non-getter method through handler.invoke
        checkThrows(IllegalArgumentException.class, "Method is not getter: doSomething",
            () -> { try {
                java.lang.reflect.Method m = NotAGetter.class.getMethod("doSomething", int.class);
                handler.invoke(null, m, new Object[]{1});
            } catch (Throwable e) { throw sneaky(e); } }, "non-getter invoke");

        // getter with no matching item -> IllegalArgumentException mentioning property name
        checkThrows(IllegalArgumentException.class, "No CompositeData item Missing or missing to match getMissing",
            () -> {
                Extra e = (Extra) Proxy.newProxyInstance(Extra.class.getClassLoader(),
                    new Class[]{Extra.class}, handler);
                e.getMissing();
            }, "getter no matching item");
    }

    @SuppressWarnings("unchecked")
    static <E extends Throwable> RuntimeException sneaky(Throwable t) throws E { throw (E) t; }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
