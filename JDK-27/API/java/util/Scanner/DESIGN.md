# java.util.Scanner 测试设计

## 1. API 功能
- 构造器：`Scanner(String)`、`Scanner(InputStream)`、`Scanner(Readable)`、`Scanner(File)` 等。
- 分词：`next()`、`hasNext()`、`nextLine()`、`hasNextLine()`。
- 类型化：`nextInt`/`hasNextInt`（可带 radix）、`nextLong`/`nextDouble`/`nextBoolean`/`nextBigInteger` 等。
- 配置：`useDelimiter`、`useLocale`、`useRadix`、`reset`。
- 匹配：`findInLine`、`findWithinHorizon`、`skip`、`match`。
- 生命周期：`close`、`ioException`。

## 2. Java 用户如何使用
- 解析字符串：`new Scanner("10 20").nextInt()`。
- 逐行读取：`while (s.hasNextLine()) { s.nextLine(); }`。
- 自定义分隔符：`new Scanner("a,b,c").useDelimiter(",")`。
- 进制解析：`new Scanner("ff").useRadix(16).nextInt()`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/io/Console.java`：`readLine` 内部用 `Scanner` 解析输入。
- JDK 源码 `java/net/InetAddress.java`：`Scanner` 解析 `netmasks`。
- JDK 源码 `java/lang/System.java`：`System.console()` 相关注释使用 Scanner。
- 广泛用法：CSV/配置解析、`hasNextInt()` 输入校验。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言 final 类、实现 Iterator/Closeable、关键方法存在。
- 黑盒（固定字符串，不碰 System.in）：
  - nextInt/next/hasNext 组合、自定义分隔符。
  - hasNextInt/nextInt（含 radix 16）。
  - nextLine/hasNextLine、findInLine 前进。
  - InputMismatchException、NoSuchElementException、close 后 IllegalStateException。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 契约深化：分隔符/进制/Locale/关闭矩阵/IO 透传 | testDelimiterContracts、testFindSkipContracts |
