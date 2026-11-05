# Name — 设计案

## 1. API 功能
SOAP 专用的元素/属性名值对象（接口）：getLocalName/getPrefix/getURI/getQualifiedName。SAAJ 1.1 时代的命名抽象，1.2 起被 QName 补充但仍是 addChildElement(Name)/addAttribute(Name) 等的参数面。

## 2. 使用方式
`SOAPFactory.newInstance().createName(local)` 或 `createName(local,prefix,uri)`；或 `envelope.createName(...)`。qualified name 规则：有前缀时 "prefix:local"，无前缀时即 local。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/soap/)、JSR 67 (SAAJ 1.3)、OpenJDK 8u 内置 RI com.sun.xml.internal.messaging.saaj（rt.jar）、本仓库测试文件

## 4. 测试用例设计
维度：local 形态（prefix/uri 均空串、qualified==local）、三元形态精确回读、空前缀+uri 组合、envelope.createName 与工厂同语义、equals/hashCode 语义（同参相等、uri/local 区分、**前缀不参与相等**、与 String/null 不等）、跨工厂等值（envelope vs SOAPFactory 同参 equals+hashCode 相等）。断言 33 条（旧 7）。
- 坑1：**equals 只看 (localName, URI)——前缀不同仍 equals==true 且 hashCode 相等**（前缀被视为上下文相关的语法糖），实测锁定。
- 坑2：createName(local) 与 createName(local,"","") 等价。
- 坑3：getPrefix/getURI 无名时返回**空串**而非 null。
