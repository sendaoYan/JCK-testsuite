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

## 契约深化（2026-11）

### API 功能
- 静态查找：`current()`（pid>0、跨调用 equals、toString 恰为 pid 字符串）、`of(long)`（current 命中、0/-1/999999999 空、1 命中 init）、`allProcesses()`（含自身、pid 全正、单快照内无重复）。
- 关系遍历：`parent()`（子进程父即 current）、`children()`/`descendants()`（活跃时含子、退出后消失、Sleeper 自身无后代）。
- 生命周期：活/死两态的 `isAlive`、`destroy()`/`destroyForcibly()` 返回值（活 true、死 false）、`supportsNormalTermination()`（Linux true）。
- Info：活跃子进程 command 精确、commandLine/arguments 含类名与参数表、user=user.name、startInstant/totalCpuDuration present；退出后全空。
- 比较/相等：compareTo 自身 0、异号反对称、序随 pid（实现稳定）；equals/hashCode 跨 of() 查找一致；compareTo(null) NPE。
- 桥接：`Process.toHandle()` 同实例、pid 一致、与 `of(pid)` equals、经 handle destroy 真杀进程。

### 使用方式
子进程一律用 `System.getProperty("java.home")+"/bin/java"` 启动 Sleeper（sleep 10000）；源码与 class 放 `Files.createTempDirectory("lcG-ph")`（/tmp 独立目录），finally 先 destroyForcibly+waitFor(5s) 再递归删除；每个 Sleeper 必然被销毁且 waitFor。

### 参考来源
Oracle javadoc `java.lang.ProcessHandle`；OpenJDK 27 实测（Temurin 27.0.1，Linux /proc 语义）。

### 测试用例设计 + 易错规范细节
- `of(currentPid).get() != current()`（不同实例）但 equals/hashCode/compareTo 一致——**身份≠实例**，只锁 equals。
- `toString()` 恰等于 `Long.toString(pid)`（实测锁定）；`Process.toHandle()` 两次返回同一实例（==）。
- `of(-1)` 不抛异常返回 empty（实测）；`of(1)` 在 Linux 恒 present（init）。
- `allProcesses()` 每次调用是新快照，忙机器上两次 count 会漂移——去重断言必须单快照内完成（首轮即踩坑修复）。
- 子进程退出后：`destroy()`/`destroyForcibly()` 返回 false、`of(pid)` 变 empty、info 各 Optional 全空、current().children() 不再含该 pid——全部实测锁定。
- `destroy()`=SIGTERM(143)、`destroyForcibly()`=SIGKILL(137)，只锁非零与返回值，不锁具体码。
- `parent()` 只断 Optional 语义（present 时 alive、且稳定），present 本身按实测锁定；`supportsNormalTermination()` 按 Linux 实测锁 true。
- compareTo 与 `Long.compare(pid)` 同号是实现行为（OpenJDK 按 pid 比较），标注后锁定。
