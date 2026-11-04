# java.util.Scanner 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。用 String 源保证确定性；`useLocale(Locale.US)` 规避默认 Locale
> 影响浮点解析；不依赖文件/网络/墙钟。

## 1. API 功能

- **构造器**：`Scanner(String)`、`Scanner(Readable)`、`Scanner(InputStream)`、`Scanner(InputStream,String)`、
  以及 String/Readable 的 charset 变体。
- **配置**：`useDelimiter(String/Pattern)`、`useLocale(Locale)`、`useRadix(int)`、`reset()`；
  读回 `delimiter()`/`locale()`/`radix()`。
- **token**：`hasNext()/next()`、`hasNext(String/Pattern)/next(String/Pattern)`、`hasNextLine()/nextLine()`。
- **数字/布尔**：`hasNextInt()/nextInt()`、`hasNextInt(int)/nextInt(int)`、byte/short/long/float/double/boolean/
  BigInteger/BigDecimal 各自 hasNext/next。
- **搜索**：`findInLine(String/Pattern)`、`findWithinHorizon(String/Pattern,int)`、`skip(String/Pattern)`。
- **其他**：`remove()`、`close()`、`ioException()`、`match()`、`toString()`。

## 2. 使用方式

`new Scanner(input).useLocale(Locale.US)` → `hasNextInt()`/`nextInt()`/`nextLine()`/`findInLine(...)`；
`useRadix(16)` 后 `nextInt()` 按十六进制解析。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/Scanner`。
- JDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/Scanner.java`：
  构造器 null → NPE；`useRadix` 越界 `[2,36]` → IAE；`useDelimiter/useLocale(null)` → NPE；
  close 后 `hasNext/next/nextInt` → IllegalStateException；`remove()` → UOE。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 基础 token + 分隔符 + hasNextInt | testTokens / testDelimiter / testHasNextInt |
| P0 | 构造器 null NPE 矩阵 | testConstructorNull |
| P0 | radix 读回 + useRadix 越界 IAE + nextInt(16) | testRadix |
| P0 | useDelimiter/useLocale null NPE | testNullSetters |
| P0 | 耗尽 NSEE + close ISE + ioException | testExhaustionAndClose |
| P0 | remove UOE | testRemoveUnsupported |
| P1 | nextByte/Short/Long/Boolean/Double + InputMismatch | testNextPrimitives / testInputMismatch |
| P1 | hasNext 变体（含 radix） | testHasNextVariants |
| P1 | nextLine/hasNextLine | testNextLine |
| P1 | findInLine/findWithinHorizon/skip | testFindAndSkip |
| P1 | reset 恢复默认 | testReset |
| P1 | match() + toString | testMatchAndToString |
| P0 | 契约深化：分隔符/进制/Locale/关闭矩阵/IO 透传 | testDelimiterContracts、testFindSkipContracts |

## 关键规范细节

- `Scanner((Readable/InputStream/String)null)` → NPE。
- `useRadix(1)`/`useRadix(37)` → IAE；合法范围 `[2,36]`；默认 radix 10。
- `useDelimiter((String)null)`/`useLocale(null)` → NPE；`useDelimiter((Pattern)null)` 不抛（直接存 null）。
- `nextInt()` 对非数字 token → `InputMismatchException`；空源 `next()` → `NoSuchElementException`。
- `close()` 后调用除 `ioException()` 外方法 → `IllegalStateException`；`remove()` → `UnsupportedOperationException`。
- `nextInt(16)` 解析十六进制数字（不含 `0x` 前缀）。

## 注意事项

String 源；`useLocale(Locale.US)` 固定浮点解析；不依赖文件/网络；无全局副作用；headless-safe 不加 `@key`。
