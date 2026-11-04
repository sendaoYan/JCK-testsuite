# ProcessBuilder.Redirect — 设计方案

## 1. API 功能
`java.lang.ProcessBuilder.Redirect`（abstract，无公开构造器）定义子进程标准流的重定向来源/去向：常量 `PIPE`/`INHERIT`/`DISCARD`，静态工厂 `from(File)`（读）/`to(File)`（写）/`appendTo(File)`（追加），抽象 `type()` 返回 `Redirect.Type`，`file()` 返回关联文件（无则为 null）。

## 2. Java 用户如何使用
- 把子进程 stdout/stderr 重定向到文件或丢弃：
```java
pb.redirectOutput(ProcessBuilder.Redirect.to(logFile));
pb.redirectError(ProcessBuilder.Redirect.DISCARD);
```

## 3. 开源代码实际用法调研
- Gradle/执行器类工具以 `Redirect.INHERIT`/`to(File)` 接管子进程输出；JDK `test/jdk/java/lang/ProcessBuilder/RedirectTest.java` 验证 type/file/append 语义（DISCARD 的 type 为 WRITE）。

## 4. 测试用例设计 + 自动化实现
白盒：验证 abstract、无公开构造器、`type()` 抽象且返回 `Redirect.Type`、`file()` 返回 File。
黑盒：三个常量的 type/file/单例性；`from/to/appendTo` 的 type 与 file 一致性；值语义 equals/hashCode（同文件等、读写不同、异文件不同）。
对应 `RedirectTest.java` 白盒 + 黑盒部分。
