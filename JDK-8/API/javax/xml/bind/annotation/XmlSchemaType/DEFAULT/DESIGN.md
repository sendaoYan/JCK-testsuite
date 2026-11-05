# XmlSchemaType.DEFAULT 设计案（JDK-8 契约测试）

## 1. API 功能
type() 成员的默认值哨兵类：final 纯标记，表示"由属性推断类型"。

## 2. 使用方式
@XmlSchemaType(name="date") 省略 type 即默认 DEFAULT。

## 3. 参考来源
Oracle javadoc javax.xml.bind.annotation.XmlSchemaType、JSR 222、本仓库 DEFAULTTest/XmlSchemaTypeTest。

## 4. 测试用例设计 + 易错规范细节
- 结构：final、纯标记、单 public 构造、身份 equals、嵌套于 XmlSchemaType。
- 角色闭环：type().getDefaultValue()==DEFAULT.class；字段级注解回读同一哨兵。
- 易错点（实测）：**namespace() 默认是 "http://www.w3.org/2001/XMLSchema"（XSD 命名空间）而非 ""/"##default"**；name() 无默认值（必填）。
