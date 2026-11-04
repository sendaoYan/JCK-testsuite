# java.beans.PropertyDescriptor 测试设计案

> 覆盖目标：完整契约（三种构造器 + 读写/只写/只读、精确 IntrospectionException、bound/constrained、
> 属性编辑器、equals/hashCode）。headless 安全。

## 1. API 功能
- 构造器：`(name, beanClass)`、`(name, beanClass, read, write)`、`(name, readMethod, writeMethod)`。
- 读写方法：`getReadMethod`/`setReadMethod`、`getWriteMethod`/`setWriteMethod`、`getPropertyType`。
- 标志：`isBound`/`setBound`、`isConstrained`/`setConstrained`。
- 编辑器：`setPropertyEditorClass`/`getPropertyEditorClass`/`createPropertyEditor`；`equals`/`hashCode`。

## 2. 使用方式
- 由 `Introspector` 或手动 `new PropertyDescriptor("foo", Bean.class)` 描述一个属性。
- 通过 `(name, beanClass, readMethodName, writeMethodName)` 指定非标准命名；null 表示只读/只写。

## 3. 参考来源
- Java SE 8/27 javadoc（`java/beans/PropertyDescriptor.html`）。
- OpenJDK `test/jdk/java/beans/PropertyDescriptor`（含 MethodProperty 等回归）。
- JCK runtime-8b `api/java_beans/PropertyDescriptor`。

## 4. 测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 三构造器 + 读写/只写/只读 + is* boolean 读取 | testBasicConstructor、testReadWriteConstructors |
| P0 | 精确异常：null/空名、null bean、缺失属性、缺失读写方法 | testConstructorExceptions |
| P0 | setReadMethod/setWriteMethod 更新类型、null 清空、类型不匹配→IE | testSettersAndType |
| P1 | bound/constrained、propertyEditorClass/createPropertyEditor（缺失/非编辑器返回 null） | testBoundConstrainedEditor |
| P1 | equals/hashCode 对称、不同名、null、非本类 | testEqualsHashCode |

## 关键规范细节
- `(name, beanClass, read, write)` 构造器对「指定了但找不到」的方法名有**回退**：找不到显式名时改用 baseName 推导的 `getX`/`isX`，因此 `("foo", Bean, "getNope", "setFoo")` 不抛异常而回退到 `getFoo`；只有推导方法也缺失才抛 `Method not found`。
- `setReadMethod` 校验：0 参数、返回值非 void、与现有写方法类型兼容，否则 IntrospectionException。
- `createPropertyEditor` 在编辑器类为 null 或非 PropertyEditor 时返回 null。
