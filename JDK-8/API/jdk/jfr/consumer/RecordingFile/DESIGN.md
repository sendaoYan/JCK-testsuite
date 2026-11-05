# jdk.jfr.consumer.RecordingFile 设计案（JDK-8）

## 1. API 功能
离线消费 .jfr 录制文件：`new RecordingFile(Path)` + `readEvent()` 流式读取 + static `readAllEvents(Path)` 一次性读取 + `close()`。

## 2. Java 用户如何使用
录制 dump 到文件后，用 RecordingFile 回读事件做离线分析（CI 产物诊断、事后剖析）。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk8u-dev/jdk/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordingFile.java（8u262 backport）。
- JDK-8 面：无 `write(Path, Predicate)`（JDK-17+ 才有）、无 EventStream 文件通道。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（录制→dump→回读闭环）。骨架 `static int passed; check(boolean,String)`，末尾唯一 PASS 打印。

## 5. 深化（本轮）
断言规模：基线 → 36。
### 行为闭环
- 自定义 @Name 事件 commit → Recording.dump → 两消费通道等价：`readAllEvents(path)` 与 `new RecordingFile(path)` 逐事件流式读出的数量、字段值完全一致。
- 文件可重读：同一 .jfr 文件两次独立打开，事件字段值（如 tag id 字符串）逐项相等。
- JDK-8 缺席断言：反射确认类不声明 `write` 方法（27 侧有 write(Path,Predicate)，8 侧无）。
### 实测负例异常表
| 操作 | 异常 | 实测消息 |
|---|---|---|
| `new RecordingFile(不存在的路径)` | java.io.FileNotFoundException | （消息含路径，断言类型） |
| `RecordingFile.readAllEvents(不存在的路径)` | java.io.FileNotFoundException | （同上） |
### 8/27 差异
27 侧有 `write(Path, Predicate)` 谓词过滤与 EventStream.openFile；8 侧只有两条消费通道且无 write。
