# javax.xml.bind.DataBindingException 契约覆盖设计（JDK-8）

## 1. API 功能
JAXB 2.1 引入的**非受检**数据绑定异常：当 `JAXB.marshal/unmarshal` 便捷静态方法或 `JAXBContext.newInstance` 静态工厂失败时，RI 把 JAXBException/IOException 等包装成它抛出（避免便捷 API 强迫用户处理受检异常）。与 JAXBException 无继承关系。

## 2. 使用方式
```java
try {
    javax.xml.bind.JAXB.unmarshal(new File("x.xml"), Person.class);
} catch (DataBindingException dbe) {   // RuntimeException，可不捕获
    dbe.getCause();                    // 真实原因（通常是被包装的 JAXBException/IOException）
}
// 直接构造：
new DataBindingException("msg", cause);
new DataBindingException(cause);
```

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/DataBindingException.html
- JSR 222 (JAXB 2.2) 维护版本 2.1+ 新增
- OpenJDK 8u RI 源码 `javax/xml/bind/DataBindingException.java`
- 本仓库测试：`DataBindingExceptionTest.java`

## 4. 测试用例设计 + 易错规范细节
- 继承：extends RuntimeException、is-a Exception、is-a Serializable；**与 JAXBException 双向无继承关系**。
- **只有 2 个构造器**：`(String,Throwable)` 与 `(Throwable)`；无 `(String)` 单参、无 `(String,String)`——用 `getConstructor` 反射锁定。
- **(Throwable) 形态 message == throwable.toString()**（实测精确锁定，与 JAXBException 的 null 不同）。
- 无 getErrorCode/setLinkedException 方法（反射锁定 NoSuchMethodException）。
- null 容忍：`(String,null)` 与 `((Throwable)null)` 合法，cause/message 回读 null。
- serialVersionUID 计算值 `-3559750092881944571`。
- 易错点：message 断言用 `t.toString()` 全文比对（含类名前缀 `java.lang.RuntimeException: boom`）。
