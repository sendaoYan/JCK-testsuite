# ProcessHandle — 设计方案

## 1. API 功能
`java.lang.ProcessHandle`（interface，`Comparable`）表示本地进程句柄：
- 标识：`pid`、静态 `of(long)`、`current()`
- 树：`parent/children/descendants`、静态 `allProcesses()`
- 信息：`info()` 返回 `ProcessHandle.Info`（command/commandLine/arguments/startInstant/totalCpuDuration/user）
- 生命周期：`isAlive/supportsNormalTermination/destroy/destroyForcibly/onExit`

## 2. Java 用户如何使用
- `ProcessHandle.current().pid()` 获取当前进程 pid；`of(pid)` 查询某进程。
```java
ProcessHandle me = ProcessHandle.current();
System.out.println("pid=" + me.pid() + " alive=" + me.isAlive());
```

## 3. 开源代码实际用法调研
- `Process` 的 `toHandle()` 返回 `ProcessHandle`；JDK `test/jdk/java/lang/ProcessHandle` 系列验证 pid、树遍历、info。

## 4. 测试用例设计 + 自动化实现
白盒：`ProcessHandle.class.isInterface()`、继承 `Comparable`。
黑盒：当前进程 pid>0、isAlive、compareTo/equals/hashCode、parent/children/descendants/allProcesses 非空、`of(pid)` present、`of(0)` empty；`info()` 各 Optional 非空。
对应 `<Simple>Test.java` 黑盒部分。
