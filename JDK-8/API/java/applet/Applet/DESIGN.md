# java.applet.Applet 测试设计（JDK-8 侧）

## 1. API 功能
面板级 applet 容器（`extends Panel`）：生命周期 init/start/stop/destroy、参数/文档基址经
`AppletStub` 转发、音频/图像经 `AppletContext` 转发；`setStub(AppletStub)` 是工具侧注入缝
（public final，仅同包或工具可调）。

## 2. Java 用户如何使用（stub 注入闭环）
```java
class SubApplet extends Applet { public void inject(AppletStub s) { setStub(s); } }
SubApplet ap = new SubApplet();
ap.inject(new RecStub(ctx));          // 注入记录型 stub
ap.getParameter("color");             // → 转发 stub.getParameter，返回 "v:color"
ap.resize(101, 202);                  // → 转发 stub.appletResize(101,202)
ap.showStatus("hello");               // → stub.getAppletContext().showStatus(...)
```
闭环：注入后每个环境方法都可在记录型 stub/context 上观测到精确转发值与调用序。

## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/java/applet/Applet*.java`（Applet 面板 + stub 转发实现）。
- 历史 JDK 测试 `test/java/applet/`（本版本已随 applet 弃用收缩，本套件补齐离线行为面）。
## 4. 测试用例设计 + 自动化实现
- 白盒：继承 Panel、`setStub` public final、`newAudioClip` static、方法清单。
- 黑盒：
  - 无 stub 负例表：getParameter/getAppletContext/showStatus/getDocumentBase/getCodeBase/
    getAudioClip/getImage → NPE；isActive → false；getLocale → IllegalComponentStateException。
  - 注入闭环（正向表）：getParameter 值与键记录、两个 getBase 精确 URL、isActive 跟随 stub 标志、
    resize(int,int)/(Dimension) 双形态 → appletResize、showStatus → context、getAppletContext 同一性、
    getAudioClip/getImage URL 转发、play 静默。
  - 生命周期四方法无 stub 也静默；getAppletInfo/getParameterInfo 默认 null；isValidateRoot true。
  - headless 构造 → HeadlessException（@key headful 下走 headful 分支）。
- 实现见 `Applet/AppletTest.java`、`Applet/AccessibleApplet/AccessibleAppletTest.java`。
