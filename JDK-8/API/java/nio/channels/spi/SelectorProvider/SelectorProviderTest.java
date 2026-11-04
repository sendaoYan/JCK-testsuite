/*
 * @test
 * @summary java.nio.channels.spi.SelectorProvider conformance: provider/open*.
 * @run main SelectorProviderTest
 */

import java.lang.reflect.Modifier;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;

public class SelectorProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSelectorProvider() throws Exception {
        check(Modifier.isAbstract(SelectorProvider.class.getModifiers()), "abstract");
        SelectorProvider provider = SelectorProvider.provider();
        check(provider != null, "provider() non-null");

        AbstractSelector sel = provider.openSelector();
        check(sel.isOpen(), "openSelector");
        sel.close();

        ServerSocketChannel ssc = provider.openServerSocketChannel();
        check(ssc.isOpen(), "openServerSocketChannel");
        ssc.close();

        SocketChannel sc = provider.openSocketChannel();
        check(sc.isOpen(), "openSocketChannel");
        sc.close();

        DatagramChannel dc = provider.openDatagramChannel();
        check(dc.isOpen(), "openDatagramChannel");
        dc.close();

        Pipe pipe = provider.openPipe();
        check(pipe.sink() != null && pipe.source() != null, "openPipe");
        pipe.sink().close();
        pipe.source().close();
    }

    public static void main(String[] args) throws Exception {
        testSelectorProvider();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
