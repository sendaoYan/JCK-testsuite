# javax.xml.stream.StreamFilter — 设计案

## 1. API 功能
javax.xml.stream.StreamFilter 接口。实例/声明方法：accept。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.StreamFilter() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.StreamFilter）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 StreamFilterTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`StreamFilterTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。
## 5. 契约深化（本轮追加：过滤矩阵）
accept 以 reader 状态判定；组合矩阵：元素序列 [Sb,Eb,Ea,EV8]（**根 START 在 priming 中被消费**——JDK XMLStreamFilterImpl 行为，与 JDK-8 一致）、reject-all 零事件、被接受事件属性状态完整。
