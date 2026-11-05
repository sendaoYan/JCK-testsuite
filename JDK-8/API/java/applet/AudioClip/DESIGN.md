# java.applet.AudioClip 测试设计（JDK-8 侧）

## 1. API 功能
音频剪辑接口（恰 3 个 void 方法）：play/loop/stop。

## 2. Java 用户如何使用
```java
AudioClip c = Applet.newAudioClip(new URL("file:/a.wav"));
c.play(); c.loop(); c.stop();     // 缺失文件也静默（不抛）
```
## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/java/applet/Applet*.java`（Applet 面板 + stub 转发实现）。
- 历史 JDK 测试 `test/java/applet/`（本版本已随 applet 弃用收缩，本套件补齐离线行为面）。
## 4. 测试用例设计 + 自动化实现
- 白盒：接口面 3 方法、无字段无常量。
- 黑盒：`Applet.newAudioClip(null)` → NPE；缺失文件仍返回可用 clip 且三方法静默；
  play(url)/play(url,name) 静默。
- 实现见 `AudioClipTest.java`。
