# XmlJavaTypeAdapter.DEFAULT 设计案（JDK-8 契约测试）

## 1. API 功能
type() 成员的默认值哨兵类：final 纯标记，表示"适配器适配整个属性类型"。

## 2. 使用方式
@XmlJavaTypeAdapter(value = MyAdapter.class) 省略 type 即默认 DEFAULT。

## 3. 参考来源
Oracle javadoc javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter、JSR 222、本仓库 DEFAULTTest/adapters 系列测试。

## 4. 测试用例设计 + 易错规范细节
- 结构：final、纯标记、单 public 构造、身份 equals、嵌套于 XmlJavaTypeAdapter。
- 角色闭环：type().getDefaultValue()==DEFAULT.class；字段级注解（value=具体 adapter）回读 type==DEFAULT 且 value==MyAdapter.class。
- 易错点：value() 才是承载 adapter 类的成员；DEFAULT 仅作 type 哨兵。
