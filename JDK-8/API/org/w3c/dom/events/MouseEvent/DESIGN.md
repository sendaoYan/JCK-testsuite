# org.w3c.dom.events.MouseEvent — 设计案

## 1. API 功能
org.w3c.dom.events.MouseEvent 的契约行为（黑盒实例级验证，非纯反射）。

## 2. Java 用户如何使用
经 `DocumentBuilderFactory.newDocumentBuilder().newDocument()` 构造真实 Xerces 文档，工厂方法建树；接口反射+方法签名 获得实例后逐方法断言返回值/副作用/异常 code。

## 3. 参考来源
DOM Level 2/3 规范（UI Events（JDK 未实现，接口锁定））+ JDK 内置 Xerces 实现（com.sun.org.apache.xerces.internal）实测；本仓库 MouseEventTest。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 接口形状：interface、extends UIEvent；initMouseEvent 为 15 参规范签名（AbstractView + EventTarget）；自有方法 getScreenX/Y、getClientX/Y、getAltKey/getCtrlKey/getMetaKey/getShiftKey、getButton、getRelatedTarget。
- getDeclaredFields 证明 MouseEvent 自身无数值常量（常量都在 Event/UIEvent）。
- 生产者缺席：Xerces 不支持 UI Events，`createEvent("MouseEvents")` → `NOT_SUPPORTED_ERR(9)`，因此无 JDK 原生实例可取（行为断言经接口反射+方法签名锁定）。
