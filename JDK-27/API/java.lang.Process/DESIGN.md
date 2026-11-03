# Process — 设计方案

## 1. API 功能
`java.lang.Process`（abstract）表示一个本地进程，实现 `Closeable`：
- 抽象：`getOutputStream/getInputStream/getErrorStream/waitFor/exitValue/destroy`
- 便捷流：`inputReader/errorReader/outputWriter`（final）
- 状态：`isAlive/pid/supportsNormalTermination/waitFor(long,TimeUnit)/waitFor(Duration)`
- 句柄：`toHandle/info/children/descendants/onExit`

## 2. Java 用户如何使用
- 通常由 `ProcessBuilder.start()` 或 `Runtime.exec()` 返回，读输出、等待退出。
```java
Process p = new ProcessBuilder("java", "-version").start();
try (BufferedReader r = p.inputReader()) { String line; while ((line = r.readLine()) != null) { ... } }
int code = p.waitFor();
```

## 3. 开源代码实际用法调研
- `ProcessBuilder` 是推荐方式（`Runtime.exec` 已不推荐）；JDK `test/jdk/java/lang/ProcessBuilder` 大量验证输入/输出/退出码。
- 本测试用内存 `MockProcess` 子类覆盖抽象方法，避免执行真实外部命令（跨平台健壮）。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Process")` 验证 abstract、实现 `Closeable`、五个抽象方法。
黑盒：内存子类验证 `inputReader/errorReader` 读流、`outputWriter` 写流、`waitFor/exitValue/isAlive` 退出状态、`waitFor(Duration)`。
对应 `<Simple>Test.java` 黑盒部分。
