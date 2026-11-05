# JAXB 设计案（JDK-8 契约测试）

## 1. API 功能
JAXB 2.1+ 静态门面：marshal/unmarshal 便捷重载（Writer/OutputStream/File/Result 与 Reader/Source/InputStream/File/URL），失败以 DataBindingException（unchecked）抛出。

## 2. 使用方式
JAXB.marshal(obj, sink) / JAXB.unmarshal(source, Class)——内部自建上下文，适合一次性操作。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/bind/)、JSR 222 (JAXB 2.2) 规范、OpenJDK 8u 内置 RI（com.sun.xml.internal.bind.v2）、本仓库 JAXBContextTest 及同包族测试。

## 4. 测试用例设计 + 易错规范细节
- final 类、全静态方法、无 public 构造。
- 写通道 4 形态 + 读通道 6 形态全部回读；facade 自闭环（marshal→unmarshal 字段全等）。
- 负例：malformed → DataBindingException 且 cause 非空；**typed unmarshal 对根名不敏感**（<notperson/> 传入 Person.class 返回默认值对象，实测）；marshal(非映射对象) → RI 抛 ClassCastException；marshal(null) → RI 抛 NPE（均实测锁定，勿锁 DataBindingException）。
