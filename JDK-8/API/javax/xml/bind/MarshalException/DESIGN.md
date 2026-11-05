# javax.xml.bind.MarshalException 契约覆盖设计（JDK-8）

## 1. API 功能
`Marshaller.marshal` 操作失败时抛出的受检异常，直接继承 `JAXBException`，无新增方法；语义 = "序列化（对象→XML）方向失败"。当 marshal 过程中的底层异常（IOException/SAXException 等）发生时由 RI 包装抛出。

## 2. 使用方式
```java
try {
    m.marshal(obj, writer);
} catch (MarshalException me) {
    me.getErrorCode();        // 继承自 JAXBException
    me.getLinkedException();  // 底层原因
} catch (JAXBException e) {   // 也可按父类捕获
    ...
}
```

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/MarshalException.html
- JSR 222 (JAXB 2.2) 规范
- OpenJDK 8u RI 源码 `javax/xml/bind/MarshalException.java`
- 本仓库测试：`MarshalExceptionTest.java`

## 4. 测试用例设计 + 易错规范细节
- 继承链三段断言：extends JAXBException、is-a Exception、is-a Serializable。
- 构造矩阵 5 形态全测（String/String,String/Throwable/String,Throwable/String,String,Throwable），errorCode/linked/cause 全回读。
- **同步语义**：`setLinkedException(t2)` 后 `getCause()==t2`（继承 JAXBException 的覆写行为，实测锁定）。
- serialVersionUID 为 **计算值** `-6039934860308470324`（类未显式声明，用 `ObjectStreamClass.lookup` 锁定；不同 JDK8 构建理论上同源同值）。
- 多态通道：`throw MarshalException` 可被 `catch (JAXBException)` 捕获且动态类型保持、errorCode 可读。
- toString/printStackTrace 覆写行为继承父类（含 "with linked exception"）。
- 易错点：不要断言 MarshalException 的 message 格式与 JAXBException 不同——格式完全一致；差异只在类名。
