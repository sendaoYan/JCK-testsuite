# jdk.management.jfr.RemoteRecordingStream 设计案

## 1. API 功能
远程录制流（implements EventStream）：setMaxAge/setMaxSize/onEvent/close。

## 2. Java 用户如何使用
new RemoteRecordingStream(MBeanServerConnection)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.management.jfr/share/classes/jdk/management/jfr/RemoteRecordingStream.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒构造+配置+close。

## 5. 深化（本轮）
断言规模：基线 20 → 30。
### 行为闭环
- 生命周期：enable/disable 返回 EventSettings、setSettings(Map) 生效、setMaxAge/setMaxSize/setReuse/setOrdered、onFlush/onEvent 注册、startAsync → stop()==true → awaitTermination（全部 try-with-resources）。
- dump(Path) 落盘非空（dump 以 CREATE_NEW 打开目标：路径必须不存在）。
### 实测负例异常表
| 操作 | 异常 | 精确消息 |
|---|---|---|
| `new RemoteRecordingStream(null)` | NullPointerException | （断言类型） |
| close 后 `startAsync()` | IllegalStateException | 消息含 "closed" |
| dump 到已存在路径 | java.nio.file.FileAlreadyExistsException | （CREATE_NEW 语义，实测 /tmp/rrs-dump*.jfr） |
### 8/27 差异
RemoteRecordingStream 为 27 独有（8 无此类）。
