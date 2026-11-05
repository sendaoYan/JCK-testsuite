# JAXBIntrospector 设计案（JDK-8 契约测试）

## 1. API 功能
运行期内省：isElement（是否 JAXB 元素）、getElementName（元素 QName）、getValue（解包值）。static getValue 门面 + 实例抽象方法。

## 2. 使用方式
ctx.createJAXBIntrospector() 取实例；JAXBIntrospector.getValue(obj) 静态便捷解包。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/bind/)、JSR 222 (JAXB 2.2) 规范、OpenJDK 8u 内置 RI（com.sun.xml.internal.bind.v2）、本仓库 JAXBContextTest 及同包族测试。

## 4. 测试用例设计 + 易错规范细节
- 解包契约：getValue(JAXBElement)==payload；isElement(JAXBElement)==true；**@XmlRootElement 标注的普通对象也是 element**（isElement==true，实测）且 getValue 是同一引用（identity）。
- getElementName：JAXBElement 用自身 QName；根标注对象推导派生根名（person）；未映射对象 → RI 抛 NPE（实测，勿锁 IAE）。
- 方法形态：getValue(Object) 是 **static**；isElement/getElementName 是抽象实例方法（实测锁定）。
- 负例：isElement(null)/getElementName(null) 抛 RuntimeException（RI 为 NPE）；getValue(null) **返回 null 不抛**（静态门面宽松，实测）。
