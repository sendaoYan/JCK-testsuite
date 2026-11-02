/*
 * @test
 * @summary java.util.ServiceLoader.Provider conformance: interface and type/get via ServiceLoader.
 * @run main ProviderTest
 */
import java.nio.file.spi.FileSystemProvider;
import java.util.Optional;
import java.util.ServiceLoader;

public class ProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(ServiceLoader.Provider.class.isInterface(), "is interface");
        ServiceLoader<FileSystemProvider> loader = ServiceLoader.load(FileSystemProvider.class);
        Optional<ServiceLoader.Provider<FileSystemProvider>> op = loader.stream().findFirst();
        check(op.isPresent(), "provider present");
        check(op.get().type() != null, "type non-null");
        check(op.get().get() != null, "get non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
