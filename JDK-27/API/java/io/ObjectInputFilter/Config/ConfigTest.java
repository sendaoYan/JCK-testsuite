/*
 * @test
 * @summary java.io.ObjectInputFilter.Config conformance: white-box structure + black-box
 *          behavior (createFilter pattern and limit evaluation against FilterInfo,
 *          toString, getSerialFilterFactory; pattern wiring into ObjectInputStream).
 * @run main ConfigTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputFilter.Config;
import java.io.ObjectInputFilter.FilterInfo;
import java.io.ObjectInputFilter.Status;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.Serial;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ConfigTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Widget implements Serializable {
        @Serial
        private static final long serialVersionUID = 501L;
        int v = 1;
    }

    static FilterInfo info(final Class<?> c, final long arr, final long depth,
                           final long refs, final long bytes) {
        return new FilterInfo() {
            public Class<?> serialClass() { return c; }
            public long arrayLength() { return arr; }
            public long depth() { return depth; }
            public long references() { return refs; }
            public long streamBytes() { return bytes; }
        };
    }

    static byte[] ser(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(o);
        }
        return bos.toByteArray();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.ObjectInputFilter$Config");
        check(Modifier.isFinal(c.getModifiers()) && Modifier.isStatic(c.getModifiers()),
              "Config is a static final nested class");
        check(c.getDeclaredConstructors().length == 1
              && Modifier.isPrivate(c.getDeclaredConstructors()[0].getModifiers()),
              "single private constructor (utility class)");
        for (String m : new String[]{"createFilter", "getSerialFilter", "getSerialFilterFactory"})
            check(hasStatic(c, m), "static method " + m);
        check(c.getMethod("createFilter", String.class).getReturnType()
              == ObjectInputFilter.class, "createFilter returns ObjectInputFilter");
    }

    static boolean hasStatic(Class<?> c, String m) {
        for (Method mm : c.getDeclaredMethods())
            if (mm.getName().equals(m) && Modifier.isStatic(mm.getModifiers())) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. class patterns: allow-list, wildcard packages, reject-all terminator
        ObjectInputFilter f = Config.createFilter("java.lang.*;java.util.*;!*");
        check(f.checkInput(info(String.class, -1, 1, 1, 0)) == Status.ALLOWED,
              "java.lang.* allows String");
        check(f.checkInput(info(java.util.ArrayList.class, -1, 1, 1, 0)) == Status.ALLOWED,
              "java.util.* allows ArrayList");
        check(f.checkInput(info(java.io.File.class, -1, 1, 1, 0)) == Status.REJECTED,
              "!* rejects everything else");
        check(f.checkInput(info(int.class, -1, 1, 1, 0)) == Status.UNDECIDED,
              "primitive classes stay UNDECIDED");
        check(f.checkInput(info(null, -1, 1, 1, 0)) == Status.UNDECIDED,
              "no class -> UNDECIDED");

        // 2. exact class name without wildcard
        ObjectInputFilter one = Config.createFilter("java.lang.String");
        check(one.checkInput(info(String.class, -1, 1, 1, 0)) == Status.ALLOWED,
              "exact match allows");
        check(one.checkInput(info(Long.class, -1, 1, 1, 0)) == Status.UNDECIDED,
              "non-matching class not decided (no !* terminator)");

        // 3. array length limit evaluated against FilterInfo
        ObjectInputFilter maxarr = Config.createFilter("maxarray=4");
        check(maxarr.checkInput(info(int[].class, 5, 1, 1, 0)) == Status.REJECTED,
              "array length over maxarray rejected");
        check(maxarr.checkInput(info(int[].class, 4, 1, 1, 0)) == Status.UNDECIDED,
              "array length at limit undecided");

        // 4. depth / references / bytes limits
        ObjectInputFilter maxdepth = Config.createFilter("maxdepth=3");
        check(maxdepth.checkInput(info(null, -1, 4, 1, 0)) == Status.REJECTED,
              "depth over maxdepth rejected");
        check(maxdepth.checkInput(info(null, -1, 3, 1, 0)) == Status.UNDECIDED,
              "depth at limit undecided");
        ObjectInputFilter maxrefs = Config.createFilter("maxrefs=10");
        check(maxrefs.checkInput(info(null, -1, 1, 11, 0)) == Status.REJECTED,
              "references over maxrefs rejected");
        ObjectInputFilter maxbytes = Config.createFilter("maxbytes=100");
        check(maxbytes.checkInput(info(null, -1, 1, 1, 101)) == Status.REJECTED,
              "streamBytes over maxbytes rejected");
        check(maxbytes.checkInput(info(null, -1, 1, 1, 100)) == Status.UNDECIDED,
              "bytes at limit undecided");

        // 5. negative counters are rejected outright (malformed FilterInfo)
        check(Config.createFilter("maxdepth=5").checkInput(info(null, -1, -1, 1, 0))
              == Status.REJECTED, "negative depth rejected");

        // 6. limits + patterns combined in one pattern string
        ObjectInputFilter combo = Config.createFilter("java.lang.String;maxdepth=2;maxrefs=5");
        check(combo.checkInput(info(String.class, -1, 1, 1, 0)) == Status.ALLOWED,
              "combined: class pattern allows");
        check(combo.checkInput(info(String.class, -1, 3, 1, 0)) == Status.REJECTED,
              "combined: depth limit wins over class pattern");

        // 7. toString echoes the configured pattern (diagnostic contract)
        check("maxdepth=3".equals(maxdepth.toString()), "toString returns the pattern");

        // 8. malformed patterns throw IllegalArgumentException with a useful message;
        //    a bare word without '=' is a class pattern, not an error
        try {
            Config.createFilter("bogus=value");
            check(false, "unknown limit must throw");
        } catch (IllegalArgumentException iae) {
            check(iae.getMessage() != null && iae.getMessage().contains("unknown limit"),
                  "IAE message: " + iae.getMessage());
        }
        check("maxdepth".equals(Config.createFilter("maxdepth").toString()),
              "bare word parses as a class pattern");

        // 9. the JVM-wide filter factory is available and is the builtin factory
        Object factory = Config.getSerialFilterFactory();
        check(factory != null, "getSerialFilterFactory non-null");
        check(String.valueOf(factory).contains("BuiltinFilterFactory"),
              "default factory is the builtin one: " + factory);
        check(Config.getSerialFilter() == null
              || Config.getSerialFilter() instanceof ObjectInputFilter,
              "getSerialFilter returns a filter or null");

        // 10. pattern wiring into a live ObjectInputStream: allow Widget, reject String
        byte[] bytes;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new Widget());
            oos.writeObject("str");
            bytes = bos.toByteArray();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            ois.setObjectInputFilter(Config.createFilter("!*"));
            try {
                ois.readObject();
                check(false, "reject-all pattern must abort deserialization");
            } catch (InvalidClassException ice) {
                check(ice.getMessage().contains("REJECTED"), "wired rejection message");
            }
        }
    }

    // ================= deep additions (contract level) =================

    // ---- empty / null patterns: no patterns -> null filter (documented) ----
    static void testEmptyOrNullPatterns() throws Exception {
        check(Config.createFilter("") == null, "empty pattern yields a null filter");
        check(Config.createFilter(";") == null, "separator-only pattern yields a null filter");
        try {
            Config.createFilter(null);
            check(false, "createFilter(null) must throw");
        } catch (NullPointerException npe) {
            check("pattern".equals(npe.getMessage()), "NPE message: " + npe.getMessage());
        }
    }

    // ---- malformed patterns: measured exception types and messages ----
    static void testMalformedPatterns() throws Exception {
        try {
            Config.createFilter("maxdepth=abc");
            check(false, "non-numeric limit must throw");
        } catch (NumberFormatException nfe) {
            check("For input string: \"abc\"".equals(nfe.getMessage()),
                  "NFE message: " + nfe.getMessage());
        }
        try {
            Config.createFilter("maxdepth=-1");
            check(false, "negative maxdepth must throw");
        } catch (IllegalArgumentException iae) {
            check("negative limit: -1".equals(iae.getMessage()), "IAE message: " + iae.getMessage());
        }
        try {
            Config.createFilter("maxrefs=-5");
            check(false, "negative maxrefs must throw");
        } catch (IllegalArgumentException iae) {
            check("negative limit: -5".equals(iae.getMessage()), "IAE message: " + iae.getMessage());
        }
        try {
            Config.createFilter("/java.base");
            check(false, "module form without a module name must throw");
        } catch (IllegalArgumentException iae) {
            check(iae.getMessage() != null && iae.getMessage().contains("module name is missing")
                      && iae.getMessage().contains("/java.base"),
                  "IAE message echoes the pattern: " + iae.getMessage());
        }
        try {
            Config.createFilter("java.base/");
            check(false, "module form without a class/package must throw");
        } catch (IllegalArgumentException iae) {
            check(iae.getMessage() != null && iae.getMessage().contains("class or package missing"),
                  "IAE message: " + iae.getMessage());
        }
        try {
            Config.createFilter(".*");
            check(false, "package wildcard without a module must throw");
        } catch (IllegalArgumentException iae) {
            check(iae.getMessage() != null && iae.getMessage().contains("package missing"),
                  "IAE message: " + iae.getMessage());
        }
    }

    // ---- module/class patterns ----
    static void testModulePatterns() throws Exception {
        check(Config.createFilter("java.base/java.lang.String")
                  .checkInput(info(String.class, -1, 1, 1, 0)) == Status.ALLOWED,
              "module/class exact form allows the class");
        check(Config.createFilter("java.base/*")
                  .checkInput(info(String.class, -1, 1, 1, 0)) == Status.ALLOWED,
              "module/* allows classes of the module");
        check(Config.createFilter("java.base/java.lang.*")
                  .checkInput(info(String.class, -1, 1, 1, 0)) == Status.ALLOWED,
              "module/package.* allows the package");
        check(Config.createFilter("nomodule/java.lang.String")
                  .checkInput(info(String.class, -1, 1, 1, 0)) == Status.UNDECIDED,
              "wrong module prefix does not match (UNDECIDED)");
    }

    // ---- first matching pattern wins: order matters ----
    static void testFirstMatchWins() throws Exception {
        ObjectInputFilter allowFirst = Config.createFilter("java.lang.String;!*");
        check(allowFirst.checkInput(info(String.class, -1, 1, 1, 0)) == Status.ALLOWED,
              "earlier allow wins over the later !*");
        check(allowFirst.checkInput(info(Long.class, -1, 1, 1, 0)) == Status.REJECTED,
              "!* terminates everything else");
        ObjectInputFilter rejectFirst = Config.createFilter("!*;java.lang.String");
        check(rejectFirst.checkInput(info(String.class, -1, 1, 1, 0)) == Status.REJECTED,
              "earlier reject wins over the later allow (order asymmetry)");
        check(rejectFirst.checkInput(info(Long.class, -1, 1, 1, 0)) == Status.REJECTED,
              "and rejects everything else too");
    }

    // ---- '*' matches reference classes only, never arrays or primitives ----
    static void testStarScope() throws Exception {
        ObjectInputFilter star = Config.createFilter("*");
        check(star.checkInput(info(String.class, -1, 1, 1, 0)) == Status.ALLOWED,
              "* matches an ordinary reference class");
        check(star.checkInput(info(int[].class, 3, 1, 1, 0)) == Status.UNDECIDED,
              "* does NOT match an array class");
        check(star.checkInput(info(long.class, -1, 1, 1, 0)) == Status.UNDECIDED,
              "* does NOT match a primitive class");
        check(star.checkInput(info(void.class, -1, 1, 1, 0)) == Status.UNDECIDED,
              "* does NOT match void");
        check(Config.createFilter("[I").checkInput(info(int[].class, 3, 1, 1, 0))
                  == Status.UNDECIDED,
              "array binary name '[I' is not a matching class pattern (recorded behavior)");
        check(Config.createFilter("java.lang.String[]").checkInput(info(String[].class, 3, 1, 1, 0))
                  == Status.UNDECIDED,
              "[]-suffixed pattern does not match an array class");
    }

    // ---- nested classes: binary '$' names match, dotted forms do not ----
    static void testNestedClassNameForms() throws Exception {
        check(Config.createFilter("java.util.Map$Entry")
                  .checkInput(info(java.util.Map.Entry.class, -1, 1, 1, 0)) == Status.ALLOWED,
              "binary nested name with $ matches");
        check(Config.createFilter("java.util.Map.Entry")
                  .checkInput(info(java.util.Map.Entry.class, -1, 1, 1, 0)) == Status.UNDECIDED,
              "dotted nested name does not match");
    }

    // ---- duplicate limits: the LAST occurrence wins ----
    static void testDuplicateLimitLastWins() throws Exception {
        ObjectInputFilter dup = Config.createFilter("maxdepth=2;maxdepth=9");
        check(dup.checkInput(info(null, -1, 3, 1, 0)) == Status.UNDECIDED,
              "depth 3 passes: the earlier maxdepth=2 is superseded");
        check(dup.checkInput(info(null, -1, 5, 1, 0)) == Status.UNDECIDED,
              "depth 5 passes too");
        check(dup.checkInput(info(null, -1, 10, 1, 0)) == Status.REJECTED,
              "depth 10 rejected: effective limit is the last one (9)");
        check("maxdepth=2;maxdepth=9".equals(dup.toString()),
              "toString echoes both occurrences verbatim");
    }

    // ---- whitespace, case, negation and odd-but-legal forms ----
    static void testWhitespaceCaseAndNegation() throws Exception {
        ObjectInputFilter ws = Config.createFilter(" java.lang.String ");
        check(ws.checkInput(info(String.class, -1, 1, 1, 0)) == Status.UNDECIDED,
              "surrounding whitespace is NOT trimmed (no match)");
        check(" java.lang.String ".equals(ws.toString()),
              "toString preserves the whitespace verbatim");
        check(Config.createFilter("java.lang.string").checkInput(info(String.class, -1, 1, 1, 0))
                  == Status.UNDECIDED, "class patterns are case sensitive");
        ObjectInputFilter neg = Config.createFilter("!java.lang.String");
        check(neg.checkInput(info(String.class, -1, 1, 1, 0)) == Status.REJECTED,
              "'!' negates the following pattern for the named class");
        check(neg.checkInput(info(Long.class, -1, 1, 1, 0)) == Status.UNDECIDED,
              "negated pattern alone leaves other classes UNDECIDED");
        check("!java.lang.String".equals(neg.toString()), "negation kept in toString");
        ObjectInputFilter dot = Config.createFilter(".example.Class");
        check(".example.Class".equals(dot.toString()), "leading-dot pattern is accepted syntax");
        check(dot.checkInput(info(String.class, -1, 1, 1, 0)) == Status.UNDECIDED,
              "leading-dot pattern never matches an ordinary class name");
    }

    // ---- maxarray applies only when a length is actually reported ----
    static void testMaxarrayNotApplicable() throws Exception {
        ObjectInputFilter f = Config.createFilter("maxarray=4");
        check(f.checkInput(info(String.class, -1, 1, 1, 0)) == Status.UNDECIDED,
              "maxarray ignores non-arrays (arrayLength == -1)");
        check(f.checkInput(info(int[].class, -1, 1, 1, 0)) == Status.UNDECIDED,
              "maxarray ignores the descriptor stage where the length is not yet known");
    }

    // ---- read-only factory accessor facts (setters are global state, never called) ----
    static void testFactoryAccessorsReadonly() throws Exception {
        check("java.io.ObjectInputFilter$Config$BuiltinFilterFactory"
                  .equals(Config.getSerialFilterFactory().getClass().getTypeName()),
              "the default filter factory is the builtin one");
        check(Config.getSerialFilter() == null
                  || Config.getSerialFilter() instanceof ObjectInputFilter,
              "getSerialFilter observes the process-wide filter without modifying it");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmptyOrNullPatterns();
        testMalformedPatterns();
        testModulePatterns();
        testFirstMatchWins();
        testStarScope();
        testNestedClassNameForms();
        testDuplicateLimitLastWins();
        testWhitespaceCaseAndNegation();
        testMaxarrayNotApplicable();
        testFactoryAccessorsReadonly();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
