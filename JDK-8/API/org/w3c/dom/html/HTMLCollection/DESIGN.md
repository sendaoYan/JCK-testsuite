# org.w3c.dom.html.HTMLCollection — 设计案

## 1. API 功能
org.w3c.dom.html.HTMLCollection 契约（HTML 模块）。

## 2. Java 用户如何使用
JDK 未实现 HTML 引擎（hasFeature("HTML","2.0")=false、getFeature null、Document/DOMImplementation 不可 cast）——接口无法实例化，只能：常量/形状经反射或直接引用锁定；行为经匿名实现（或 Proxy）路由验证。

## 3. 参考来源
DOM Level 2 HTML 规范 + JDK 接口定义（javap 对照）实测。

## 4. 测试用例设计 + 易错细节
- 生产者缺席（双侧实测一致）：HTML 模块未实现，无任何 JDK 原生 HTMLCollection 实例可取。
- 形状锁定：声明方法数恰为 3（HTMLCollection 顶层接口）、contains("getLength")、contains("item")、contains("namedItem") 等。
- 行为补充：匿名实现 getLength/item（越界 null）/namedItem（按名/未知 null）索引与命名双通道。
- 访问器按 get/set 成对覆盖属性：Length。
