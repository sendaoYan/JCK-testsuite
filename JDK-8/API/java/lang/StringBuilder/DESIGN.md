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

## 契约深化（2026-11）

### API 功能（本轮新增覆盖）
- append 全家精确值矩阵：boolean/char/int/long/float/double（含 `Integer.MIN_VALUE`/`Long.MIN_VALUE` 十进制全等）/String/char[] 全量与区间/CharSequence（String、StringBuilder、StringBuffer、自定义 WrapCs 四种来源 + (start,end) 切片）/`append(StringBuffer)` 跨类型/自追加（`sb.append(sb)` 内容翻倍）/`appendCodePoint(0/0xFFFF/0x10FFFF/0x1D11E)`。
- insert 全家（0/中间/length 合法位 + 每个重载返回 this + null 三形态与 null CharSequence 切片）。
- delete/deleteCharAt/replace（区间替换/增长/缩短/空串=delete/端点形态/返回 this）、setCharAt/setLength（增长补 `'\u0000'`、缩短丢尾、append 落点在 NUL 之后）。
- reverse 矩阵：ASCII/回文/空串/单字符/代理对按单字符整体成对反转（`"a𝄞b"` → `"b𝄞a"`，codePointCount 保持 3）/孤立代理作为普通单元/对合。
- codePoint 家族：codePointAt/codePointBefore/codePointCount/offsetByCodePoints 在代理对上的全部边界值。
- substring/subSequence（subSequence 返回 String 类实例、每次新 String）、indexOf/lastIndexOf（空串 0/len、fromIndex 越界 clamp、lastIndexOf 负 from、代理对 needle、大小写敏感）。
- toString 快照独立性（取串后 append/setCharAt 不影响快照、两次 toString 相等但不同实例）、`String.contentEquals`。
- equals/hashCode 未覆写（同内容两实例 equals false，toString().equals 桥接对比）、CharSequence 多态视图 + 接口 default chars()/codePoints()。
- capacity 契约：`()=16`、`(String)/(CharSequence)=str.length()+16`、`(0)` 合法、ensureCapacity 负/0/小于当前不改、大于则增长、append 后 capacity>=length 弱断言、trimToSize 后 capacity==length 且幂等。

### 使用方式
- 同前：链式 append/insert；新增通过 `expectExact`（异常类名+消息逐字断言）与 `MODERN` 常量把 JDK-8 与 JDK-27 的差异编码进同一份源文件。

### 参考来源
- Oracle javadoc StringBuilder/CharSequence/Appendable；JDK-8 与 JDK-27 双侧实测（`java.lang.StringBuilder` javap 枚举 + 每个负例在两侧运行锁定异常类与消息文本）。

### 测试用例设计 + 易错规范细节（本轮锁定）
- **`append((CharSequence)null, s, e)` 规范特例**：null 被当 `"null"` 再按区间切片 —— `(null,1,4)→"ull"`、`(null,0,4)→"null"`、`(null,4,4)→""`。
- **`append((StringBuffer)null)` → 追加 `"null"`**；`append((char[])null)` → NPE（数组与对象语义分叉）。
- **自追加**：`new StringBuilder("ab").append(自身)` → `"abab"`（按当前字符序列内容复制，无自引用无限循环）。
- **reverse 保留代理对**：JDK-8 与 JDK-27 一致，成对作为单字符整体移动（规范行为，非交换高低代理）。
- **appendCodePoint 非法码点**：两侧均 `IllegalArgumentException`；JDK-8 消息 null，JDK-27 消息 `Not a valid Unicode code point: 0x110000`/`0xFFFFFFFF`。
- **`append(char[],off,len)` 越界**：JDK-8 抛 `ArrayIndexOutOfBoundsException`（消息 null）；JDK-27 抛**纯** `IndexOutOfBoundsException`，消息 `Range [a, b) out of bounds for length n` —— 实测锁定，两侧不同。
- **`insert(off,char)` 越界**：JDK-8 `ArrayIndexOutOfBoundsException`（null 消息）；JDK-27 `StringIndexOutOfBoundsException`（Range 消息）。其余 insert 重载越界两侧均 SIOOBE（消息格式不同：8 `String index out of range: n`，27 `Range [n, len) ...`）。
- **`insert(off, csq, s, e)` 越界**：JDK-8 纯 `IndexOutOfBoundsException` 消息 `dstOffset 3`；JDK-27 SIOOBE Range 消息 —— 独有差异形态。
- **`setLength(-1)`/`setLength(MIN_VALUE)` 消息两侧逐字相同**（`String index out of range: -1` / `-2147483648`），无 MODERN 分支直接锁定。
- **`offsetByCodePoints` 越界**：两侧均为纯 `IndexOutOfBoundsException` 且消息 null —— 直接锁定。
- **`delete(2,1)`（JDK-8）消息为 null**、`replace(2,1,..)` JDK-8 消息 `start > end`、`getChars(2,1,..)` JDK-8 消息 `srcBegin > srcEnd`；JDK-27 统一为 Range 消息。
- **substring(4)（len+1）JDK-8 消息是 `String index out of range: -1`**（begin-length 差值），JDK-27 是 `Range [4, 3) out of bounds for length 3`。
- **`new StringBuilder(-1)`**：两侧 `NegativeArraySizeException`；JDK-8 消息 null，JDK-27 消息 `-1`。
- **capacity==0 构造器合法**；`trimToSize` 后 capacity==length（两侧一致已锁）；增长策略只锁 `capacity >= length` 弱不变量。
