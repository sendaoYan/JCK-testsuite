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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
