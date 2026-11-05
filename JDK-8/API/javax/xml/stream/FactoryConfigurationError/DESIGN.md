# javax.xml.stream.FactoryConfigurationError — 设计案（JDK-8）

## 1. API 功能
工厂配置错误（extends Error；(String)/(Exception)/(Exception,String)/() 4 构造；getException/getCause）。

## 2. 使用方式
工厂发现失败时抛出；亦可直接构造测试。

## 3. 参考来源
- Oracle javadoc javax.xml.stream（StAX 1.2/JAXP；异常契约、事件常量、属性名常量）。
- JDK 内置实现 com.sun.xml.internal.stream.*（本 JDK 即被测实现）；OpenJDK jdk8u test/jdk/javax/xml 下 StAX 用例思路借鉴（代码自写）。
- javap（/data/yansendao/JDK/binary/temurin/jdk8u372-b07/bin/javap）枚举公开方法面；行为断言全部以本机 JDK 实测为准。

## 4. 测试用例设计 + 易错细节
### 构造链 4 形态
(String)/(Exception)/(Exception,String)/()。getException/getCause 同一性；(Exception) 的 message 取异常消息；(Exception,String) 的 message 取串；no-arg 全 null。
### 抛出/捕获
可作为 Error 抛出捕获（message 保持）。
### 发现机制触发
系统属性 javax.xml.stream.XMLInputFactory 指向不存在类 → newFactory() 抛 FactoryConfigurationError（finally 恢复属性）。

### 断言计数
见 FactoryConfigurationErrorTest.java 末尾 PASS 输出（本轮深化后 15 断言，双侧只增不减）。
