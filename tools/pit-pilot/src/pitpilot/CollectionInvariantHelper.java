package pitpilot;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 测试辅助「等价逻辑」：集合不变量校验器。
 * 把 java.util.Collection/Set 的去重、空判断、交集等关系抽成纯逻辑，
 * 供行为测试复用，并作为 PIT 的被测类自检这些辅助断言是否写实。
 */
public final class CollectionInvariantHelper {

    private CollectionInvariantHelper() {
    }

    /** 判断 items 是否两两互异（无重复元素）。 */
    public static <T> boolean allDistinct(List<T> items) {
        Set<T> seen = new HashSet<>();
        for (T item : items) {
            if (!seen.add(item)) {
                return false;
            }
        }
        return true;
    }

    /** 判断 set 是否为 list 的去重结果（集合相等）。 */
    public static <T> boolean isDedupOf(List<T> list, Set<T> set) {
        return new HashSet<>(list).equals(set);
    }

    /** 判断集合是否为空（null 也视为空）。 */
    public static boolean isEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    /** 判断 a 与 b 的交集是否等于 expected。 */
    public static <T> boolean intersectionEquals(Collection<T> a, Collection<T> b, Set<T> expected) {
        Set<T> inter = new HashSet<>(a);
        inter.retainAll(b);
        return inter.equals(expected);
    }
}
