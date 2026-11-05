# JAXBElement 设计案（JDK-8 契约测试）

## 1. API 功能
显式元素包装：QName 名 + 声明类型 + 值 + scope；nil 语义；序列化。用于非根元素映射、泛型元素声明。

## 2. 使用方式
new JAXBElement<>(qname, type, value)；或 JAXBElement(qname, type, scopeClass, value) 指定局部 scope。marshal 时以 qname 命名输出。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/bind/)、JSR 222 (JAXB 2.2) 规范、OpenJDK 8u 内置 RI（com.sun.xml.internal.bind.v2）、本仓库 JAXBContextTest 及同包族测试。

## 4. 测试用例设计 + 易错规范细节
- 值/nil 对偶：value null ⇔ isNil true；setValue 非 null 后 isNil false；**setNil(true) 不清空已存值**（实测，仅置 nil 标志）。
- scope：默认 GlobalScope.class；显式 scope 可读回；isGlobalScope 便捷判断。
- equals：**未覆写**——身份语义（实测两个同内容实例不 equals）。
- 序列化：SUID==1L（规范锁定）；round-trip 保 name/type/value/scope。
- marshal 命名：输出用 qname localName + ns 前缀（ns2:payload），unmarshal(Source, Class) 还原 name+value；nil 输出 xsi:nil="true" 且绑定 xsi 命名空间。
- 负例：null QName / null declaredType → IAE（实测）。
