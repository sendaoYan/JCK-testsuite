# java.util.StringJoiner 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。纯内存对象，无全局副作用。

## 1. API 功能

- **构造器**：`StringJoiner(CharSequence)`、`StringJoiner(CharSequence, CharSequence, CharSequence)`。
- **元素**：`add(CharSequence)`、`merge(StringJoiner)`。
- **空值**：`setEmptyValue(CharSequence)`。
- **查询**：`length()`、`toString()`。

## 2. 使用方式

`new StringJoiner(",", "[", "]").add(a).add(b)` → `[a,b]`；空时用 `setEmptyValue` 覆盖默认 `prefix+suffix`。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/StringJoiner`。
- JDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/StringJoiner.java`：
  delimiter/prefix/suffix null → NPE；`add(null)` 追加字面量 "null"（不 NPE）；`setEmptyValue(null)`/`merge(null)` → NPE。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | delimiter/prefix/suffix/setEmptyValue | testDelimiter / testPrefixSuffix / testEmptyValue |
| P0 | 构造器 null NPE 矩阵 | testConstructorNull |
| P0 | add(null)→"null" + add 链式 + 空元素 | testAddNullAndEmpty |
| P0 | setEmptyValue(null) NPE | testSetEmptyValueNull |
| P0 | merge(null) NPE + 非空/空 other 语义 | testMerge |
| P1 | length 空值/prefix+suffix 计数 | testLengthAndEmpty |

## 关键规范细节

- `add(null)` 追加字面量 `"null"`；`add("")` 使 joiner 变为非空（空元素）。
- 空 joiner `toString()` = `prefix+suffix`（或 `emptyValue`）；`length()` 等价 `toString().length()`。
- 构造器 delimiter/prefix/suffix、`setEmptyValue(null)`、`merge(null)` → NPE。

## 注意事项

纯内存；无全局副作用；headless-safe 不加 `@key`。

## 5. 契约深化（第二轮追加，映射到新增方法）

| 测试点 | 用例方法 |
|---|---|
| merge 跨分隔符：other 的已拼内容（其内部分隔符、去 prefix/suffix）作为 this 的下一个单一元素；other 不被修改 | testMergeDifferentDelimiters |
| 空 this merge 非空 other：this 变非空、采用 this 的 prefix/suffix、emptyValue 失效 | testMergeDifferentDelimiters |
| other 的 emptyValue 与 merge 无关（只看是否当前有元素）；连续 merge 累加 | testMergeDifferentDelimiters |
| length() 恒等于 toString().length()（空/emptyValue/有元素/add(null) 各态） | testLengthAlwaysConsistent |
| 已有元素后 setEmptyValue 不影响 toString/length | testSetEmptyValueAfterElements |
| add 链式/null 渲染 "null"/单元素无分隔符/空前缀后缀/100 元素精确 | testAddContracts |
| toString 幂等 | testToStringIdempotent |

### 易错细节

- `"[0,1;2]"`：merge 保留 other 的内部分隔符（";"），并丢弃 other 的 prefix/suffix；
  other 本身不变（仍可继续用）。
- 空无前缀后缀 joiner 的空态是 `""`（length 0）；`add("")` 使其非空但内容仍为 `""`。
