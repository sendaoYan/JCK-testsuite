/*
 * @test
 * @summary java.util.InvalidPropertiesFormatException conformance: white-box structure + black-box behavior.
 * @run main InvalidPropertiesFormatExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class InvalidPropertiesFormatExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.InvalidPropertiesFormatException");
        check(c.getSuperclass() == IOException.class, "extends IOException");
        c.getDeclaredConstructor(Throwable.class);
        c.getDeclaredConstructor(String.class);
    }

    static void testBlackBox() {
        Throwable cause = new IllegalStateException("c");
        check(cause == new InvalidPropertiesFormatException(cause).getCause(),
                "Throwable ctor cause");
        check("bad".equals(new InvalidPropertiesFormatException("bad").getMessage()),
                "String ctor message");

        final Properties p = new Properties();
        boolean thrown = false;
        try {
            p.loadFromXML(new ByteArrayInputStream("<not-closed".getBytes(StandardCharsets.UTF_8)));
        } catch (InvalidPropertiesFormatException e) {
            thrown = true;
        } catch (IOException e) {
            throw new AssertionError("expected InvalidPropertiesFormatException, got " + e);
        }
        check(thrown, "malformed XML throws InvalidPropertiesFormatException");
    }


    // ===================== 契约深化：两种构造器语义/cause 无包装 =====================
    static void testIpfContracts() {
        check(InvalidPropertiesFormatException.class.getSuperclass() == java.io.IOException.class,
              "extends IOException (checked)");
        check(InvalidPropertiesFormatException.class.getName().startsWith("java.util."),
              "lives in java.util");
        RuntimeException cause = new RuntimeException("bad-xml");
        InvalidPropertiesFormatException e = new InvalidPropertiesFormatException(cause);
        check(e.getCause() == cause, "(Throwable) ctor stores cause verbatim");
        check(e.getMessage() == null || String.valueOf(e.getMessage()).contains("bad-xml"),
              "(Throwable) ctor message null or derived from cause (impl-dependent)");
        check(new InvalidPropertiesFormatException("m").getCause() == null,
              "(String) ctor leaves cause null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testIpfContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
