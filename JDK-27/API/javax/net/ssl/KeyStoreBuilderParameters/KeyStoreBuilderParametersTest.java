/*
 * @test
 * @summary javax.net.ssl.KeyStoreBuilderParameters conformance: ctor validation + unmodifiable view.
 * @run main KeyStoreBuilderParametersTest
 */
import java.lang.reflect.*;
import java.security.*;
import java.util.*;
import javax.net.ssl.*;
public class KeyStoreBuilderParametersTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    static KeyStore.Builder builder(String name) {
        return KeyStore.Builder.newInstance(KeyStore.getDefaultType(), null,
                new KeyStore.PasswordProtection(new char[0]));
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.KeyStoreBuilderParameters");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getInterfaces().length == 1
                && c.getInterfaces()[0].getName().equals("javax.net.ssl.ManagerFactoryParameters"),
                "implements ManagerFactoryParameters");
        boolean listCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (Modifier.isPublic(k.getModifiers()) && k.getParameterCount() == 1
                    && k.getParameterTypes()[0].getName().equals("java.util.List")) listCtor = true;
        check(listCtor, "public constructor taking List<KeyStore.Builder>");

        KeyStore.Builder b1 = builder("one"), b2 = builder("two");
        KeyStoreBuilderParameters p = new KeyStoreBuilderParameters(Arrays.asList(b1, b2));
        List<KeyStore.Builder> got = p.getParameters();
        check(got.size() == 2, "list size preserved");
        check(got.get(0) == b1 && got.get(1) == b2, "exact builder objects preserved in order");
        // the returned list is an unmodifiable view
        check(expect(() -> got.add(builder("three"))).equals("UnsupportedOperationException"),
                "getParameters() list rejects add");
        check(expect(() -> got.remove(0)).equals("UnsupportedOperationException"),
                "getParameters() list rejects remove");
        check(expect(() -> { got.clear(); return "ok"; }).equals("UnsupportedOperationException"),
                "getParameters() list rejects clear");
        // a single-element list works
        KeyStoreBuilderParameters single = new KeyStoreBuilderParameters(
                Collections.singletonList(builder("solo")));
        check(single.getParameters().size() == 1, "single-element list kept");
        // ctor validation
        check(expect(() -> new KeyStoreBuilderParameters((List<KeyStore.Builder>) null))
                .equals("NullPointerException"), "null list -> NullPointerException");
        check(expect(() -> new KeyStoreBuilderParameters(new ArrayList<KeyStore.Builder>()))
                .equals("IllegalArgumentException"), "empty list -> IllegalArgumentException");
        // mutating the source list after construction does not change the parameters
        List<KeyStore.Builder> src = new ArrayList<>(Arrays.asList(b1, b2));
        KeyStoreBuilderParameters mut = new KeyStoreBuilderParameters(src);
        src.add(builder("late"));
        check(mut.getParameters().size() == 2, "source-list mutation after ctor is not observed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
