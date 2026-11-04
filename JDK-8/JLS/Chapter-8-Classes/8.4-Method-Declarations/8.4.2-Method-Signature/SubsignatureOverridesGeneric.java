/*
 * @test
 * @summary JLS 8.4.2 (Java SE 8): a signature whose erasure equals that of a generic method
 *          is a subsignature, so a non-generic method can override a generified
 *          method.
 * @run main SubsignatureOverridesGeneric
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
class CollectionConverter {
    <T> List<T> toList(Collection<T> c) { return new ArrayList<T>(c); }
}
public class SubsignatureOverridesGeneric extends CollectionConverter {
    @Override
    @SuppressWarnings({"rawtypes","unchecked"})
    List toList(Collection c) { return super.toList(c); }
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) {
        Collection<String> c = Arrays.asList("a", "b");
        check(new SubsignatureOverridesGeneric().toList(c).size() == 2, "raw method overrides generic method via erasure subsignature");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
