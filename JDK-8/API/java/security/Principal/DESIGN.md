# Principal — 设计案

## 1. API 功能
主体接口：getName() + equals/hashCode/toString 重声明 + **implies(Subject) default 方法**（成员判定）。5 个声明方法。

## 2. 使用方式
X500Principal 是旗舰实现；自定义 lambda/匿名实现最小只需 getName。ProtectionDomain 携带 Principal[]；Subject.getPrincipals 聚合。

## 3. 参考来源
- javadoc `java.security.Principal`（implies 默认语义：this 是否 subject 的成员）。
- 双侧 javap（JDK-8 与 JDK-27 implies 均为 default；老 javap 不打印 default 关键字，反射 isDefault() 为准）。

## 4. 测试用例设计 + 易错规范细节
- 结构：恰 5 声明方法；implies 是 default、返回 boolean；getName 返回 String。
- implies 默认语义矩阵：Subject 含本 principal → true；不含 → false；null Subject → false（X500Principal 实测，双侧一致）。
- 最小实现：lambda/匿名 getName；默认 toString 含实现类名。
- X500Principal 值语义：RFC2253/规范名、equals 对称、hashCode 一致、异名不等、equals(null)/异类型 false、DER getEncoded 往返 equals。
- 组合：ProtectionDomain.getPrincipals 元素身份保持；Identity instanceof Principal。
- 易错点：implies 的参数是 javax.security.auth.Subject（不是 Principal）；X500Principal.getName() 默认 RFC2253 无空格规范形。
