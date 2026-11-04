/*
 * @test
 * @summary javax.net.ssl.SNIMatcher conformance: abstract matcher contract + createSNIMatcher behavior.
 * @run main SNIMatcherTest
 */
import java.lang.reflect.*;
import java.util.*;
import javax.net.ssl.*;
public class SNIMatcherTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // recording matcher: returns a configurable verdict and remembers the last probed name
    static class Recording extends SNIMatcher {
        final boolean verdict;
        SNIServerName last;
        Recording(int type, boolean verdict) { super(type); this.verdict = verdict; }
        public boolean matches(SNIServerName serverName) { last = serverName; return verdict; }
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SNIMatcher");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        boolean protCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (Modifier.isProtected(k.getModifiers()) && k.getParameterCount() == 1
                    && k.getParameterTypes()[0] == int.class) protCtor = true;
        check(protCtor, "protected (int type) constructor");
        Method m = c.getDeclaredMethod("matches", SNIServerName.class);
        check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                "public abstract matches(SNIServerName)");

        Recording yes = new Recording(0, true), no = new Recording(0, false);
        check(yes.getType() == 0, "getType from ctor");
        SNIHostName probe = new SNIHostName("example.com");
        check(yes.matches(probe), "recording matcher returns its verdict (true)");
        check(!no.matches(probe), "recording matcher returns its verdict (false)");
        check(yes.last == probe, "matches receives the exact server name object");
        Recording other = new Recording(131, true);
        check(other.getType() == 131, "arbitrary type supported");

        // SNIHostName.createSNIMatcher: case-insensitive host matching, type host_name
        SNIMatcher std = SNIHostName.createSNIMatcher("Example.COM");
        check(std.getType() == StandardConstants.SNI_HOST_NAME, "createSNIMatcher type is host_name");
        check(std.matches(new SNIHostName("example.com")), "standard matcher ignores case: exact lower");
        check(std.matches(new SNIHostName("EXAMPLE.com")), "standard matcher ignores case: mixed");
        check(!std.matches(new SNIHostName("sub.example.com")), "standard matcher is exact: subdomains do not match");
        check(!std.matches(new SNIHostName("other.org")), "standard matcher rejects different host");
        check(!std.matches(new SNIHostName("notexample.com")), "standard matcher requires domain boundary");
        check(expect(() -> SNIHostName.createSNIMatcher(null)).equals("NullPointerException"),
                "createSNIMatcher(null) -> NullPointerException");
        // matchers plug into SSLParameters
        SSLParameters p = new SSLParameters();
        List<SNIMatcher> ms = new ArrayList<>();
        ms.add(std);
        p.setSNIMatchers(ms);
        java.util.Collection<SNIMatcher> back = p.getSNIMatchers();
        check(back.size() == 1 && back.contains(std),
                "matcher collection round trip through SSLParameters");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
