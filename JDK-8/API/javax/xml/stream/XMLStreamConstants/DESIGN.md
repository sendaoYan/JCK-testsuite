# javax.xml.stream.XMLStreamConstants — 设计案（JDK-8）

## 1. API 功能
事件码常量接口（15 个 public static final int：11 个文档事件码 1..11 + CDATA/NAMESPACE/NOTATION_DECLARATION/ENTITY_DECLARATION 12..15）。

## 2. 使用方式
作为事件码的比较基准（XMLStreamReader/XMLEvent 实现之）。

## 3. 参考来源
- Oracle javadoc javax.xml.stream（StAX 1.2/JAXP；异常契约、事件常量、属性名常量）。
- JDK 内置实现 com.sun.xml.internal.stream.*（本 JDK 即被测实现）；OpenJDK jdk8u test/jdk/javax/xml 下 StAX 用例思路借鉴（代码自写）。
- javap（/data/yansendao/JDK/binary/temurin/jdk8u372-b07/bin/javap）枚举公开方法面；行为断言全部以本机 JDK 实测为准。

## 4. 测试用例设计 + 易错细节
### 全部 15 个常量精确值
START_ELEMENT=1/END_ELEMENT=2/PROCESSING_INSTRUCTION=3/CHARACTERS=4/COMMENT=5/SPACE=6/START_DOCUMENT=7/END_DOCUMENT=8/ENTITY_REFERENCE=9/ATTRIBUTE=10/DTD=11/CDATA=12/NAMESPACE=13/NOTATION_DECLARATION=14/ENTITY_DECLARATION=15（11 个文档事件码密集 1..11；12..15 为事件对象码）。
### 白盒
15 个 public static final int 字段齐全、类型 int、无多余公共字段。
### 易错
流解析器把 CDATA 节报为 CHARACTERS(4)（常量 12 仅在特定配置/事件对象层出现），但两常量值必须可区分——已断言 CDATA!=CHARACTERS。

### 断言计数
见 XMLStreamConstantsTest.java 末尾 PASS 输出（本轮深化后 64 断言，双侧只增不减）。
