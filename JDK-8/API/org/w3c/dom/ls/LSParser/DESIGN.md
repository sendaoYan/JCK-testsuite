# org.w3c.dom.ls.LSParser — 设计案

## 1. API 功能
LSParser 的契约行为。

## 2. Java 用户如何使用
经 DocumentBuilderFactory.newDocumentBuilder().newDocument() + getFeature("LS","3.0") 获得真实 Xerces 实例（xpath 模块 JDK 未实现，形状锁定）。

## 3. 参考来源
DOM Level 3 LS Load/Save / DOM XPath 规范 + JDK 内置 Xerces 实测。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 常量：ACTION_APPEND_AS_CHILDREN=1、ACTION_REPLACE_CHILDREN=2、ACTION_INSERT_BEFORE=3、ACTION_INSERT_AFTER=4、ACTION_REPLACE=5。
- 行为：parse(stringData) 建全树且解析器可复用；同步模式 busy=false/async=false；parseWithContext → `NOT_SUPPORTED_ERR(9)` 且不动上下文节点；parse(null) → NullPointerException；getDomConfig 可设 comments=false 并在解析时剔除注释；setFilter/getFilter 往返（缺省 null）。