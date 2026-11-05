# org.xml.sax.helpers.NamespaceSupport — 设计案

## 1. API 功能
org.xml.sax.helpers.NamespaceSupport 类。常量：XMLNS, NSDECL（共 2 个）。 实例/声明方法：reset, pushContext, popContext, declarePrefix, processName, getURI, getPrefixes, getPrefix, getDeclaredPrefixes, setNamespaceDeclUris, isNamespaceDeclUris。

## 2. Java 用户如何使用
declarePrefix/getURI/getPrefix/pushContext/popContext/processName。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.helpers.NamespaceSupport）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 NamespaceSupportTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`NamespaceSupportTest.testBlackBox()`）覆盖：内置 xml 前缀、声明、遮蔽、pop 恢复、processName 三元拆分。

### 深化轮次2（行为契约）
黑盒：XMLNS 常量=XML 前缀官方 URI；xml 前缀预绑定且 declarePrefix 拒绝；processName 未声明前缀→null；默认命名空间元素立即采用、属性永不采用；push 继承/pop 还原/reset 保留 xml。易错：processName 复用数组互相覆盖。
