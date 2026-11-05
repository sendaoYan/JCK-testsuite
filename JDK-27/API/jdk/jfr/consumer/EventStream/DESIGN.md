# jdk.jfr.consumer.EventStream 设计案

## 1. API 功能
事件流抽象基类：openRepository/onEvent/onClose/setReuse/setOrdered。

## 2. Java 用户如何使用
EventStream.openRepository() 打开仓库流。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/consumer/EventStream.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 abstract/关键方法；黑盒 openRepository + 配置回调。

## 5. 深化（本轮）
断言规模：基线 → 30。
### 行为闭环
- `openFile(path)` 双模式（阻塞流式）计数与 `RecordingFile.readAllEvents(path)` 等价——27 旗舰：流式通道与批量通道事件集合相等。
- onEvent/onEvent(name)/onFlush/onClose 回调注册 + remove（Consumer null 清除）语义。
- try-with-resources 关闭后行为受控。
### 实测负例异常表
| 操作 | 异常 | 精确消息 |
|---|---|---|
| `EventStream.openFile(不存在路径)` | java.io.FileNotFoundException | （消息含路径，断言类型） |
| 构造/注册 null 回调 | NullPointerException | （断言类型） |
### 8/27 差异
EventStream 家族（openFile/openRepository）为 27 独有；8 侧只有 RecordingFile。
