# javax.print.PrintService 黑盒行为测试设计

## 1. API 功能
PrintService 15 方法接口：name/job/attributes/flavors/categories/values/listeners。

## 2. Java 用户如何使用
打印服务统一抽象。

## 3. 开源代码实际用法调研
实现权威：JDK 源码（JDK-8: jdk8u jdk/src/share/classes/javax/print；JDK-27: jdk/src/java.desktop/share/classes/javax/print）。行为断言均以双 JDK 实测探针为基准（javap + 运行时探针输出 diff）。

## 4. 测试用例设计 + 自动化实现
真实 PS 流服务全表面：getName()=="Postscript output"；11 flavors / 11 categories；SERVICE_FORMATTED.PRINTABLE 支持而 BYTE_ARRAY.POSTSCRIPT 不支持；getDefaultAttributeValue(Copies)==Copies(1)；getSupportedAttributeValues(Copies,...).toString()=="1-1000"；isAttributeCategorySupported/isAttributeValueSupported 真；getAttributes().size()==1；PrinterName 为 null；getUnsupportedAttributes(支持 flavor)==null；getServiceUIFactory()==null；getAttribute(null) → NPE。
- 记录型 BaseSvc 全方法矩阵 + listener add/remove 接受；接口反射：>=15 方法且重声明 equals/hashCode。

## 5. 负例与精确异常
getAttribute(null) → NPE（消息 null）。

## 6. 闭环与版本差异
PS 流服务闭环（与 StreamPrintServiceFactory 联动）。无版本差异（数值两侧一致）。

## 7. 运行纪律
纯 headless（env -u DISPLAY），不加 @key；不真用打印机/声卡。jtreg 双侧（JDK-8 需 JTREG_JAVA=jdk-17）独立 workDir/reportDir 全绿后提交。
