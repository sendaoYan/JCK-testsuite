# Element — 设计案

## 1. API 功能
DOM Element 接口。getTagName；属性 CRUD（set/get/removeAttribute、get/set/removeAttributeNode、hasAttribute）；命名空间属性（set/get/removeAttributeNS、get/setAttributeNodeNS、hasAttributeNS）；getElementsByTagName(NS)；setIdAttribute*。

## 2. Java 用户如何使用
createElement 后 setAttribute/getAttribute 属性 CRUD 与命名空间属性。

## 3. 开源代码实际用法调研
JDK test/jdk/org/w3c/dom 校验属性通道、命名空间属性、getElementsByTagNameNS 通配。

## 4. 测试用例设计 + 自动化实现
黑盒：getAttribute(missing)→空串；setAttribute 覆盖/删除；setAttribute(null/数字开头)→INVALID_CHARACTER_ERR；removeAttribute(null)→NPE（有属性时）；属性节点通道 set/get/removeAttributeNode 返回旧/被移除节点与 ownerElement；removeAttributeNode(absent)→NOT_FOUND_ERR；命名空间 set/get/removeAttributeNS 与 setAttributeNS(null,prefix)→NAMESPACE_ERR；getElementsByTagName(NS) 计数与 */* 通配。


## 深化第 2 轮（契约级，2026-11-05）
getAttribute(缺)="" vs getAttributeNode(缺)=null 不对称；removeAttribute(缺) 静默 vs removeAttributeNode(未挂载)→NOT_FOUND_ERR(8)；重复 setAttribute 覆盖且计数不变；setAttributeNode 同名返回旧 attr（ownerElement 清空）与挂载语义；非法名（"a b"/""）→INVALID_CHARACTER_ERR(5)；ns 属性全家族（setAttributeNS/hasAttributeNS/getAttributeNodeNS/setAttributeNodeNS/removeAttributeNS 缺失静默）；getElementsByTagNameNS 通配矩阵（("*",local) 只匹配 ns 节点）；getSchemaTypeInfo 非 null+无 schema 时 typeName/typeNamespace null+isDerivedFrom false；setTextContent/getTextContent；isEqualNode 属性差异敏感；cloneNode 保留属性与子树；qualified name 分解（prefix/localName/tagName）。
