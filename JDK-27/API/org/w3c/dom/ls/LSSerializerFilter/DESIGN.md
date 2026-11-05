# org.w3c.dom.ls.LSSerializerFilter — 设计案

## 1. API 功能
org.w3c.dom.ls.LSSerializerFilter 接口。实例/声明方法：getWhatToShow。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.ls.LSSerializerFilter() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.ls.LSSerializerFilter）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 LSSerializerFilterTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`LSSerializerFilterTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
- 形状：extends NodeFilter，仅新增 getWhatToShow（判定值继承自 NodeFilter）。
- 行为闭环：FILTER_REJECT 把元素从输出剪除、兄弟保留、源树不动；清除过滤器恢复完整输出。
- 版本差异：JDK 8 被拒元素的文本随之消失且 setFilter(null) 恢复完整输出；JDK 27 被拒元素的文本内容保留（<r>T<b/></r>）且 setFilter(null) 不清除旧过滤器（实现怪癖）。
