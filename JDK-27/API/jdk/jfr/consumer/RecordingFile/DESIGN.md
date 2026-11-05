# jdk.jfr.consumer.RecordingFile 设计案

## 1. API 功能
读取 .jfr 文件：构造/hasMoreEvents/readEvent/readAllEvents。

## 2. Java 用户如何使用
new RecordingFile(path) 逐事件读取。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordingFile.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 dump → readEvent 计数 + readAllEvents 非空。

## 5. 深化（本轮）
断言规模：基线 → 38。
### 行为闭环
- 两消费通道等价：自定义事件 dump 后 `readAllEvents(path)` 与 `new RecordingFile(path)` 流式逐事件读取，数量与字段值（tag id 等）完全一致。
- 文件可重读：同一 .jfr 两次独立打开逐字段相等。
- `write(Path, Predicate)`（27 独有）：谓词过滤落盘，写入副本字段完好（"write() copy field intact"）。
### 实测负例异常表
| 操作 | 异常 | 精确消息 |
|---|---|---|
| `new RecordingFile(不存在路径)` | java.io.FileNotFoundException | （消息含路径，断言类型） |
| `RecordingFile.readAllEvents(不存在路径)` | java.io.FileNotFoundException | （同上） |
### 8/27 差异
8 侧无 write(Path,Predicate)（反射缺席断言）；两通道等价与重读双侧同构。
