# SubjectDomainCombiner 设计案

## 1. API 功能
`javax.security.auth.SubjectDomainCombiner`（JDK-27 已标记 `@Deprecated(forRemoval=true)`）实现
`java.security.DomainCombiner`，曾用于把 `Subject` 的 Principals 动态注入 `ProtectionDomain`。
- 构造器 `SubjectDomainCombiner(Subject subject)`。
- `getSubject()` 返回关联 Subject。
- `combine(ProtectionDomain[] currentDomains, ProtectionDomain[] assignedDomains)`：
  对非静态 current 域创建携带 Subject principals 的新 `ProtectionDomain`，追加 assigned 域；
  current 域为空时直接返回 assigned 数组（引用不变）。

来源：`~/git/jdk25u-dev/src/java.base/share/classes/javax/security/auth/SubjectDomainCombiner.java`。

## 2. Java 用户如何使用
```java
Subject s = new Subject();
s.getPrincipals().add(new X500Principal("CN=Alice"));
SubjectDomainCombiner sdc = new SubjectDomainCombiner(s);
ProtectionDomain[] combined = sdc.combine(currentDomains, assignedDomains);
// combined[0..n-1] 为注入 principals 的新域，combined[n..] 为 assigned 域
```
典型链路：`new SubjectDomainCombiner(subject) → getSubject() → combine(current, assigned) → 检查 getPrincipals()`。

## 3. 开源代码实际用法调研
- JDK 源码：构造器若 subject 只读则捕获 principal 数组；`combine` 对 `staticPermissionsOnly()` 的域原样保留、
  其余域用相同 CodeSource/Permissions/ClassLoader 加 principals 新建；current 为空时返回 assigned 引用；
  非只读 subject 变更会清空缓存。
- JDK 自带测试 `test/jdk/javax/security/auth/SubjectDomainCombiner/Regression.java`：
  用 `new ProtectionDomain(codeSource, null, null, null)` 建动态域，断言 combine 后域被替换、assigned 保留、
  长度=current+assigned（本测试调研过该文件）。
- `Optimize.java` 同目录用于去重/静态域行为。

## 4. 测试用例设计 + 自动化实现
黑盒断言（`SubjectDomainCombinerTest.java` 的 `testBlackBox()`）：
- `getSubject` 返回关联 Subject；null subject → `NullPointerException`。
- 空 current：`combine(null, assigned)`、`combine(empty, assigned)` 返回 assigned 同一引用；`combine(null,null)` 为 null。
- 动态 current + assigned：长度=current+assigned、assigned 保留、current 域被替换、
  CodeSource 保留、新域 `getPrincipals()` 携带 subject 的 principal。
- 只读 subject 同样注入 principal；仅 current 无 assigned 时长度=current。
白盒补齐：`public`、实现 `DomainCombiner`、父类 `Object`、`@Deprecated`。
