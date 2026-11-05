# org.w3c.dom.events.UIEvent — 设计案

## 1. API 功能
org.w3c.dom.events.UIEvent 的契约行为（黑盒实例级验证，非纯反射）。

## 2. Java 用户如何使用
经 `DocumentBuilderFactory.newDocumentBuilder().newDocument()` 构造真实 Xerces 文档，工厂方法建树；接口反射+方法签名 获得实例后逐方法断言返回值/副作用/异常 code。

## 3. 参考来源
DOM Level 2/3 规范（UI Events（JDK 未实现，接口锁定））+ JDK 内置 Xerces 实现（com.sun.org.apache.xerces.internal）实测；本仓库 UIEventTest。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 接口形状：interface、extends Event；getView 返回 AbstractView、getDetail 返回 int、initUIEvent(String,boolean,boolean,AbstractView,int) 签名锁定。
- getDeclaredFields 证明 UIEvent 自身无数值常量。
- 生产者缺席：`createEvent("UIEvents")` → `NOT_SUPPORTED_ERR(9)`（Xerces 未实现 view 依赖的 UI Events），无 JDK 原生实例。
