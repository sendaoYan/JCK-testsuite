# NamespaceSupport — 设计案

## 1. API 功能
命名空间上下文栈：pushContext/popContext/declarePrefix/getURI/getPrefix/getPrefixes/getDeclaredPrefixes/processName/reset，XMLNS 常量。

## 2. Java 用户如何使用
手写解析器/序列化器的命名空间跟踪。

## 3. 开源代码实际用法调研
SAX 2.0.2 helpers javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：XMLNS 常量=XML 前缀官方 URI（1998）；xml 前缀预绑定且 declarePrefix('xml',…) 官方/错误 URI 均拒绝；processName 返回 [uri,local,raw]、未声明前缀/xmlns 前缀元素名→null；默认命名空间：元素立即采用、属性永不采用、declarePrefix('','') 撤销；getPrefixes 含声明+xml 而 getDeclaredPrefixes 仅当前层；push 继承/子层重绑/pop 还原；reset 保留 xml。易错：processName 结果数组复用会互相覆盖（返回的就是传入数组）。
