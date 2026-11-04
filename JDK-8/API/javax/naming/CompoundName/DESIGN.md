# javax.naming.CompoundName — 测试设计

## 1. API 功能
按 Properties 描述的语法解析的分层名：`jndi.syntax.direction`（flat/left_to_right/right_to_left）、
`separator`、`escape`、`beginquote/endquote`、`trimblanks`；实现全部 Name 操作。

## 2. Java 用户如何使用
服务提供方定义语法（如 DNS 用 "." flat，LDAP 用 ","），用户拿 NameParser 得到 CompoundName 后按组件操作；
`new CompoundName("a.b.c", syntax)` → getPrefix/getSuffix/add。典型：语法属性复用同一 Properties 解析多个名字。

## 3. 开源代码实际用法调研
- JDK `javax/naming/CompoundName.java` 自身 main() 用 dotSyntax 演示；Harmony JNDI 对 CompoundName 的方向语义有系统测试。
- Apache DS LdapName（前 RDN 时代）与 org.apache.harmony.jndi.tests.javax.naming.CompoundNameTest 覆盖 flat/rtl 语法。
- `com.sun.jndi.toolkit.ctx.ComponentContext` 以 CompoundName 做原子名切分。

## 4. 测试用例设计（CompoundNameTest.java）
- 白盒：public、实现 Name、(String,Properties) 构造器异常声明、方法清单。
- 黑盒：flat（无 direction）单组件 + add 抛精确 message；right_to_left get(0)=最右、toString 往返；
  escape 保护分隔符；beginquote 保护分隔符 + 未闭合引用精确 message；ltr 下 add/add(int)/remove/addAll 链；
  **equals 含语法**（不同 direction 不等）；clone 独立；compareTo。
