# java.lang.StringBuilder 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「append 拼串」的冒烟。重点补：白盒结构（final、非接口、
> 继承 AbstractStringBuilder、实现 Appendable/CharSequence/Serializable、方法非 synchronized）、
> 构造器（默认 16 / String 16+len / int 容量 / CharSequence / 容量 0 合法）、负容量 NASBE、
> null 边界（String/CharSequence 构造与 append(String/Object/CharSequence) 转 "null"、append(char[]) NPE）、
> append 全重载 + appendCodePoint 补充平面与 IAE、长度/charAt/setCharAt/setLength/getChars、
> 异常矩阵（SIOOBE vs IOOBE、delete/replace 越界 clamp）、insert/delete/deleteCharAt/replace、
> reverse/substring/subSequence/indexOf/lastIndexOf（含 fromIndex）、capacity/ensureCapacity/trimToSize、
> streams（chars/codePoints）、边界等价（reverse 对合、setLength(0) 保容量、自动扩容、toString 往返）。
> 断言确定，headless 安全，无同步依赖。

## 1. API 提供了哪些功能

- **类**：`java.lang.StringBuilder`（final、非 abstract、继承 `AbstractStringBuilder`、
  implements `Appendable`/`CharSequence`/`Serializable`；方法**非** synchronized —— 与 StringBuffer 的唯一本质区别）。
- **构造器**：`()`（容量 16）、`(String)`（容量 16+len）、`(int capacity)`、`(CharSequence)`。
- **追加**：`append`（boolean/char/char[]/char[],off,len/double/float/int/long/Object/String/StringBuffer/
  CharSequence/CharSequence,start,end）、`appendCodePoint(int)`。
- **修改**：`insert`（多重载）、`delete`/`deleteCharAt`/`replace`/`setCharAt`/`setLength`/`reverse`。
- **查询**：`length`/`charAt`/`getChars`/`substring`/`subSequence`/`indexOf`/`lastIndexOf`/
  `codePointAt`/`codePointBefore`/`codePointCount`/`offsetByCodePoints`/`toString`。
- **容量**：`capacity`/`ensureCapacity`/`trimToSize`；流：`chars`/`codePoints`（CharSequence 默认方法）。

## 2. Java 用户如何使用

- 循环拼接字符串用 `StringBuilder`（非同步、快）：`new StringBuilder().append(a).append(b).toString()`。
- `append(null)` 字符串参数等价于追加 `"null"`；`appendCodePoint(0x1D518)` 追加一对代理项。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/StringBuilder.html`）：构造器、append null 语义、容量规则。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/StringBuilder.java`
  （与 `AbstractStringBuilder.java`）。
- **实测 JDK-8 运行时**（Temurin 8u372）：`insert` 越界抛 `ArrayIndexOutOfBoundsException`（JDK-27 抛
  `StringIndexOutOfBoundsException`，两者都是 `IndexOutOfBoundsException` 子类，测试统一断言父类）。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/StringBuilder`。

## 4. 测试用例设计（映射到 StringBuilderTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器：默认 16 / String 16+len / int / CharSequence / 容量 0 合法 | testConstructors |
| P0 | 构造器异常：负容量 NASBE、String/CharSequence null NPE | testConstructorExceptions |
| P0 | append 全重载返回 this、混合拼接 | testAppend |
| P0 | append null 语义矩阵（String/Object/CharSequence→"null"、char[]→NPE、切片越界 IOOBE） | testAppendNullSemantics |
| P0 | appendCodePoint：补充平面、BMP、非法码点 IAE | testAppendCodePoint |
| P0 | length/charAt/setCharAt/setLength/getChars（截断/NUL 填充） | testLengthChars |
| P0 | 异常矩阵：SIOOBE/IOOBE、delete/replace 越界 clamp、indexOf/insert null NPE | testExceptionMatrix |
| P0 | insert/delete/deleteCharAt/replace 行为 + 越界 IOOBE | testInsertDelete |
| P0 | insert 全重载 + null→"null" | testInsertOverloads |
| P0 | reverse/substring/subSequence/indexOf/lastIndexOf | testReverseSubstringIndex |
| P0 | indexOf/lastIndexOf 含 fromIndex | testIndexOfFromIndex |
| P0 | ensureCapacity/trimToSize | testCapacity |
| P0 | chars/codePoints 流 | testStreams |
| P0 | 方法非 synchronized（反射 Modifier.isSynchronized） | testNotSynchronized |
| P1 | 边界等价：reverse 对合、setLength(0) 保容量、自动扩容、toString 往返 | testBoundaryEquivalence |
| P0 | 白盒：final、非接口、继承 AbstractStringBuilder、实现三接口 | testWhiteBox |

## 关键规范细节（易错，已锁入断言）

- **`new StringBuilder()` 容量 16；`new StringBuilder(String)` 容量 16+len**。
- **`append((String)null)`/`append((Object)null)`/`append((CharSequence)null)` → 追加 `"null"`**；
  但 **`append((char[])null)` 抛 NPE**；`append((CharSequence)null,start,end)` 把 null 当 `"null"` 再切片。
- **`appendCodePoint` 非 BMP 码点长度 +2**；`< MIN_CODE_POINT` 或 `> MAX_CODE_POINT` 抛 `IllegalArgumentException`。
- **`delete`/`replace` 的 end 越界被 clamp 到 length**（不抛）；start>end 抛 SIOOBE。
- **`insert` 越界在 JDK-8 抛 `ArrayIndexOutOfBoundsException`**（JDK-27 抛 StringIndexOutOfBoundsException，
  统一断言父类 IndexOutOfBoundsException）。
- **StringBuilder 方法非 synchronized**（对比 StringBuffer 全 synchronized）；final、继承 AbstractStringBuilder。

## 注意事项

- headless 安全，不加 `@key`；结果确定；不涉及线程竞争。
