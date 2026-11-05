# javax.xml.stream.EventFilter — 设计案（JDK-8）

## 1. API 功能
事件过滤器接口（accept(XMLEvent)）。

## 2. 使用方式
实现 accept 后 XMLInputFactory.createFilteredReader(reader, filter) 安装。

## 3. 参考来源
- Oracle javadoc javax.xml.stream（StAX 1.2/JAXP；异常契约、事件常量、属性名常量）。
- JDK 内置实现 com.sun.xml.internal.stream.*（本 JDK 即被测实现）；OpenJDK jdk8u test/jdk/javax/xml 下 StAX 用例思路借鉴（代码自写）。
- javap（/data/yansendao/JDK/binary/temurin/jdk8u372-b07/bin/javap）枚举公开方法面；行为断言全部以本机 JDK 实测为准。

## 4. 测试用例设计 + 易错细节
### 接口
accept(XMLEvent) 单方法布尔。
### 与 createFilteredReader(EventFilter) 组合矩阵
- 只留元素事件：[Sr,Sa,Ea,Sb,Eb,Er]（文档序保持）；只留 StartElement：[a,b]。
- reject-all：hasNext 立即 false，零事件。
- 过滤器上的 peek 同样跳过被拒事件。
- 单元级：accept-all/accept-none/element-only 对 Characters/Comment 的区分。

### 断言计数
见 EventFilterTest.java 末尾 PASS 输出（本轮深化后 9 断言，双侧只增不减）。
