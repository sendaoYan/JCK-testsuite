# javax.print.ServiceUI 黑盒行为测试设计

## 1. API 功能
ServiceUI.printDialog(GraphicsConfiguration,x,y,services,default,flavor,attrs) —— 类的唯一 public static 方法。

## 2. Java 用户如何使用
原生打印对话框入口。

## 3. 开源代码实际用法调研
实现权威：JDK 源码（JDK-8: jdk8u jdk/src/share/classes/javax/print；JDK-27: jdk/src/java.desktop/share/classes/javax/print）。行为断言均以双 JDK 实测探针为基准（javap + 运行时探针输出 diff）。

## 4. 测试用例设计 + 自动化实现
headless 契约：isHeadless() 时 printDialog(...) 抛 HeadlessException（本机 env -u DISPLAY）；反射：恰好一个 printDialog、返回 PrintService、声明 HeadlessException、类具体非抽象。

## 5. 负例与精确异常
headless 下 HeadlessException（消息含 X11 提示文本，两侧措辞不同故不断言消息）。

## 6. 闭环与版本差异
skip 理由：无显示器；断言无显示契约（异常本身）。无功能性版本差异（两侧均抛）。

## 7. 运行纪律
纯 headless（env -u DISPLAY），不加 @key；不真用打印机/声卡。jtreg 双侧（JDK-8 需 JTREG_JAVA=jdk-17）独立 workDir/reportDir 全绿后提交。
