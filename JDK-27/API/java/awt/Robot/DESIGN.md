# java.awt.Robot 黑盒行为测试设计

## 1. API 功能
- 构造：`Robot()`、`Robot(GraphicsDevice)`（平台不支持输入控制时抛 `AWTException`）。
- 输入：`mouseMove/mousePress/mouseRelease/mouseWheel/keyPress/keyRelease/click/glide/type`。
- 捕获：`createScreenCapture(Rectangle)`、`createMultiResolutionScreenCapture`、`getPixelColor(x,y)`。
- 配置：`getAutoDelay/setAutoDelay`、`isAutoWaitForIdle/setAutoWaitForIdle`、`delay/waitForIdle`。
- 常量：`DEFAULT_DELAY`、`DEFAULT_STEP_LENGTH`。

## 2. Java 用户如何使用
程序化控制鼠标键盘并截屏（自动化/演示）：
```java
Robot r = new Robot();
r.mouseMove(100, 100);
BufferedImage shot = r.createScreenCapture(new Rectangle(0,0,100,100));
Color c = r.getPixelColor(0, 0);
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/Robot.java`：默认 `autoDelay=0`、`autoWaitForIdle=false`；构造不支持抛 `AWTException`。
- `test/jdk/java/awt/Robot/` 大量使用 createScreenCapture/getPixelColor/mouseMove 做屏幕断言。
- `test/jdk/java/awt/event/MouseEvent/`、`KeyEvent/` 用 Robot 合成输入。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`，健壮模式）：
- `new Robot()`（捕获 `AWTException` 时记录可接受）。
- 默认 autoDelay==0/autoWaitForIdle==false；set/get 往返。
- 常量 DEFAULT_DELAY==20、DEFAULT_STEP_LENGTH==2。
- `createScreenCapture(8x8)` 尺寸、`getPixelColor(0,0)` 不透明非负通道。
- `mouseMove/waitForIdle/delay/toString` 可调用。
headful（需真实显示）。
