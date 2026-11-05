# java.applet.AppletContext 测试设计（JDK-8 侧）

## 1. API 功能
applet 环境接口（恰 10 个方法）：音频/图像获取、applet 查找（按名/枚举）、showDocument 两形态、
showStatus、流键值存储 setStream/getStream/getStreamKeys（setStream 声明 IOException）。

## 2. Java 用户如何使用
```java
AppletContext ctx = applet.getAppletContext();
ctx.setStream("k", in);       // 存
ctx.getStream("k");           // 取同一 InputStream 实例
ctx.setStream("k", null);     // null 流即删除（getStreamKeys 随之收缩）
```
## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/java/applet/Applet*.java`（Applet 面板 + stub 转发实现）。
- 历史 JDK 测试 `test/java/applet/`（本版本已随 applet 弃用收缩，本套件补齐离线行为面）。
## 4. 测试用例设计 + 自动化实现
- 白盒：接口面 10 方法逐签名锁定、全 public abstract、setStream 的受检异常表。
- 黑盒（记录型 MapContext）：applet 按名查找命中/未命中、getApplets 枚举全量、showStatus/showDocument
  两形态记录、流存储的存/取/替换/null 删除/getStreamKeys 一致性。
- 实现见 `AppletContextTest.java`。
