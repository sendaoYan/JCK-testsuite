# java.beans.IndexedPropertyDescriptor 测试设计案

> 覆盖目标：完整契约（三种构造器、数组类型 vs 索引类型、索引读写方法、异常、equals/hashCode）。

## 1. API 功能
- 构造器：`(name, beanClass)`、`(name, beanClass, read, write, iread, iwrite)`、`(name, read, write, iread, iwrite)`。
- 访问：`getIndexedReadMethod`/`setIndexedReadMethod`、`getIndexedWriteMethod`/`setIndexedWriteMethod`、`getIndexedPropertyType`。
- 继承 PropertyDescriptor 的 `getPropertyType`（数组类型）。

## 2. 使用方式
- 描述一个数组属性 `items`：`getItems()`/`setItems(int[])` + `getItems(int)`/`setItems(int,int)`。

## 3. 参考来源
- Java SE 8/27 javadoc（`java/beans/IndexedPropertyDescriptor.html`）。
- OpenJDK `test/jdk/java/beans/Introspector` 的索引属性回归；JCK `api/java_beans/IndexedPropertyDescriptor`。

## 4. 测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 基本构造器：数组类型 int[]、索引类型 int、四个方法 | testBasicConstructor |
| P0 | 6 参数（方法名）构造器 | testMethodNameConstructor |
| P0 | 5 参数（Method 对象）构造器 + 方法身份 | testMethodPairConstructor |
| P0 | setIndexedRead/WriteMethod 更新、null 清空、坏方法→IE | testSetters |
| P1 | null 名→IE、equals/hashCode | testExceptionsAndEquals |

## 关键规范细节
- 索引方法校验：读方法 1 个 int 参数、写方法 2 个参数且首个为 int，否则 IntrospectionException。
- 数组组件类型必须等于索引类型，否则 `type mismatch between indexed and non-indexed methods`。
- **不能连续清空索引读写两个方法**：清掉一个后数组类型仍在而索引类型变 null，第二次清空会触发 type mismatch。测试中对每个方法用独立实例单独清空。
