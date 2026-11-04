# java.awt.PageAttributes 测试设计

## 1. API 功能
页面属性束（Cloneable）：color/media/orientation/origin/printQuality/printerResolution 六属性，
整数编码 setter（orientation 3/4、quality 3/4/5）、嵌套常量类（非枚举）、equals/hashCode、拷贝构造。

## 2. Java 用户如何使用
`PageAttributes pa = new PageAttributes(); pa.setOrientationRequested(
PageAttributes.OrientationRequestedType.LANDSCAPE); pa.setPrinterResolution(600);`
配合 PrintJob 属性传递（headless 安全，不实际打印）。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/PageAttributes.java`；
java.awt.PrintJob/Toolkit 调用点。

## 4. 测试用例设计 + 易错细节
- 默认：MONOCHROME/NA_LETTER/PORTRAIT/PHYSICAL/NORMAL/72,72,dpi。
- **整数编码精确域**：orientation 仅 3(portrait)/4(landscape)，2/5 抛 IAE；
  quality 3(draft)/4(normal)/5(high)，2 抛 IAE（3/4/5 与枚举一一对应锁定）。
- printerResolution：int 重载隐含 dpi；{x,y,units} 数组 units 仅 3(dpi)/4(dpcm)，
  错误 units/错 arity/null/负数均抛 IAE；返回数组是防御性副本。
- 所有 setXxx(null) 抛 IAE。
- 嵌套常量类为 pre-enum 风格：非 enum、toString 小写（"color"/"iso-a4"）、身份单例。
- equals/hashCode/clone；拷贝构造深度独立（含分辨率数组）；
  **不可序列化**（NotSerializableException）。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
