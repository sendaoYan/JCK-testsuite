# java.util.Set — 测试设计案

## 1. API 功能
不重复元素集合基接口。继承 Collection。JDK-27 预览新增 `ofLazy`。

- static：of/copyOf（以及预览 ofLazy）。

## 2. Java 用户如何使用
`Set.of("a","b")` 不可变工厂、`copyOf` 拷贝、`ofLazy(keys, pred)` 惰性过滤。
链路：of -> 重复元素 IAE -> copyOf -> ofLazy。

## 3. 开源代码实际用法调研
- OpenJDK `Set`（本地 `~/git/jdk25u-dev`）：of 拒绝重复元素（IAE）与 null（NPE）——
  第 1/2/4 节对齐；JDK-27 ofLazy 需 `--enable-preview`。
- 官方测试 `test/jdk/java/util/Set/`.

## 4. 测试用例设计 + 自动化实现（SetTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| of 工厂 | 空/多元素、重复 IAE、null NPE |
| copyOf | 拷贝内容 |
| ofLazy | 惰性过滤 |

共 3 组场景、约 7 个 check（`testBlackBox()`）。

---

## 契约深化增补（2026-11-05，只追加不改上文）

### 新增测试方法
- `testImmutableFactories`（JDK-9+）— Set.of 全矩阵：of()/of(E)/多元素；**六条写通道全 UOE**
  （add/remove/addAll/removeAll/retainAll/clear）+ iterator.remove UOE；of(null 元素) NPE、
  **of((Object[])null) NPE**、copyOf(null) NPE；copyOf 内容相等且**与后续源变更隔离**、
  结果不可变；of 与 HashSet 参与统一 equals 契约；hashCode 符合求和公式。
- `testEqualsHashCodeContract` — 跨实现相等矩阵（HashSet/LinkedHashSet/TreeSet/Set.of）；
  hashCode=Σ元素hash 手算锁定（294/6）；null 贡献 0；空集 0；Set 永不 equals List。
- `testNullElementSemantics` — HashSet null 全通；TreeSet add(null) NPE。
- `testToArrayContract` — 复用+null 终止/恰好/新分配/toArray(null) NPE。
- `testBulkEdgeSemantics` / `testIteratorWriteChannel` — 返回值矩阵 + equals 匹配 + 迭代器写通道
  （remove 合法一次、二次 ISE、耗尽 NSEE）。

### 负例精确异常表
| 操作 | 异常 |
|---|---|
| Set.of 任意写操作（add/remove/…/clear/iterator.remove） | UnsupportedOperationException |
| Set.of("a", null) / Set.of((Object[])null) / Set.copyOf(null) | NullPointerException |
| Set.of("a","a") | IllegalArgumentException |
| TreeSet 自然序 add(null) | NullPointerException |
| toArray(null) | NullPointerException |
| iterator 连续第二次 remove / 耗尽后 next | IllegalStateException / NoSuchElementException |

### JDK-8 vs JDK-27 差异（本类）
- 27 侧有 Set.of(0..10 元素固定重载 + varargs)/copyOf（JDK-9+）与预览 ofLazy；8 侧无工厂。
