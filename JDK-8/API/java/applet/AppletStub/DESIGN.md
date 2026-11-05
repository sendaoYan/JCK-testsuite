# java.applet.AppletStub 测试设计（JDK-8 侧）

## 1. API 功能
applet 与浏览器之间的缝接口（恰 6 个方法）：isActive/getDocumentBase/getCodeBase/getParameter/
getAppletContext/appletResize(int,int)。

## 2. Java 用户如何使用
```java
class RecStub implements AppletStub { /* 记录每个调用 */ }
applet.setStub(new RecStub(ctx));   // 注入后 Applet 的全部环境方法转发至此
```
## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/java/applet/Applet*.java`（Applet 面板 + stub 转发实现）。
- 历史 JDK 测试 `test/java/applet/`（本版本已随 applet 弃用收缩，本套件补齐离线行为面）。
## 4. 测试用例设计 + 自动化实现
- 白盒：接口面 6 方法精确签名（返回型 boolean/URL/URL/String/AppletContext/void）、全 public abstract、
  无受检异常。
- 黑盒（记录型实现）：active 标志翻转、两个 base URL 精确值、参数命中/未命中、context 同一性、
  resize 记录序列、双实例状态独立。
- 实现见 `AppletStubTest.java`。
