# javax.xml.stream.StreamFilter — 设计案（JDK-8）

## 1. API 功能
游标过滤器接口（accept(XMLStreamReader)）。

## 2. 使用方式
实现 accept 后 XMLInputFactory.createFilteredReader(reader, filter) 安装。

## 3. 参考来源
- Oracle javadoc javax.xml.stream（StAX 1.2/JAXP；异常契约、事件常量、属性名常量）。
- JDK 内置实现 com.sun.xml.internal.stream.*（本 JDK 即被测实现）；OpenJDK jdk8u test/jdk/javax/xml 下 StAX 用例思路借鉴（代码自写）。
- javap（/data/yansendao/JDK/binary/temurin/jdk8u372-b07/bin/javap）枚举公开方法面；行为断言全部以本机 JDK 实测为准。

## 4. 测试用例设计 + 易错细节
### 接口
accept(XMLStreamReader) 单方法布尔（以 reader 当前事件状态判定）。
### 与 createFilteredReader(StreamFilter) 组合矩阵
- 只留元素：[Sb,Eb,Ea,EV8]——**根元素 START 在过滤器 priming 时被消费**（JDK XMLStreamFilterImpl 行为，两版本一致）；END_DOCUMENT 仍送达。
- reject-all：零事件。
- 被接受事件上属性/命名空间状态完整可读（getAttributeValue 正常）。

### 断言计数
见 StreamFilterTest.java 末尾 PASS 输出（本轮深化后 8 断言，双侧只增不减）。
