# jdk.dynalink.Namespace（标记接口） 设计案

## 1. API 功能
纯标记接口（无任何方法）：NamespaceOperation 的目标命名空间标记；标准实现是 StandardNamespace{PROPERTY, ELEMENT, METHOD}。

## 2. Java 用户如何使用
实现该接口即可定义语言自己的命名空间，传给 Operation.withNamespaces(...)。

## 3. 开源代码实际用法调研
来源：JDK-27 src.zip jdk/dynalink/Namespace.java；Nashorn/GraalJS 用它区分属性/元素/方法三种成员访问。

## 4. 测试用例设计 + 易错点
白盒 getDeclaredMethods()==0；自定义 Namespace 进 NamespaceOperation 可按索引取回、contains 命中、toString 保序（GET:PROPERTY|<anon>）；相等性按引用（两个同 toString 的实例互不 contains）。

