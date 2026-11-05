# org.w3c.dom.ls.LSSerializerFilter — 设计案

## 1. API 功能
LSSerializerFilter 的契约行为。

## 2. Java 用户如何使用
经 DocumentBuilderFactory.newDocumentBuilder().newDocument() + getFeature("LS","3.0") 获得真实 Xerces 实例（xpath 模块 JDK 未实现，形状锁定）。

## 3. 参考来源
DOM Level 3 LS Load/Save / DOM XPath 规范 + JDK 内置 Xerces 实测。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 形状：extends NodeFilter，仅新增 getWhatToShow（判定值继承自 NodeFilter）。
- 行为闭环：FILTER_REJECT 把元素从输出剪除、兄弟保留、源树不动；清除过滤器恢复完整输出。
- 版本差异：JDK 8 被拒元素的文本随之消失且 setFilter(null) 恢复完整输出；JDK 27 被拒元素的文本内容保留（<r>T<b/></r>）且 setFilter(null) 不清除旧过滤器（实现怪癖）。