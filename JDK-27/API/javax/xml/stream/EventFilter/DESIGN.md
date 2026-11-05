# javax.xml.stream.EventFilter — 设计案

## 1. API 功能
javax.xml.stream.EventFilter 接口。实例/声明方法：accept。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.EventFilter() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.EventFilter）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 EventFilterTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`EventFilterTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。
## 5. 契约深化（本轮追加：过滤矩阵）
accept-all/none/element-only 单元矩阵 + createFilteredReader(EventFilter) 组合：元素事件保持文档序 [Sr,Sa,Ea,Sb,Eb,Er]、start-only [a,b]、reject-all 零事件、过滤 peek 跳过被拒事件。
