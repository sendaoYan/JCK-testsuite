# NumberFormatException 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「catch 一下」的冒烟。重点补：层次链（IllegalArgumentException/
> RuntimeException/Exception/Throwable/Serializable）与父类断言、恰好 2 个声明构造器
> （无 `(String,Throwable)` 形态）、Throwable 通用契约（initCause 一次/二次 IllegalStateException、
> addSuppressed/getSuppressed、getStackTrace、fillInStackTrace、toString 逐字）、消息透传
> （unicode/长文本）、以及 parse 家族（Integer/Long/Short/Byte 的 parseInt/parseLong/parseShort/
> parseByte/valueOf/decode/parseUnsignedInt）NFE 消息的逐字锁定。断言确定，headless 安全。

## 1. API 提供了哪些功能

- **类**：`java.lang.NumberFormatException`（unchecked，`extends IllegalArgumentException`）。
- **构造器**：`NumberFormatException()`（null 消息）、`NumberFormatException(String)`（消息透传）。
  仅这 2 个（javap 双侧实证；无 `(String,Throwable)` 形态）。
- **无自有方法**：全部行为继承 `Throwable`（getMessage/getLocalizedMessage/getCause/initCause/
  addSuppressed/getSuppressed/getStackTrace/printStackTrace/fillInStackTrace/toString）。

## 2. Java 用户如何使用

- 捕获诊断：`try { Integer.parseInt(s); } catch (NumberFormatException e) { ... }`——消息含被拒串。
- 主动抛出：`throw new NumberFormatException("reason")` 上报解析失败；可后补 `initCause`。
- 层次定位：`instanceof IllegalArgumentException` 成立，可被统一的 illegal-argument 处理捕获。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/NumberFormatException.html`）：层次、2 构造器。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/NumberFormatException.java`
  与 `Integer.java` 的 `throwFor`/`forInputString` 消息拼装。
- **实测 JDK-8 运行时**（Temurin 8u372）：全部期望消息逐字探测后写入
  （`For input string: "z"`、`radix 1 less than Character.MIN_RADIX`、`Zero length string`、
  `Illegal leading minus sign on unsigned string -1.`、
  `String value 4294967296 exceeds range of unsigned int.`、
  `Value out of range. Value:"32768" Radix:10`）。
- **JDK-27 对照**（jdk-27.0.1.0.2）：null 消息 `Cannot parse null string`（8 为 `"null"`）；
  非 10 基失败追加 ` under radix N` 后缀（8 无）。

## 4. 测试用例设计（映射到 NumberFormatExceptionTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 层次：NFE is IA/RE/Exception/Throwable/Serializable + getSuperclass | testHierarchy |
| P0 | 构造器：无参 null 消息 / (String) 消息透传 | testConstructors |
| P0 | parseInt NFE 触发 + 消息 + null 入参也是 NFE（非 NPE） | testThrown |
| P0 | 结构深化：恰好 2 个声明构造器（无 (String,Throwable)） | testHierarchyFull |
| P0 | Throwable 契约：initCause 一次/二次 ISE、addSuppressed、栈、fillInStackTrace、toString 逐字、unicode/长消息 | testThrowableContract |
| P0 | parse 家族消息矩阵：Integer/Long/Short/Byte + radix 界 + unsigned + decode + valueOf 转抛 | testThrownFamily |

## 关键规范细节（易错，已锁入断言）

- **null 入参是 NFE 不是 NPE**：`Integer.parseInt(null)` 抛 NFE（8 消息恰为 `"null"`）。
- **`initCause` 只允许一次**：二次调用抛 IllegalStateException（Throwable 通用契约）。
- **(String) 构造后 cause 仍为 null 且可补设**（与 (String,Throwable) 构造的异常不同，NFE 没有该形态）。
- **overflow 消息带 radix**：`Value out of range. Value:"32768" Radix:10`（Short/Byte 专属格式）。
- **unsigned 家族有专属消息**：负号拒绝与超 2^32 上界各一种。

## 注意事项

- headless 安全，不加 `@key`；结果确定；不依赖默认 Locale/时区。
- MODERN 分支（版本串前缀探测）同时锁定 8/27 两侧消息差异，同一源在两侧语义一致。
