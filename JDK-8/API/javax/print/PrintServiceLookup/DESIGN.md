# javax.print.PrintServiceLookup 黑盒行为测试设计

## 1. API 功能
静态 lookupPrintServices/lookupMultiDocPrintServices/lookupDefaultPrintService + registerServiceProvider/registerService；抽象 SPI 3+1 方法。

## 2. Java 用户如何使用
服务发现与注册扩展点。

## 3. 开源代码实际用法调研
实现权威：JDK 源码（JDK-8: jdk8u jdk/src/share/classes/javax/print；JDK-27: jdk/src/java.desktop/share/classes/javax/print）。行为断言均以双 JDK 实测探针为基准（javap + 运行时探针输出 diff）。

## 4. 测试用例设计 + 自动化实现
无服务契约（本机 CUPS-less）：lookupPrintServices(null,null) 非-null 数组且非-null 元素为 0；lookupMultiDocPrintServices 空；lookupDefaultPrintService() null。
- registerService(BaseSvc) → true 且经 lookup 可见（null-flavor 与 PRINTABLE-flavor 过滤均见）。
- registerServiceProvider(DeepLookup) → true 且 ProvSvc 可见；同一服务再注册第二个 provider → false（去重）。
- registerServiceProvider(null) → 两侧均 true（接受）；registerService(null)：JDK-8 返回 true 且在结果数组里泄漏一个 null 元素（后续带 flavor 的过滤查询直接 NPE），JDK-27 返回 false 且数组干净 —— 显式版本差异断言。
- 抽象类 + 默认构造存在。

## 5. 负例与精确异常
JDK-8 泄漏路径：lookupPrintServices(PRINTABLE, null) 在 registerService(null) 之后抛 NPE（javax.print.PrintServiceLookup.getServices 内部遍历 null 元素）——测试以 try/catch 捕获并按 isOld 断言归属。

## 6. 闭环与版本差异
注册闭环 + 版本差异闭环。skip 理由：headless 无 CUPS 打印机。

## 7. 运行纪律
纯 headless（env -u DISPLAY），不加 @key；不真用打印机/声卡。jtreg 双侧（JDK-8 需 JTREG_JAVA=jdk-17）独立 workDir/reportDir 全绿后提交。
