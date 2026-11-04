# java.util.Comparator 测试设计

## 1. API 功能
- 核心：`compare(T,T)`。
- 静态工厂：`naturalOrder`、`reverseOrder`、`nullsFirst`、`nullsLast`、`comparing`、`comparingInt/Long/Double`。
- 组合子：`reversed`、`thenComparing`、`thenComparingInt/Long/Double`。
- 便捷：`min`、`max`。

## 2. Java 用户如何使用
- 排序：`list.sort(Comparator.comparing(Person::getName))`。
- 多级排序：`comparingInt(...).thenComparing(...)`。
- 空值排序：`Comparator.nullsFirst(naturalOrder())`。
- 取极值：`natural.max(a, b)`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/util/Comparators.java`：包私有 `NullComparator` 支持 `nullsFirst`/`nullsLast`。
- JDK 源码 `java/util/Collections.java`：`sort`/`binarySearch` 依赖 Comparator。
- JDK 自带测试 `test/jdk/java/util/Comparator/BasicTest.java`：工厂与组合子。
- 广泛用法（Spring/Guava）：流排序、TreeMap/TreeSet 自定义比较器。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言接口与关键方法存在。
- 黑盒：
  - naturalOrder/reverseOrder/reversed 符号。
  - comparing/comparingInt/comparing(带显式比较器)/thenComparing。
  - nullsFirst/nullsLast（null 与委托路径）。
  - min/max、thenComparingInt/comparingLong/comparingDouble。

## 第 2 轮深化（2026-11，契约覆盖，37→204 断言）

新增方法：testSgnContract、testThenComparingChain、testSortStabilityUnderComparator、
testNullsCompositions、testExceptionPropagation、testEqualsContract、
testPrimitiveSpecializationBoundaries、testMinMaxContract、testThenComparingPrimitiveNulls、
testDefaultMethodReflection。

### 本轮锁定的易错规范细节（先探测后断言，jdk-27.0.1 实测）

- **JDK-21+ `min`/`max` 相等参数身份语义**：`compare(a,b)==0` 时返回**第一个参数**（用 `==` 引用判定，
  非仅 equals）；`reverseOrder().min(1,2)==2`（min 随比较器翻转）；全零比较器 min/max 均返回首参；
  `max(null,"a")`/`min("a",null)` NPE。
- **min/max 为 default 方法**（反射非 abstract）；`reversed` 亦 default。
- **sgn 契约矩阵**：三比较器全组合反对称 + 传递性；naturalOrder/reverseOrder 精确互反。
- **3 级链排序**：`[b2,a2,c1,a1]` → `[a1,a2,b2,c1]`；整链 reversed 反号；reversed() 不改原比较器。
- **异常同实例透传**：`e == boom`（comparing key extractor 与 thenComparing 二级比较器两条路径）；
  主键分出胜负则二级比较器不被调用。
- **equals 语义**：comparing(fn)/nullsFirst(cmp) 身份 equals；naturalOrder()/reverseOrder() 缓存单例；
  `naturalOrder().reversed().equals(reverseOrder())`。
- **nullsFirst(reverseOrder()) TreeSet** → `[null, b, a]`；`nullsFirst(...).reversed()` 连 null 次序翻转；
  `nullsFirst(null,null)==0` 不调用委托。
- **thenComparingInt/Long/Double(null) NPE**、comparingInt/Long/Double(null) NPE、thenComparing(fn, null cmp) NPE。
- **comparingDouble 全序**：NaN>+Inf、-0.0<+0.0；comparingLong MIN/MAX 边界。
- TimSort 契约违规 IAE 刻意不断言。

## 第 2 轮健壮性

全部固定输入；无随机/墙钟；headless 无 `@key`。
