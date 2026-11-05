# XmlType.DEFAULT 设计案（JDK-8 契约测试）

## 1. API 功能
factoryClass() 成员的默认值哨兵类：final 纯标记，表示"未指定工厂类"。

## 2. 使用方式
@XmlType(factoryClass = XmlType.DEFAULT.class) 显式等于默认；省略同效。

## 3. 参考来源
Oracle javadoc javax.xml.bind.annotation.XmlType、JSR 222、本仓库 DEFAULTTest/XmlTypeTest。

## 4. 测试用例设计 + 易错规范细节
- 结构：final、纯标记、单 public 构造、嵌套于 XmlType、身份 equals。
- 角色闭环：factoryClass().getDefaultValue()==DEFAULT.class；裸 @XmlType 与显式 DEFAULT 形态的注解实例回读同一哨兵；factoryMethod 默认 ""（无哨兵）。
- 易错点：与 XmlSchemaType.DEFAULT/XmlJavaTypeAdapter.DEFAULT 是三个不同嵌套类，勿混。
