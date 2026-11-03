# java.rmi.MarshalledObject 行为测试设计

## 1. API 功能
`MarshalledObject<T>` 把对象序列化为字节数组并保持其「注解」（location）字节。final、Serializable。
- 构造器 `MarshalledObject(T)`（throws IOException）。
- `T get()` 反序列化返回副本（throws IOException, ClassNotFoundException）。
- `hashCode()`/`equals()` 基于序列化字节内容（equal 对象 hash 相等）。

## 2. Java 用户如何使用
- 跨 RMI 传递一个「已序列化的对象」而不立刻反序列化：`MarshalledObject<Foo> mo = new MarshalledObject<>(foo);` 之后 `Foo f = mo.get();`。
- 每次 `get()` 返回新反序列化的独立对象（深拷贝语义）。

## 3. 开源代码实际用法调研
- JDK 测试 `test/jdk/java/rmi/MarshalledObject/compare/HashCode.java`（bug 4105900）与 `MOFilterTest.java` 验证 get/equals/hashCode。
- JDK 测试 `test/jdk/java/rmi/RemoteException/chaining/Chaining.java` 用 `new MarshalledObject(pair).get()` 做异常往返。
- 类定义 `src/java.rmi/share/classes/java/rmi/MarshalledObject.java`。

## 4. 测试用例设计 + 自动化实现
黑盒部分（MarshalledObjectTest.testBlackBox）：String 往返；int[] 往返且 get 返回副本（非同一数组、两次 get 不同）；equals 自反/同内容/不同内容/null/非 MarshalledObject；hashCode 稳定且 equal 对象相等；null 往返。
白盒部分（testWhiteBox）：final、Serializable、public 方法 get/equals/hashCode。
