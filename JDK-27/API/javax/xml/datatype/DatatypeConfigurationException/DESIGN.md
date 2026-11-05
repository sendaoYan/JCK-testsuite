# javax.xml.datatype.DatatypeConfigurationException — 设计案

## 1. API 功能
javax.xml.datatype.DatatypeConfigurationException 类。标记接口，无声明成员。

## 2. Java 用户如何使用
三种构造。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.datatype.DatatypeConfigurationException）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DatatypeConfigurationExceptionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DatatypeConfigurationExceptionTest.testBlackBox()`）覆盖：message/throwable/no-arg。

## 5. 深化（本批新增）
- 构造矩阵（javap 核对）：恰 4 构造器 ()/(String)/(String,Throwable)/(Throwable)；(Throwable) 的 message 取 cause.toString；cause identity 精确。
- 真实触发：见 DatatypeFactoryTest（系统属性指向不存在类 → DCE，cause ClassNotFoundException）。
- 双侧无差异。
