# javax.xml.stream.XMLReporter — 设计案（JDK-8）

## 1. API 功能
非致命告警回调接口（report(message, errorType, relatedInfo, location)）。

## 2. 使用方式
实现 report 后 XMLInputFactory.setXMLReporter 安装。

## 3. 参考来源
- Oracle javadoc javax.xml.stream（StAX 1.2/JAXP；异常契约、事件常量、属性名常量）。
- JDK 内置实现 com.sun.xml.internal.stream.*（本 JDK 即被测实现）；OpenJDK jdk8u test/jdk/javax/xml 下 StAX 用例思路借鉴（代码自写）。
- javap（/data/yansendao/JDK/binary/temurin/jdk8u372-b07/bin/javap）枚举公开方法面；行为断言全部以本机 JDK 实测为准。

## 4. 测试用例设计 + 易错细节
### 接口契约
report(String message, String errorType, Object relatedInfo, Location)；null 参数容忍；setXMLReporter/getXMLReporter 实例同一性；默认工厂无 reporter。
### 触发场景实测（记录性结论）
- 内部子集重复实体声明：解析完成、**回调未触发**（JDK StAX 无非致命告警路径触发点——两版本一致，记录"未触发"并保留结构断言）。
- 致命 well-formedness 错误 → XMLStreamException 直抛，**不经过 reporter**。
- 安装 reporter 不改变解析输出（"rk" 全等）。

### 断言计数
见 XMLReporterTest.java 末尾 PASS 输出（本轮深化后 10 断言，双侧只增不减）。
