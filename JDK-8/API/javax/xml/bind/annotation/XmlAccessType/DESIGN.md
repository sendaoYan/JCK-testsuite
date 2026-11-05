# XmlAccessType 契约测试设计

## 1. API 功能

`javax.xml.bind.annotation.XmlAccessType` 枚举定义 JAXB 字段/属性访问策略，被 `@XmlAccessorType.value()` 引用：

- `PROPERTY`：仅 public getter/setter 对（JavaBean 属性）参与绑定；
- `FIELD`：仅每个非 static、非 transient 字段参与绑定；
- `PUBLIC_MEMBER`（默认）：public 字段 + public getter/setter 对；
- `NONE`：全部显式注解才绑定。

## 2. 使用方式

```java
@XmlAccessorType(XmlAccessType.FIELD)
static class Person { String name; }   // 私有字段也绑定
```

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlAccessType.html
- JSR 222 (JAXB 2.2) 第 8 章访问策略
- OpenJDK 8u RI 实测；本仓库测试 `XmlAccessTypeTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 常量：恰好 4 个；`values()` 声明顺序 `PROPERTY, FIELD, PUBLIC_MEMBER, NONE`；ordinal 0/1/2/3 逐一锁定。
2. Enum 通用契约：4 常量 valueOf 恒等回读循环；name/toString；compareTo 链（FIELD>PROPERTY、NONE>PUBLIC_MEMBER、自身==0）；`values()` 新数组；`getEnumConstants` 一致。
3. 负例：`valueOf("bogus")` → IAE（消息含枚举 FQCN）；`valueOf(null)` → NPE。
4. 交叉契约：`@XmlAccessorType.value()` 返回类型恰为 `XmlAccessType`。

易错规范细节：

- 声明顺序是 PROPERTY 在前而非 FIELD 在前——ordinal 断言按实测声明序锁定。
- 默认策略是 `PUBLIC_MEMBER`（`@XmlAccessorType` 的默认值），不是 NONE。
