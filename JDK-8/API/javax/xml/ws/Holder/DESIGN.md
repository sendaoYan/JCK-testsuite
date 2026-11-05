# Holder 设计案（JDK-8 契约测试）

## 1. API 功能
javax.xml.ws.Holder<T> 是 OUT/IN-OUT 参数的持有者包装（JSR 224 §2.3.3）：Java 无输出参数语法，SEI 方法形参为 Holder 类型时，容器把响应消息中的对应部分写回 holder.value。final 类 + public 可变字段 value + Serializable。

## 2. 使用方式
典型路径：`Holder<Integer> out = new Holder<>(); sei.op("x", out); // out.value 被容器填充`。本测试：构造/字段读写矩阵、恒等 equals/hashCode 实测、writeObject→readObject 序列化复活、OUT/IN-OUT 语义模拟（方法内改 value 对调用方可见）。

## 3. 参考来源
Oracle javadoc（docs.oracle.com/javase/8/docs/api/javax/xml/ws/Holder.html）、JSR 224 (JAX-WS 2.2)、OpenJDK 8u 源码（rt.jar javax.xml.ws.Holder 无 equals/hashCode 覆写）、本仓库 HolderTest。

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵：() → value null；(T) → value 回读；null 值合法；任意引用类型（Integer/数组/List/Object）。
- **equals/hashCode 恒等语义（实测锁定，勿按值语义断言）**：Holder 未覆写 Object.equals/hashCode——`new Holder<>("v").equals(new Holder<>("v"))` 为 **false**，hashCode()==System.identityHashCode(h)；测试从结构面（无声明 equals/hashCode 方法）与行为面双重锁定。
- 序列化（P0）：Holder<String>("ser") writeObject→readObject → value "ser" 复活、反序列化实例≠原实例；null value 往返保 null；**载荷非 Serializable（new Object()）→ NotSerializableException**（字段透传）；ArrayList 载荷反序列化是深拷贝——改副本不影响原列表。
- 结构面：final、单泛型参数 T、public 非静态非 final 实例字段 value、恰 2 public 构造器。
- OUT/IN-OUT 语义：方法内写 holder.value 调用方可见；两个 Holder 互不影响。
- 坑：Java 泛型不做协变，Holder<Integer> 与 Holder<String> 不可比较（!= 需转 Object）；null Holder 引用访问 value 抛 NPE。
