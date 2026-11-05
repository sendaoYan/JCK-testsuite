/*
 * @test
 * @summary jdk.jshell.spi.SPIResolutionException conformance: spi resolution exception.
 * @run main SPIResolutionExceptionTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class SPIResolutionExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.spi.SPIResolutionException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("id")) f=true; check(f, "has id"); }
    }

    static void testBlackBox() throws Exception {

        SPIResolutionException e = new SPIResolutionException(42);
        check(e.id() == 42, "id");
        check(e instanceof RuntimeException, "runtime exception");
    }


    // ---- deep contract: message format and serialization behavior ----
    static void testDeepSemantics() throws Exception {
        SPIResolutionException e = new SPIResolutionException(42);
        check(e.getMessage().equals("resolution exception"), "fixed message 'resolution exception'");
        check(new SPIResolutionException(-1).id() == -1, "negative ids carried");
        // the id survives serialization (no declared serialVersionUID)
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bos)) { oo.writeObject(e); }
        try (java.io.ObjectInputStream oi = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            SPIResolutionException back = (SPIResolutionException) oi.readObject();
            check(back.id() == 42, "id survives serialization");
            check(back.getMessage().equals("resolution exception"), "message survives serialization");
        }
        // it is the engine-side twin of ExecutionControl.ResolutionException
        Class<?> re = Class.forName("jdk.jshell.spi.ExecutionControl$ResolutionException");
        check(!re.isAssignableFrom(SPIResolutionException.class), "ResolutionException and SPIResolutionException are unrelated types");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
