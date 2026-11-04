# java.lang.annotation.Inherited — 设计方案

## 1. API 功能
标记元注解（无成员）：标注的注解在类继承时被子类自动“继承”——子类查询该注解时能查到超类上的用法。
- 仅作用于类继承；接口/方法不继承。
- 自身 `@Inherited @Retention(RUNTIME) @Target(ANNOTATION_TYPE)`

## 2. Java 用户如何使用
```java
@Inherited @Retention(RUNTIME) @interface Transactional { }
@Transactional class ServiceBase { }
class ServiceSub extends ServiceBase {}
ServiceSub.class.isAnnotationPresent(Transactional.class); // true
ServiceSub.class.getDeclaredAnnotation(Transactional.class); // null
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/annotation/Inherited.java`：javadoc 明确“only applies to class inheritance”。
- Spring `@Component`（及派生 `@Service` 等）带 `@Inherited`，故父类标注组件注解子类也被组件扫描识别（`ClassPathScanningCandidateComponentProvider`）。
- JUnit/Jakarta 事务注解（`@Transactional`）依赖 `@Inherited` 让代理子类保持事务语义。

## 4. 测试用例设计 + 自动化实现
白盒：注解类型、零成员、自注解、RUNTIME 保留。
黑盒（Base/Child/GrandChild/覆写子类四级继承链）：
- 子类 `getAnnotation`/`isAnnotationPresent` 看到父类注解且成员值一致；孙类同样；
- 子类自有用法遮蔽父类；
- 对照注解（无 @Inherited）不继承；
- `getDeclaredAnnotation`/`getDeclaredAnnotations` 不走继承路径；
- `getAnnotations` 包含继承注解；
- 接口实现类不适用继承。
对应 `InheritedTest.java` 的 `testBlackBox()`。
