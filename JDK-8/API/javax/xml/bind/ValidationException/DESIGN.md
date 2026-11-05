# javax.xml.bind.ValidationException 契约覆盖设计（JDK-8）

## 1. API 功能
校验（Validator/事件处理管线）失败时抛出的受检异常，继承 `JAXBException`，无新增方法。JAXB 2.x 中 Validator 已被移除，该异常主要由自定义 ValidationEventHandler/框架层使用。

## 2. 使用方式
```java
try {
    ...
} catch (ValidationException ve) {
    ve.getErrorCode();
    ve.getLinkedException();
}
```

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/ValidationException.html
- JSR 222 (JAXB 2.2) 规范
- OpenJDK 8u RI 源码 `javax/xml/bind/ValidationException.java`
- 本仓库测试：`ValidationExceptionTest.java`

## 4. 测试用例设计 + 易错规范细节
- 继承链：extends JAXBException、is-a Exception、is-a Serializable。
- 构造矩阵 5 形态：message/errorCode/linked/cause 全回读；(Throwable) 形态 message 为 null。
- setLinkedException → getCause 同步（继承覆写行为）。
- serialVersionUID 计算值 `-3491641813205786786`（ObjectStreamClass.lookup 锁定）。
- 多态捕获：`catch (JAXBException)` 捕获后动态类型保持、errorCode 可读。
- 易错点：与 Marshal/UnmarshalException 同构，测试不依赖 RI 内部何时抛它（JAXB 2.x 无 Validator 入口），只锁类型契约本身。
