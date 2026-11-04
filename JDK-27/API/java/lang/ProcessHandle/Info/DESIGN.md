# ProcessHandle.Info — 设计方案

## 1. API 功能
`java.lang.ProcessHandle.Info`（接口）是进程信息快照，6 个访问器均返回 `Optional`：`command()`（可执行路径）、`commandLine()`（完整命令行）、`arguments()`（参数数组）、`startInstant()`、`totalCpuDuration()`、`user()`；信息不可得时为空 Optional。

## 2. Java 用户如何使用
- 监控/诊断工具读取本进程或子进程元数据：
```java
ProcessHandle.current().info().command().ifPresent(cmd -> log(cmd));
info.startInstant().map(Duration::between...)...
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/ProcessHandle` InfoTest 系列验证当前进程 command/user/startInstant 存在性、参数可为空；运维类工具（如 spring-boot Admin）用它展示进程表。

## 4. 测试用例设计 + 自动化实现
白盒：验证接口、访问器返回 `Optional` 且无参。
黑盒：当前进程 `command`（含 java）/`commandLine`（前缀包含 command）/`user` 非空/`startInstant` 存在且不晚于现在/`totalCpuDuration` 非负存在/`arguments` Optional 对象非空；两次快照 command/user 一致。
对应 `InfoTest.java` 白盒 + 黑盒部分。
