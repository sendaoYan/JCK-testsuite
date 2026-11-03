# com.sun.security.auth.PrincipalComparator 测试设计案

## 1. API 功能
`PrincipalComparator` 是一个**接口**（非 Comparator，也非 PrivilegedAction）。JDK-27 javap 实测仅一个方法：
- `boolean implies(javax.security.auth.Subject subject)`：判定当前对象是否「蕴含」给定 Subject（例如组/角色成员关系）。

## 2. Java 用户如何使用
作为函数式接口用 lambda 实现，典型「组蕴含」：
```java
PrincipalComparator group = subject -> {
    for (Principal p : subject.getPrincipals()) {
        if ("jck-group-member".equals(p.getName())) return true;
    }
    return false;
};
boolean ok = group.implies(subject);
```

## 3. 开源代码实际用法调研
- 实现基准：`~/git/jdk25u-dev/src/jdk.security.auth/share/classes/com/sun/security/auth/PrincipalComparator.java`。
- 说明：其 javadoc 明确「实现该接口的类通常也实现 `java.security.Principal`，但不是必须」。
（网络不可用，以 JDK 源码为权威来源；brief 中提到的 Comparator/PrivilegedAction 结构经 javap 确认不成立，已按实际接口修正。）

## 4. 测试用例设计 + 自动化实现
黑盒断言在 `PrincipalComparatorTest.java` 的 `testBlackBox()`：
- lambda 可作为 `PrincipalComparator` 使用（函数式接口）。
- 空 Subject 不蕴含；含匹配主体时蕴含；含其他主体时不蕴含；null 由实现自行处理（返回 false）。
