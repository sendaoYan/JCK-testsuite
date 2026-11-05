# javax.print.SimpleDoc 黑盒行为测试设计

## 1. API 功能
SimpleDoc(Object printData, DocFlavor, DocAttributeSet)：printData 类型必须匹配 flavor rep class。

## 2. Java 用户如何使用
构造最小打印文档。

## 3. 开源代码实际用法调研
实现权威：JDK 源码（JDK-8: jdk8u jdk/src/share/classes/javax/print；JDK-27: jdk/src/java.desktop/share/classes/javax/print）。行为断言均以双 JDK 实测探针为基准（javap + 运行时探针输出 diff）。

## 4. 测试用例设计 + 自动化实现
读取闭环：String 数据 getReaderForText 全量读回；byte[] 数据 getStreamForBytes 逐字节序读尽；getPrintData 同引用；getAttributes() 返回缓存式不可变实时视图（两次调用同引用、源集后续变更可见、视图写 → UnmodifiableSetException）；CHAR_ARRAY/Reader 文档读取；匹配 charset 的 [B 文档流可用。
- 断言合计 28/38（JDK-8/JDK-27 含既有基础断言）。

## 5. 负例与精确异常
NPE/IAE 矩阵（实测消息）：printData 为 null → IAE "null argument(s)"；flavor 为 null → IAE "null argument(s)"；String 数据配 BYTE_ARRAY.PNG → IAE "data is not of declared type"；byte[] 配 STRING.TEXT_PLAIN → IAE "data is not of declared type"。字符类 flavor 的 getStreamForBytes()==null；字节类数据的 getReaderForText()==null（均不抛异常）。

## 6. 闭环与版本差异
读闭环 + 视图语义闭环。无版本差异（NPE 消息两侧均为 null/IAE 文本一致）。

## 7. 运行纪律
纯 headless（env -u DISPLAY），不加 @key；不真用打印机/声卡。jtreg 双侧（JDK-8 需 JTREG_JAVA=jdk-17）独立 workDir/reportDir 全绿后提交。
