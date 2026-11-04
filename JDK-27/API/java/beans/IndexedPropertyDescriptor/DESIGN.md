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

## 5. 追加深化（top-up）设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | indexed-only（null,null,ir,iw）；全 null 构造器（两类型 null、四访问器 null、name 保留） | testIndexedOnlyMethodCtor/testAllNullMethodCtor |
| P0 | array-only（gr,sw,null,null）→ IE type mismatch（索引类型 null 与数组类型冲突） | testArrayOnlyMethodCtorThrows |
| P0 | 不匹配矩阵：Method 构造器 Integer[]/String → IE；不匹配 bean 的 2 参构造器 → IE（消息 `Method not found: setVals`）；indexed-only bean 的 2 参构造器 → IE `Method not found: getNames`（2 参要求数组级访问器）；6 参 null 数组名可用 | testMismatchTypeRules |
| P0 | 非 int 下标（读/写）、1 参写、void 索引读 → IE；非 void 索引写合法 | testIndexedSetterNegatives |
| P1 | equals 族 + **PD/IPD 不对称**（IPD.equals(PD)=false、PD.equals(IPD)=true，实测记录）；继承能力；非 Serializable | testEqualsFamily/testInheritedPropertyFeatures/testNotSerializable |

### 追加实测细节（双侧一致）
- 2 参构造器委托 6 参（全常规名），要求存在数组级访问器；只给数组方法不给索引方法时
  indexedPropertyType 为 null 与数组类型冲突 → type mismatch（array-only 无法用公共构造器表达）。
- IPD.equals 只认 IndexedPropertyDescriptor；基类 PD.equals 对同名同方法 IPD 返回 true——
  非对称但双侧一致，按实际断言。
