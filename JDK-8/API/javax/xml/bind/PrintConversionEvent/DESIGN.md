# javax.xml.bind.PrintConversionEvent 契约覆盖设计（JDK-8）

## 1. API 功能
ValidationEvent 的标记子接口：**写方向**（Java 值→XML 词法）类型转换失败。不新增方法/常量，仅类型分派。与 ParseConversionEvent 对偶。

## 2. 使用方式
```java
m.setEventHandler(e -> {
    if (e instanceof PrintConversionEvent) { /* 值打印失败 */ }
    return true;
});
```
具体构造用 helpers 包 `PrintConversionEventImpl`。

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/PrintConversionEvent.html
- JSR 222 (JAXB 2.2) 规范第 6 章
- OpenJDK 8u RI 源码 `javax/xml/bind/PrintConversionEvent.java`
- 本仓库测试：`PrintConversionEventTest.java`

## 4. 测试用例设计 + 易错规范细节
- 标记形态锁定：0 声明方法/0 常量/继承恰 4 方法；单向 isAssignableFrom。
- 具体类型闭环：PrintConversionEventImpl 3/4 参构造、双层 instanceof、继承的 setSeverity/setMessage 可用（含 severity 校验 IAE 继承生效）。
- 真实通道（弱断言+实证）：Marshaller 安装 handler 后 marshal 合法数据——**不产生任何事件**（锁定）；输出含 `<person>`/`<age>9</age>`。
- 易错点：合法数据的写方向是静默的——断言 events.isEmpty() 是正确姿势；人为制造写方向转换失败需自定义 XmlAdapter 抛异常，那属于 annotation/adapters 测试域，本目录不越界。
