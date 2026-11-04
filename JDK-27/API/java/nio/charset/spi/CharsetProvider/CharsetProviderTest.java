/*
 * @test
 * @summary java.nio.charset.spi.CharsetProvider conformance: white-box structure + black-box provider behavior.
 * @run main CharsetProviderTest
 */

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.charset.spi.CharsetProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CharsetProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (expected " + type.getSimpleName()
                    + ", got " + t.getClass().getName() + ")");
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    // A minimal concrete Charset served by the provider below; decoder/encoder delegate
    // to ISO-8859-1 so encode/decode round trips are real.
    static final class SimpleCharset extends Charset {
        SimpleCharset(String canonical, String[] aliases) {
            super(canonical, aliases);
        }
        public boolean contains(Charset cs) { return cs == this; }
        public java.nio.charset.CharsetDecoder newDecoder() {
            return StandardCharsets.ISO_8859_1.newDecoder();
        }
        public java.nio.charset.CharsetEncoder newEncoder() {
            return StandardCharsets.ISO_8859_1.newEncoder();
        }
    }

    // A named provider that records every SPI invocation.
    static final class CountingProvider extends CharsetProvider {
        int forNameCalls;
        int charsetsCalls;
        String lastQuery;
        final SimpleCharset a = new SimpleCharset("X-TEST-A", new String[]{"xta", "testA"});
        final SimpleCharset b = new SimpleCharset("X-TEST-B", new String[0]);
        public Iterator<Charset> charsets() {
            charsetsCalls++;
            return Arrays.asList((Charset) a, b).iterator();
        }
        public Charset charsetForName(String charsetName) {
            forNameCalls++;
            lastQuery = charsetName;
            if ("X-TEST-A".equals(charsetName)) return a;
            if ("X-TEST-B".equals(charsetName)) return b;
            return null;
        }
    }

    static final class EmptyProvider extends CharsetProvider {
        public Iterator<Charset> charsets() { return Collections.<Charset>emptyIterator(); }
        public Charset charsetForName(String charsetName) { return null; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.charset.spi.CharsetProvider");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() == Object.class, "superclass is Object");

        Constructor<?> ctor = c.getDeclaredConstructor();
        check(ctor != null, "no-arg constructor present");
        check(Modifier.isProtected(ctor.getModifiers()), "constructor is protected");
    }

    static void testBlackBox() {
        CharsetProvider p = new CharsetProvider() {
            public Iterator<Charset> charsets() {
                return Arrays.asList(StandardCharsets.UTF_8, StandardCharsets.US_ASCII).iterator();
            }
            public Charset charsetForName(String charsetName) {
                if ("UTF-8".equals(charsetName)) return StandardCharsets.UTF_8;
                if ("US-ASCII".equals(charsetName)) return StandardCharsets.US_ASCII;
                return null;
            }
        };
        check(p.charsetForName("UTF-8") == StandardCharsets.UTF_8, "charsetForName UTF-8");
        check(p.charsetForName("US-ASCII") == StandardCharsets.US_ASCII, "charsetForName US-ASCII");
        check(p.charsetForName("no-such") == null, "charsetForName null for unknown");

        List<Charset> all = new ArrayList<>();
        p.charsets().forEachRemaining(all::add);
        check(all.size() == 2, "charsets yields two charsets");
        check(all.get(0) == StandardCharsets.UTF_8, "charsets first is UTF-8");
        check(all.get(1) == StandardCharsets.US_ASCII, "charsets second is US-ASCII");

        // An empty provider.
        CharsetProvider empty = new CharsetProvider() {
            public Iterator<Charset> charsets() { return Collections.emptyIterator(); }
            public Charset charsetForName(String charsetName) { return null; }
        };
        check(!empty.charsets().hasNext(), "empty charsets iterator");
        check(empty.charsetForName("anything") == null, "empty provider returns null for any name");
    }

    static void testWhiteBoxMembers() throws Exception {
        Class<?> c = CharsetProvider.class;
        // The two SPI methods are the only abstract members.
        int abstractCount = 0;
        boolean foundCharsets = false;
        boolean foundForName = false;
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) {
                abstractCount++;
                if ("charsets".equals(m.getName())) foundCharsets = true;
                if ("charsetForName".equals(m.getName())) foundForName = true;
            }
        }
        check(abstractCount == 2, "exactly two abstract methods (charsets, charsetForName)");
        check(foundCharsets, "charsets() is abstract");
        check(foundForName, "charsetForName(String) is abstract");
        check(!Serializable.class.isAssignableFrom(c), "not Serializable");
        check(CharsetProvider.class.isAssignableFrom(CountingProvider.class),
                "subclass is assignable to CharsetProvider");
        // Direct instantiation of the abstract class is impossible. On the modular JDK,
        // java.base does not open java.nio.charset.spi to unnamed modules, so even
        // reflective access to the protected constructor is denied outright.
        try {
            Constructor<CharsetProvider> abstractCtor = CharsetProvider.class.getDeclaredConstructor();
            abstractCtor.setAccessible(true);
            abstractCtor.newInstance();
            check(false, "abstract CharsetProvider must not be instantiable");
        } catch (java.lang.reflect.InaccessibleObjectException expected) {
            check(true, "setAccessible on java.base protected ctor -> InaccessibleObjectException");
        } catch (ReflectiveOperationException e) {
            check(false, "unexpected reflective failure: " + e);
        }
    }

    // Black-box delegation: the base class forwards lookups verbatim; alias resolution is
    // the provider's own responsibility; charsets() yields a fresh iterator each call.
    static void testProviderDelegation() {
        CountingProvider p = new CountingProvider();
        check(p.forNameCalls == 0 && p.charsetsCalls == 0, "invocation counters start at zero");
        check(p.charsetForName("X-TEST-A") == p.a, "canonical name hit returns the custom charset");
        check(p.forNameCalls == 1, "charsetForName invoked exactly once");
        check("X-TEST-A".equals(p.lastQuery), "query string forwarded verbatim");
        check(p.charsetForName("xta") == null,
                "alias not auto-resolved by the base class: provider sees raw alias, may answer null");
        check("xta".equals(p.lastQuery), "alias query also forwarded verbatim");
        check(p.charsetForName("X-TEST-B") == p.b, "second canonical name hit");
        check(p.forNameCalls == 3, "three lookups recorded");
        check(p.charsetForName("no-such") == null, "unknown name answers null by contract");

        Iterator<Charset> it = p.charsets();
        check(p.charsetsCalls == 1, "charsets() invoked once");
        check(it.hasNext(), "iterator non-empty");
        check(it.next() == p.a, "first served charset is a");
        check(it.next() == p.b, "second served charset is b, order preserved");
        check(!it.hasNext(), "iterator exhausted after both charsets");
        Iterator<Charset> it2 = p.charsets();
        check(it2 != it, "each charsets() call yields a fresh iterator");
        check(it2.hasNext() && it2.next() == p.a, "fresh iterator restarts from the beginning");

        EmptyProvider empty = new EmptyProvider();
        check(!empty.charsets().hasNext(), "empty provider iterator is empty");
        check(empty.charsetForName("anything") == null, "empty provider answers null for any name");

        // A directly instantiated provider is NOT globally registered: the Charset lookup
        // entry points must not see it (and this test must not pollute global state).
        checkThrows(UnsupportedCharsetException.class, () -> Charset.forName("X-TEST-A"),
                "unregistered provider invisible to forName");
        check(!Charset.isSupported("X-TEST-A"), "unregistered provider invisible to isSupported");
    }

    // Contract of a custom Charset served by a provider, exercising the Charset defaults
    // a provider implementer relies on (name/aliases/displayName/toString/canEncode/...).
    static void testCustomCharsetContract() {
        SimpleCharset sc = new SimpleCharset("X-TEST-A", new String[]{"xta", "testA"});
        check("X-TEST-A".equals(sc.name()), "canonical name");
        Set<String> aliases = sc.aliases();
        check(aliases.size() == 2, "two aliases");
        check(aliases.contains("xta") && aliases.contains("testA"), "alias contents");
        checkThrows(UnsupportedOperationException.class, () -> aliases.add("zzz"),
                "aliases set is immutable");
        check("X-TEST-A".equals(sc.displayName()), "displayName() default == canonical name");
        check("X-TEST-A".equals(sc.displayName(Locale.FRENCH)), "displayName(Locale) default == canonical name");
        check("X-TEST-A".equals(sc.toString()), "toString() default == canonical name");
        check(sc.canEncode(), "canEncode() default true");
        check(!sc.isRegistered(), "X- prefixed names report unregistered");
        check(sc.hashCode() == "X-TEST-A".hashCode(), "hashCode == canonical name hashCode");
        check(sc.equals(sc), "equals reflexive");
        check(sc.equals(new SimpleCharset("X-TEST-A", new String[0])),
                "equals by canonical name across instances");
        check(!sc.equals(new SimpleCharset("X-TEST-Z", new String[0])), "different name not equal");
        check(!sc.equals(null), "not equal to null");
        check(!sc.equals("X-TEST-A"), "not equal to a plain String");
        check(sc.compareTo(sc) == 0, "compareTo self is 0");
        check(sc.compareTo(new SimpleCharset("X-TEST-Z", new String[0]))
                        == "X-TEST-A".compareTo("X-TEST-Z"),
                "compareTo is canonical-name ordered");
        check(sc.contains(sc), "contains delegates to the subclass implementation");
        check(!sc.contains(StandardCharsets.UTF_8), "contains false for unrelated charset");
        check(sc.newDecoder() != null && sc.newEncoder() != null, "newDecoder/newEncoder constructible");
        ByteBuffer encoded = sc.encode("AB");
        check(encoded.remaining() == 2, "encode via delegated ISO-8859-1 encoder");
        CharBuffer decoded = sc.decode(ByteBuffer.wrap(new byte[]{0x41, 0x42}));
        check("AB".contentEquals(decoded), "decode via delegated ISO-8859-1 decoder");

        SimpleCharset noAliases = new SimpleCharset("X-NA", null);
        check(noAliases.aliases().isEmpty(), "null aliases array tolerated, empty alias set");
        check(noAliases.aliases() == noAliases.aliases(), "aliases() returns a memoized set instance");
        check(new SimpleCharset("MyCS-1", new String[0]).isRegistered(),
                "non X- prefixed name reports registered");
    }

    // Charset constructor name/alias validation shared with Charset.forName rules.
    // (Note: JDK-27 skips these checks for the interned built-in names "UTF-8",
    // "ISO-8859-1", "US-ASCII" via a loader fast path, so neutral names are used here.)
    static void testCharsetCtorMatrix() {
        checkThrows(NullPointerException.class, () -> new SimpleCharset(null, new String[0]),
                "null canonical name -> NPE");
        checkThrows(IllegalCharsetNameException.class, () -> new SimpleCharset("", new String[0]),
                "empty canonical name -> ICE");
        checkThrows(IllegalCharsetNameException.class, () -> new SimpleCharset("bad name", new String[0]),
                "space in canonical name -> ICE");
        checkThrows(IllegalCharsetNameException.class, () -> new SimpleCharset("-ABC", new String[0]),
                "leading '-' in canonical name -> ICE");
        checkThrows(NullPointerException.class, () -> new SimpleCharset("OK-1", new String[]{null}),
                "null alias element -> NPE");
        checkThrows(IllegalCharsetNameException.class, () -> new SimpleCharset("OK-2", new String[]{"bad alias"}),
                "space in alias -> ICE");
        checkThrows(IllegalCharsetNameException.class, () -> new SimpleCharset("OK-3", new String[]{""}),
                "empty alias -> ICE");
        check("1A".equals(new SimpleCharset("1A", new String[0]).name()),
                "digit-leading canonical name accepted");
        check("A+B:C_.D-E".equals(new SimpleCharset("A+B:C_.D-E", new String[0]).name()),
                "digits and -:.+_ accepted after the first character");
    }

    // JDK-27 specific: the Charset constructor defensively copies the alias array for
    // non-bootstrap loaders (jdk27 Charset.java, Arrays.copyOf branch), so a
    // post-construction mutation of the caller's array is never visible.
    static void testAliasesArrayCopySemantics() {
        String[] arr = {"al1"};
        SimpleCharset sc = new SimpleCharset("X-COPY", arr);
        arr[0] = "mutated";
        check(sc.aliases().contains("al1"), "JDK-27: alias array defensively copied");
        check(!sc.aliases().contains("mutated"), "JDK-27: post-ctor mutation invisible");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testWhiteBoxMembers();
        testProviderDelegation();
        testCustomCharsetContract();
        testCharsetCtorMatrix();
        testAliasesArrayCopySemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
