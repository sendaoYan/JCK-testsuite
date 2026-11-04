/*
 * @test
 * @summary java.beans.Encoder conformance: exception listener + persistence delegate.
 * @run main EncoderTest
 */

import java.beans.*;
public class EncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Encoder enc = new Encoder();
        enc.setExceptionListener(new ExceptionListener() {
            public void exceptionThrown(Exception e) {}
        });
        check(enc.getExceptionListener() != null, "exception listener");
        PersistenceDelegate pd = new DefaultPersistenceDelegate();
        enc.setPersistenceDelegate(String.class, pd);
        check(enc.getPersistenceDelegate(String.class) == pd, "persistence delegate");
        enc.writeStatement(new Statement("x", "toString", new Object[0]));
        System.out.println("PASS (" + passed + " assertions)");
    }
}
