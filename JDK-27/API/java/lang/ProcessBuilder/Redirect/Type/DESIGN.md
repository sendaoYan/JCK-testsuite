# ProcessBuilder.Redirect.Type — 设计方案

## 1. API 功能
`java.lang.ProcessBuilder.Redirect.Type`（final 枚举）描述重定向的类别：`PIPE`、`INHERIT`、`READ`（子进程读文件）、`WRITE`（写文件/DISCARD）、`APPEND`（追加）；`values()/valueOf(String)` 为标准枚举机制。

## 2. Java 用户如何使用
- 在拿到 `Redirect` 后按类别分支处理：
```java
if (pb.redirectInput().type() == ProcessBuilder.Redirect.Type.READ) { ... }
```

## 3. 开源代码实际用法调研
- 进程封装库（如 zt-exec、Gradle 进程 API）以 `type()` 分派；JDK `test/jdk/java/lang/ProcessBuilder` 大量断言 Redirect.type 映射。

## 4. 测试用例设计 + 自动化实现
白盒：验证 final 枚举、继承 Enum、`values/valueOf` public static。
黑盒：`values()` 恰 5 个且声明顺序 PIPE, INHERIT, READ, WRITE, APPEND；`valueOf` 每个常量往返、非法名抛 IAE、`name()`；`values()` 每次新数组、`compareTo` 按声明序。
对应 `RedirectTypeTest.java` 白盒 + 黑盒部分。
