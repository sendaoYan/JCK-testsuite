# java.util.StringJoiner 测试设计

## 1. API 功能
- 构造器：`StringJoiner(CharSequence delimiter)`、`(delimiter, prefix, suffix)`。
- 追加：`add(CharSequence)`（链式）、`merge(StringJoiner)`。
- 空值：`setEmptyValue(CharSequence)`。
- 查询：`length()`、`toString()`。

## 2. Java 用户如何使用
- 拼接：`new StringJoiner(",").add("a").add("b")`。
- 带括号：`new StringJoiner(",", "[", "]")`。
- 合并：`joiner.merge(other)`。
- 空集合默认值：`setEmptyValue("none")`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/io/FilePermission.java`：`StringJoiner` 拼接权限 actions。
- JDK 源码 `java/io/ObjectOutputStream.java`：拼接字段描述。
- JDK 源码 `java/net/SocketPermission.java`：`StringJoiner` 拼接 actions。
- 广泛用法：`String.join` 内部委托 `StringJoiner`。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言 final 类、关键方法存在。
- 黑盒：分隔符拼接、prefix/suffix、length、setEmptyValue（含带前后缀空串忽略空值）、
  merge（追加元素、返回接收者、空 joiner 保留 emptyValue）。

## 5. 契约深化（第二轮追加，映射到新增方法）

| 测试点 | 用例方法 |
|---|---|
| merge 跨分隔符：other 的已拼内容（其内部分隔符、去 prefix/suffix）作为 this 的下一个单一元素；other 不被修改 | testMergeDifferentDelimiters |
| 空 this merge 非空 other：this 变非空、this 的 prefix/suffix、emptyValue 失效 | testMergeDifferentDelimiters |
| other 的 emptyValue 与 merge 无关；连续 merge 累加 | testMergeDifferentDelimiters |
| length() 恒等于 toString().length()（各状态） | testLengthAlwaysConsistent |
| 已有元素后 setEmptyValue 被忽略 | testSetEmptyValueAfterElements |
| add 链式/null 渲染/单元素/空前缀后缀/100 元素 | testAddContracts |
| toString 幂等 | testToStringIdempotent |

### 易错细节

- merge 结果 `"[0,1;2]"`：保留 other 内部分隔符、去 other prefix/suffix；other 不变。
- 空无前缀后缀 joiner 空态是 `""`；`add("")` 使其非空但内容为 `""`。
