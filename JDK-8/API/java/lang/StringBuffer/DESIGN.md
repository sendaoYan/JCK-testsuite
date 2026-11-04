# java.lang.StringBuffer 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「append 拼串」的冒烟。重点补：白盒结构（final、非接口、
> 继承 AbstractStringBuilder、实现 Appendable/CharSequence/Serializable、方法 synchronized —— 与
> StringBuilder 的唯一本质区别）、构造器（默认 16 / String 16+len / int / CharSequence / 容量 0 合法）、
> 负容量 NASBE、null 边界（构造与 append(String/Object/CharSequence) 转 "null"、append(char[]) NPE）、
> append 全重载 + appendCodePoint 补充平面与 IAE、长度/charAt/setCharAt/setLength/getChars、
> 异常矩阵（SIOOBE vs IOOBE、delete/replace 越界 clamp）、insert/delete/deleteCharAt/replace、
> reverse/substring/subSequence/indexOf/lastIndexOf（含 fromIndex）、capacity/ensureCapacity/trimToSize、
> streams、边界等价。断言确定，headless 安全。

## 1. API 提供了哪些功能

- **类**：`java.lang.StringBuffer`（final、非 abstract、继承 `AbstractStringBuilder`、
  implements `Appendable`/`CharSequence`/`Serializable`；公开方法 **synchronized** —— 线程安全）。
- **构造器**：`()`（容量 16）、`(String)`（容量 16+len）、`(int capacity)`、`(CharSequence)`。
- **追加**：`append`（全重载）、`appendCodePoint(int)`。
- **修改**：`insert`（多重载）、`delete`/`deleteCharAt`/`replace`/`setCharAt`/`setLength`/`reverse`。
- **查询**：`length`/`charAt`/`getChars`/`substring`/`subSequence`/`indexOf`/`lastIndexOf`/
  `codePoint*`/`toString`。
- **容量**：`capacity`/`ensureCapacity`/`trimToSize`；流：`chars`/`codePoints`。

## 2. Java 用户如何使用

- 多线程共享可变字符串用 `StringBuffer`（synchronized）；单线程拼串用 `StringBuilder`。
- `append(null)` 字符串参数等价于追加 `"null"`；`appendCodePoint(0x1D518)` 追加一对代理项。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/StringBuffer.html`）：构造器、append null 语义、同步性。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/StringBuffer.java`。
- **实测 JDK-8 运行时**（Temurin 8u372）：`insert` 越界抛 `ArrayIndexOutOfBoundsException`（JDK-27 抛
  `StringIndexOutOfBoundsException`，统一断言父类 IndexOutOfBoundsException）。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/StringBuffer`。

## 4. 测试用例设计（映射到 StringBufferTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器：默认 16 / String 16+len / int / CharSequence / 容量 0 合法 | testConstructors |
| P0 | 构造器异常：负容量 NASBE、String/CharSequence null NPE | testConstructorExceptions |
| P0 | append 全重载返回 this、混合拼接 | testAppend |
| P0 | append null 语义矩阵（→"null" vs char[] NPE、切片越界 IOOBE） | testAppendNullSemantics |
| P0 | appendCodePoint：补充平面、BMP、非法码点 IAE | testAppendCodePoint |
| P0 | length/charAt/setCharAt/setLength/getChars | testLengthChars |
| P0 | 异常矩阵：SIOOBE/IOOBE、delete/replace 越界 clamp、indexOf/insert null NPE | testExceptionMatrix |
| P0 | insert/delete/deleteCharAt/replace 行为 + 越界 IOOBE | testInsertDelete |
| P0 | insert 全重载 + null→"null" | testInsertOverloads |
| P0 | reverse/substring/subSequence/indexOf/lastIndexOf | testReverseSubstringIndex |
| P0 | indexOf/lastIndexOf 含 fromIndex | testIndexOfFromIndex |
| P0 | ensureCapacity/trimToSize | testCapacity |
| P0 | chars/codePoints 流 | testStreams |
| P0 | 方法 synchronized（反射 Modifier.isSynchronized） | testSynchronized |
| P1 | 边界等价：reverse 对合、setLength(0) 保容量、自动扩容、toString 往返 | testBoundaryEquivalence |
| P0 | 白盒：final、继承 AbstractStringBuilder、实现三接口 | testWhiteBox |

## 关键规范细节（易错，已锁入断言）

- **`new StringBuffer()` 容量 16；`new StringBuffer(String)` 容量 16+len**。
- **`append((String)null)`/`append((Object)null)`/`append((CharSequence)null)` → `"null"`**；
  **`append((char[])null)` 抛 NPE**；`append((CharSequence)null,start,end)` 把 null 当 `"null"` 再切片。
- **`appendCodePoint` 非 BMP 码点长度 +2**；非法码点抛 `IllegalArgumentException`。
- **`delete`/`replace` 的 end 越界被 clamp**（不抛）；start>end 抛 SIOOBE。
- **`insert` 越界在 JDK-8 抛 `ArrayIndexOutOfBoundsException`**（JDK-27 抛 StringIndexOutOfBoundsException，
  统一断言父类）。
- **StringBuffer 公开方法 synchronized**（对比 StringBuilder 非同步）；final、继承 AbstractStringBuilder。

## 注意事项

- headless 安全，不加 `@key`；结果确定；不做真正多线程竞争（只断言方法含 synchronized 修饰符）。
