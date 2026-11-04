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
| P0 | indexed-only（null,null,ir,iw）：数组访问器 null、propertyType null、索引类型解析 | testIndexedOnlyMethodCtor |
| P0 | 全 null Method 构造器合法：两个类型 null、四个访问器 null、name 保留 | testAllNullMethodCtor |
| P0 | array-only（gr,sw,null,null）→ IE `type mismatch between indexed and non-indexed methods`（索引类型 null 与数组类型冲突，实测） | testArrayOnlyMethodCtorThrows |
| P0 | 类型不匹配矩阵：Method 构造器 Integer[]/String → IE；(name,Class) 在不匹配 bean 上 → IE（消息为 `Method not found: setVals`，查找失败掩盖了不匹配）；indexed-only bean 的 2 参构造器 → IE `Method not found: getNames`（2 参构造器要求非索引访问器）；6 参 null 数组名 + 显式索引名可用 | testMismatchTypeRules |
| P0 | 索引方法负例：非 int 下标（读/写）、1 参写、void 索引读 → IE；非 void 返回值的索引写合法（类型取自索引读） | testIndexedSetterNegatives |
| P1 | equals 族：同构造相等；清除索引读后不等；**PD/IPD 不对称**（IPD.equals(PD)=false 而 PD.equals(IPD)=true，实测记录） | testEqualsFamily |
| P1 | 继承 PropertyDescriptor 能力：bound/constrained/propertyEditorClass/值表 | testInheritedPropertyFeatures |
| P1 | **非 Serializable** → NotSerializableException | testNotSerializable |

### 追加实测细节（JDK-8 与 JDK-27 一致）
- 2 参构造器委托 6 参构造器（全常规名），**要求存在非索引（数组级）访问器**：只有索引访问器的
  bean 抛 `Method not found: getXxx`。
- findIndexedPropertyType 末尾检查：propertyType 非 null 时必须是 indexedPropertyType 的数组，
  indexedPropertyType 为 null（如只给数组方法）同样抛 type mismatch——array-only 描述符无法
  用公共 Method 构造器表达。
- IPD.equals 只认 IndexedPropertyDescriptor；基类 PD.equals 见到同名同方法的 IPD 返回 true
  ——非对称但为两侧一致的实测行为，按实际断言。
