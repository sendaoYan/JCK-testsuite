# jdk.jfr.Recording 设计案

## 1. API 功能
录制会话，管理一次 JFR 录制：构造/start/stop/dump/close，查询 id/state/name/settings/size。

## 2. Java 用户如何使用
new Recording() → enable → start → stop → dump(Path) → close，查询状态机 NEW→RUNNING→STOPPED。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/Recording.java；test/jdk/jdk/jfr/ 中 Recording 相关用例。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 public/关键方法；黑盒状态机 + name/settings + dump 非空 + size。

## 5. 深化（2026-11-05）：契约覆盖扩展
- 录制-序列化-回读全链路闭环（旗舰）：自定义事件 commit 2 条 → dump → readAllEvents 与 RecordingFile 流式双通道计数一致、字段全等（id/msg 配对）、startTime<=endTime、duration>=0、线程非 null、文件可重读。
- 状态机全序：NEW→DELAYED→RUNNING→STOPPED→CLOSED；非法转换全部精确异常。
- scheduleStart(150ms)→DELAYED→有界轮询(≤2s)→RUNNING；setDuration(150ms)+start→有界轮询→STOPPED。
- 默认值全表：size=0、maxAge=null、maxSize=0、dest=null、duration=null、dumpOnExit=false、toDisk=true、settings 空、start/stopTime=null。
- with* 链值格式实测：withThreshold(3ms)→"3000000 ns"、withoutThreshold→"0 s"、withPeriod(5s)→"5000000000 ns"、withStackTrace()→"true"、withoutStackTrace→"false"；disable 只改 enabled 键；enable(未知名) 容忍；enable(Class) 写 <typeId>#enabled 数字键（不是事件名键）；getSettings() 返回副本。
- copy 语义：NEW copy(false)→NEW 且 settings/id 不同；RUNNING 中 copy(true)→STOPPED、copy(false)→RUNNING。
- destination 语义（实测修正）：stop 自动落盘并自动 CLOSED（不是停在 STOPPED），其后 dump→IOException "has been closed"；dump 失败也会创建空目标文件（勿断言无残留文件）。
- getStream：RUNNING 中 IOException；STOPPED 后可读；end<start→IAE。

负例精确异常表（8+27 一致，除注明）：
| 操作 | 异常 | 消息（contains） |
|---|---|---|
| stop() 于 NEW | IllegalStateException | Recording must be started before it can be stopped. |
| 二次 start() | IllegalStateException | Recording can only be started once. |
| stop() 于 STOPPED | IllegalStateException | Can't stop an already stopped recording. |
| dump() 于 NEW | IOException | has not started |
| dump() 于 CLOSED | IOException | has been closed（8="no contents to write" / 27="no content to write"） |
| setMaxSize(-1) | IllegalArgumentException | Max size of recording can't be negative（且原值不变） |
| setMaxAge(负) | IllegalArgumentException | Max age of recording can't be negative |
| setName(null) | NullPointerException | — |
| getStream 于 RUNNING | IOException | Recording must be stopped before it can be read. |
| getStream(end<start) | IllegalArgumentException | End time of requested stream must not be before start time |
