package pitpilot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 测试辅助「等价逻辑」：集合排序不变量校验器。
 * 行为测试常用「排序后有序 + 排序是原集合的置换」两条不变量来验证 sort 系列。
 */
public final class SortInvariantVerifier {

    private SortInvariantVerifier() {
    }

    /** 校验自然序列表严格非降（允许相邻相等）。 */
    public static <T extends Comparable<? super T>> boolean isSorted(List<T> list) {
        if (list == null) {
            throw new IllegalArgumentException("list must not be null");
        }
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i).compareTo(list.get(i + 1)) > 0) {
                return false;
            }
        }
        return true;
    }

    /** 校验两个列表互为置换（排序后相等）。 */
    public static <T extends Comparable<? super T>> boolean isPermutation(List<T> a, List<T> b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.size() != b.size()) {
            return false;
        }
        List<T> sa = new ArrayList<>(a);
        List<T> sb = new ArrayList<>(b);
        Collections.sort(sa);
        Collections.sort(sb);
        return sa.equals(sb);
    }

    public static boolean isEmptyOrSingleton(List<?> list) {
        return list == null || list.size() <= 1;
    }
}
