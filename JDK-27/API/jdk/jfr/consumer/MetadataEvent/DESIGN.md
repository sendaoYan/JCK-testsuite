# jdk.jfr.consumer.MetadataEvent 设计案

## 1. API 功能
流式消费的元数据事件（final，无公开构造器）：getEventTypes/getConfigurations。

## 2. Java 用户如何使用
实例仅由 EventStream 元数据回调投递，难以确定性复现。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/consumer/MetadataEvent.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 final/关键方法存在；黑盒降级为白盒（无公开构造器）。

## 5. 深化（本轮）
断言规模：基线 → 17。
### 行为闭环
- 经 `EventStream.openFile(...).onMetadata(...)` 触发：MetadataEvent 收到的事件类型目录非空、含自定义 @Name 事件与 jdk.* 内建类型。
- getEventType()/getTypes() 通道一致；公共构造缺失检查（无公共 ctor，只能经框架实例化）。
### 实测负例异常表
无专门负例（目录语义为主）。
### 8/27 差异
MetadataEvent 为 27 独有。
