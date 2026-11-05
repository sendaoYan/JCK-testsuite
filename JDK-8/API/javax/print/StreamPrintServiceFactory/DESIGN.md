# javax.print.StreamPrintServiceFactory 黑盒行为测试设计

## 1. API 功能
抽象工厂：lookupStreamPrintServiceFactories(DocFlavor, outputMimeType) / getOutputFormat / getSupportedDocFlavors / getPrintService(OutputStream)。

## 2. Java 用户如何使用
内存/流式 PS 等导出。

## 3. 开源代码实际用法调研
实现权威：JDK 源码（JDK-8: jdk8u jdk/src/share/classes/javax/print；JDK-27: jdk/src/java.desktop/share/classes/javax/print）。行为断言均以双 JDK 实测探针为基准（javap + 运行时探针输出 diff）。

## 4. 测试用例设计 + 自动化实现
PS 闭环：application/postscript 工厂恰好 1 个；输出格式精断；11 输入 flavor 含 SERVICE_FORMATTED.PRINTABLE；未知输出格式 "image/bogus" → 空非-null。
- 端到端：getPrintService(bos) → createPrintJob → print(SimpleDoc(PRINTABLE), attrs) → bos 输出以 "%!PS" 开头且 >100 字节；job.getPrintService()==svc；dispose() 后 isDisposed()==true。
- null 输出格式 → 非-null 结果。

## 5. 负例与精确异常
无强制异常（print 可抛 PrintException 由实现决定）。

## 6. 闭环与版本差异
PS 字节级输出闭环（headless 可用，软件实现）。无版本差异。

## 7. 运行纪律
纯 headless（env -u DISPLAY），不加 @key；不真用打印机/声卡。jtreg 双侧（JDK-8 需 JTREG_JAVA=jdk-17）独立 workDir/reportDir 全绿后提交。
