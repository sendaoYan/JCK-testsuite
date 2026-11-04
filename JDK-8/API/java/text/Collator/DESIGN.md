# java.text.Collator 测试设计案（JDK-8）

> 覆盖目标：完整契约（常量、比较、强度、排序键、分解、equals/clone、子类、非法参数异常），非冒烟。

## 1. API 提供了哪些功能

- 常量：强度 `PRIMARY=0`/`SECONDARY=1`/`TERTIARY=2`/`IDENTICAL=3`；分解 `NO_DECOMPOSITION=0`/
  `CANONICAL_DECOMPOSITION=1`/`FULL_DECOMPOSITION=2`。
- 工厂：`getInstance()`、`getInstance(Locale)`、`getAvailableLocales()`。
- 比较：抽象 `compare(String,String)`、`compare(Object,Object)`（实现 `Comparator<Object>`）、
  `equals(String,String)`。
- 排序键：抽象 `getCollationKey(String)`。
- 属性：`get/setStrength`、`get/setDecomposition`。
- 契约：`clone`、`equals(Object)`、抽象 `hashCode`；实现 `Cloneable`；**非 `Serializable`**。

## 2. Java 用户如何使用

- `Collator.getInstance(Locale.US)` 获得按 locale 排序比较器 → `compare` → `setStrength` 控制敏感度 →
  `getCollationKey` 生成可反复比较排序键。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/Collator.java`。
- JDK-27 同目录 DESIGN.md（JDK-8 实测：默认 strength=TERTIARY、默认 decomposition=NO_DECOMPOSITION）。

## 4. 测试用例设计（映射到 CollatorTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：abstract、Comparator、Cloneable、7 个常量、工厂非空 | testWhiteBox |
| P0 | compare 三态 + equals(String,String) + compare(Object,Object) + 空串 + null→NPE | testCompare |
| P0 | strength TERTIARY/PRIMARY/SECONDARY/IDENTICAL 大小写/重音敏感 | testStrength |
| P0 | getCollationKey 相等/排序/空串边界 | testCollationKey |
| P0 | decomposition 三态 + equals/clone/hashCode + 非法 strength/decomposition→IAE + getInstance(null)→NPE | testDecompositionAndContract |
| P0 | 受保护构造器经子类 | testSubclass |

## 关键规范细节（易错，已锁入断言）

- `setStrength`/`setDecomposition` 非法值抛 `IllegalArgumentException`。
- `Collator` 实现 `Cloneable` 但**不实现 `Serializable`**（不能序列化断言）。
- 默认 strength=TERTIARY（大小写敏感）、默认 decomposition=NO_DECOMPOSITION。

## 注意事项

- 固定 Locale.US；无默认 Locale/墙钟/随机；headless-safe 不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- Strength 矩阵（PRIMARY 忽略重音+大小写/SECONDARY 区分重音/TERTIARY 区分大小写/IDENTICAL 码点敏感，é 预组 vs e+U+0301 在 IDENTICAL+NO_DECOMPOSITION 下 !=0（实测 132），+CANONICAL 下 ==0）
- 默认 TERTIARY/NO_DECOMPOSITION；decomposition 可切换（CANONICAL 折叠组合序列）
- getCollationKey 与 compare 一致性 + toByteArray 确定性；getCollationKey(null)→null
- compare(null,·)/getInstance(null)→NPE；equals 含 strength；getAvailableLocales
