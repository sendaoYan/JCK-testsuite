# org.xml.sax.ext.Attributes2Impl — 设计案

## 1. API 功能
org.xml.sax.ext.Attributes2Impl 类。实例/声明方法：isDeclared, isSpecified, setAttributes, addAttribute, removeAttribute, setDeclared, setSpecified。

## 2. Java 用户如何使用
addAttribute → isSpecified/isDeclared → set 标志。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.ext.Attributes2Impl）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 Attributes2ImplTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`Attributes2ImplTest.testBlackBox()`）覆盖：标志默认值 + set + 拷贝。

### 深化轮次2（行为契约）
黑盒：新条目默认 spec=T/decl=F；两 setter 往返；拷贝构造保标志位；removeAttribute 清空；越界 setSpecified → ArrayIndexOutOfBoundsException。
