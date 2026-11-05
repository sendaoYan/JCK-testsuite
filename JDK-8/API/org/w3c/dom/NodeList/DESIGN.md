# NodeList — 设计案

## 1. API 功能
有序节点集合接口（getLength/item）。实现为活视图：Document/Element 的 getChildNodes()、getElementsByTagName(NS) 均返回随树变化的 NodeList。

## 2. Java 用户如何使用
经 Document 工厂建树后持有 getChildNodes()/getElementsByTagName() 引用做变更观察；无直接实现类。

## 3. 参考来源
W3C DOM Level 3 Core 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节

## 深化第 2 轮（契约级，2026-11-05）
行为化（经 Document 工厂取实例）：childNodes 活视图（append/remove 后旧引用同步）；item 越界/负数 null；getElementsByTagName 递归+活+通配（Element 级不含自身、Document 级含根）；removeChild 经 NodeList 引用联动；ns 扫描矩阵（("*","local") 只匹配有 ns 的节点——Xerces 实测；("*","*") 全量）；无匹配空表。
