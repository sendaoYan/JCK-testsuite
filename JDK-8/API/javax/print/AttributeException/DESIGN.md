# javax.print.AttributeException 黑盒行为测试设计

## 1. API 功能
PrintException 的子接口：getUnsupportedAttributes() 返回 Class<?>[]（类别），getUnsupportedValues() 返回 Attribute[]（值）。

## 2. Java 用户如何使用
打印服务报告不支持的属性类别/值；自定义异常实现两方法。

## 3. 开源代码实际用法调研
实现权威：JDK 源码（JDK-8: jdk8u jdk/src/share/classes/javax/print；JDK-27: jdk/src/java.desktop/share/classes/javax/print）。行为断言均以双 JDK 实测探针为基准（javap + 运行时探针输出 diff）。

## 4. 测试用例设计 + 自动化实现
记录型实现 RecAttrException(Class<?>[], Attribute[])：数组同引用回读 + 内容断言 + null 数组允许 + instanceof PrintException。
- 断言：values/categories 数组身份；内容逐项；null 双数组合法；extends PrintException。

## 5. 负例与精确异常
无（接口无抛出约束）。

## 6. 闭环与版本差异
注意接口双方法语义：getUnsupportedAttributes 是 Class[] 而不是 Attribute[]（与直觉相反，javap 双侧验证）。

## 7. 运行纪律
纯 headless（env -u DISPLAY），不加 @key；不真用打印机/声卡。jtreg 双侧（JDK-8 需 JTREG_JAVA=jdk-17）独立 workDir/reportDir 全绿后提交。
