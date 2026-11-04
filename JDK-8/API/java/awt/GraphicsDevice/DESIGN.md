# java.awt.GraphicsDevice 黑盒行为测试设计（JDK-8）

## 1. API 功能
抽象类，代表图形设备：TYPE_RASTER_SCREEN=0/TYPE_PRINTER=1/TYPE_IMAGE_BUFFER=2、getType/getIDstring、
getConfigurations/getDefaultConfiguration/getBestConfiguration(GraphicsConfigTemplate)、
getDisplayMode(s)/setDisplayMode/isDisplayChangeSupported、
isFullScreenSupported/setFullScreenWindow/getFullScreenWindow、
getAvailableAcceleratedMemory、isWindowTranslucencySupported(WindowTranslucency)。

## 2. Java 用户如何使用
`GraphicsDevice dev = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();`
枚举 display modes/configurations；`dev.setFullScreenWindow(frame)` 进入/退出全屏。

## 3. 开源实际用法调研（本地源码，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/GraphicsDevice.java`
（getDisplayMode 默认由默认配置 bounds+pixelSize 合成；getBestConfiguration 委派模板；
getFullScreenWindow 初始 null；加速内存允许 -1 unknown 哨兵）。

## 4. 测试用例设计 + 易错细节
- 结构反射（保留原断言全集 + 类型常量值）→ `testStructure`。
- 设备枚举（type==RASTER_SCREEN、IDstring 非空）、display mode(s) 正尺寸、configs 非空非 null、
  getFullScreenWindow 初始 null → `testBlackBox`。
- **模板协议全覆盖（易错）：委托模板返回 configs[0]；返回 null 的模板传播 null** → `testBlackBox`。
- 稳定性：getIDstring 两次相等、getDisplayMode 两次 equals。
- 能力探针仅断言布尔性；条件分支 `isDisplayChangeSupported()` 时 `setDisplayMode(getDisplayMode())` 幂等；
  加速内存 >= -1；WindowTranslucency 枚举逐值。
- 全屏往返：可见 Frame set→get==f→null 清除。
- JDK-8 侧 51 断言通过（DISPLAY=:77，@key headful；不锁分辨率/刷新率数值）。
