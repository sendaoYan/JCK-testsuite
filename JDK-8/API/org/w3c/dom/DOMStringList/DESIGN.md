# DOMStringList — 设计案

## 1. API 功能
（见 org.w3c.dom.DOMStringList javadoc / DOM Level 3 Core）

## 2. 使用方式
经 Document/DOMConfiguration/LSParser 组合取得实例或回调。

## 3. 参考来源
W3C DOM Level 3 Core/LS 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节
## 深化第 2 轮（契约级，2026-11-05）
活生产者：DOMConfiguration.getParameterNames() 返回 org.w3c.dom.DOMStringList（实为 Xerces DOMStringListImpl）——length≥15、contains("comments"/"entities") 命中、未知名 false、item(0) 非 null；接口契约经用户实现锁定（item 越界 null、getLength、contains(null) false）；JDK-8 可反射直达 DOMStringListImpl 空实例、JDK-27 被模块系统拦截（双侧各自断言）。
