# org.w3c.dom.html.HTMLDocument — 设计案

## 1. API 功能
org.w3c.dom.html.HTMLDocument 契约（HTML 模块）。

## 2. Java 用户如何使用
JDK 未实现 HTML 引擎（hasFeature("HTML","2.0")=false、getFeature null、Document/DOMImplementation 不可 cast）——接口无法实例化，只能：常量/形状经反射或直接引用锁定；行为经匿名实现（或 Proxy）路由验证。

## 3. 参考来源
DOM Level 2 HTML 规范 + JDK 接口定义（javap 对照）实测。

## 4. 测试用例设计 + 易错细节
- 生产者缺席（双侧实测一致）：HTML 模块未实现，无任何 JDK 原生 HTMLDocument 实例可取。
- 形状锁定：声明方法数恰为 19（HTMLDocument extends org.w3c.dom.Document）、contains("close")、contains("getAnchors")、contains("getApplets")、contains("getBody")、contains("getCookie")、contains("getDomain") 等。
- extends org.w3c.dom.Document；文档级访问器族（title/referrer/URL/body/images/forms/links/anchors/cookie 等）。
- 访问器按 get/set 成对覆盖属性：Anchors、Applets、Body、Cookie、Domain、ElementsByName 等。
