/*
 * @test
 * @summary java.lang.Appendable API conformance: append(CharSequence), append(CharSequence,
 *          int,int), and append(char) return the receiver and append in order; index bounds
 *          on the (csq,start,end) overload.
 * @run main AppendableTest
 */

public class AppendableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAppend() throws Exception {
        Appendable sb = new StringBuilder();
        check(sb.append("ab") == sb, "append(csq) returns this");
        check(sb.append("cd", 0, 2).toString().equals("abcd"), "append(csq,start,end)");
        check(sb.append('e').toString().equals("abcde"), "append(char)");
        check(sb.append(null).toString().equals("abcdenull"), "append(null csq) appends \"null\"");
    }

    static void testBounds() throws Exception {
        Appendable sb = new StringBuilder();
        try {
            sb.append("ab", -1, 1);
            throw new AssertionError("append(csq,-1,1): expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }
        try {
            sb.append("ab", 1, 3);
            throw new AssertionError("append(csq,1,3): expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }
        try {
            sb.append("ab", 2, 1);
            throw new AssertionError("append(csq,2,1): expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }
    }

    static final class Recorder implements Appendable {
        final StringBuilder buf = new StringBuilder();
        boolean fail;
        public Appendable append(CharSequence csq) throws java.io.IOException {
            if (fail) throw new java.io.IOException("fail");
            buf.append(csq);
            return this;
        }
        public Appendable append(CharSequence csq, int start, int end) throws java.io.IOException {
            if (fail) throw new java.io.IOException("fail");
            buf.append(csq, start, end);
            return this;
        }
        public Appendable append(char c) throws java.io.IOException {
            if (fail) throw new java.io.IOException("fail");
            buf.append(c);
            return this;
        }
        public String toString() { return buf.toString(); }
    }

    static void testContract() throws Exception {
        Recorder r = new Recorder();
        check(r.append("hello") == r, "append(csq) returns this");
        check(r.toString().equals("hello"), "append(csq) content");
        r = new Recorder();
        r.append("hello", 1, 3);
        check(r.toString().equals("el"), "append(csq,1,3) == subSequence(1,3)");
        r.append('!');
        check(r.toString().equals("el!"), "append(char)");

        Recorder a = new Recorder(), b = new Recorder();
        a.append("world");
        b.append("world", 0, 5);
        check(a.toString().equals(b.toString()), "append(csq) == append(csq,0,len)");

        Recorder n = new Recorder();
        n.append((CharSequence) null);
        check(n.toString().equals("null"), "append(null) == \"null\"");
        Recorder n2 = new Recorder();
        n2.append((CharSequence) null, 0, 4);
        check(n2.toString().equals("null"), "append(null,0,4) == \"null\"");
    }

    static void testIOException() throws Exception {
        Recorder r = new Recorder();
        r.fail = true;
        try {
            r.append('x');
            throw new AssertionError("append(char): expected IOException");
        } catch (java.io.IOException expected) {
            passed++;
        }
        try {
            r.append("x");
            throw new AssertionError("append(csq): expected IOException");
        } catch (java.io.IOException expected) {
            passed++;
        }
        try {
            r.append("x", 0, 1);
            throw new AssertionError("append(csq,s,e): expected IOException");
        } catch (java.io.IOException expected) {
            passed++;
        }
    }

    static void testNullAndOtherImpls() throws Exception {
        Appendable sb = new StringBuilder();
        sb.append((CharSequence) null, 0, 4);
        check(sb.toString().equals("null"), "StringBuilder.append(null,0,4)");
        Appendable sb2 = new StringBuilder();
        try {
            sb2.append((CharSequence) null, 0, 5);
            throw new AssertionError("append(null,0,5): expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }
        Appendable buf = new StringBuffer();
        buf.append("ab").append('c');
        check(buf.toString().equals("abc"), "StringBuffer as Appendable");
    }

    // ---------- white-box: structural / reflection ----------
    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try {
            c.getMethod(name, params);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Appendable");
        check(c.isInterface(), "Appendable is an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "Appendable abstract");
        check(!java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Appendable not final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasMethod(c, "append", CharSequence.class), "append(CharSequence)");
        check(hasMethod(c, "append", CharSequence.class, int.class, int.class),
              "append(CharSequence,int,int)");
        check(hasMethod(c, "append", char.class), "append(char)");
        boolean declaresIO = false;
        for (Class<?> t : c.getMethod("append", CharSequence.class).getExceptionTypes()) {
            if (t == java.io.IOException.class) declaresIO = true;
        }
        check(declaresIO, "append(CharSequence) declares IOException");
    }

    // ---------- black-box: behavioral composition ----------
    static void testBlackBox() throws Exception {
        testAppend();
        testBounds();
        testContract();
        testIOException();
        testNullAndOtherImpls();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
