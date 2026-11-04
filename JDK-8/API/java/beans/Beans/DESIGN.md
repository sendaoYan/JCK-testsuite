# java.beans.Beans — 设计案（JDK-8）

## 一、API 功能
`java.beans.Beans` 是 JavaBeans 的入口工具类（final 语义的具体 public 类，全部静态方法、无字段）：
- `instantiate(ClassLoader, String)` / `instantiate(ClassLoader, String, BeanContext)` / `instantiate(ClassLoader, String, BeanContext, AppletInitializer)`（第 3 个重载为 **JDK-8 独有**，JDK-9 随 AppletInitializer 一并删除）：按二进制名实例化 Bean。流程：先按 `beanName.replace('.','/') + ".ser"` 在 classloader 资源里找序列化形式，找不到则 `Class.forName` + 公有无参构造器；实例化失败统一**重映射为 `ClassNotFoundException`**（cause 保留原始 `InstantiationException` / `IllegalAccessException`），声明 `throws IOException, ClassNotFoundException`。BeanContext 参数非 null 时实例会被 `add` 进上下文。
- `isInstanceOf(Object, Class)` / `getInstanceOf(Object, Class)`：委托 `Introspector.isSubclass(bean.getClass(), targetType)`——沿**超类链 + 接口闭包**判定，**不做数组协变**（`String[]` 对 `Object[]` 为 false，但对 `Cloneable/Serializable` 为 true）。当前 JDK 把所有 bean 视作简单 bean：`getInstanceOf` 恒返回原对象（即使类型不匹配、即使 targetType 为 null）。
- `isDesignTime()/setDesignTime(boolean)`、`isGuiAvailable()/setGuiAvailable(boolean)`：ThreadGroupContext（AppContext）级开关。JDK-8 侧两个 setter **声明 `throws SecurityException`**（JDK-9+ 删除该声明，仅签名差异）。headless 环境初值 `isGuiAvailable()==false`（测试只做相对断言）。

## 二、使用方式
IDE/Bean 装配工具：设计期 `Beans.setDesignTime(true)`；`Beans.instantiate(cl, "com.acme.Foo")` 按名实例化（可挂入 BeanContext）；`isInstanceOf/getInstanceOf` 查询"目标类型视图"（为未来多态 bean 预留，当前为恒等实现）。

## 三、参考来源
- Oracle javadoc（Java SE 8，java.beans.Beans）：instantiate 的 .ser 资源查找顺序、异常重映射语义、isInstanceOf/getInstanceOf 说明。
- JavaBeans 规范 1.01-A（§5 "Beans 入口"、§8 多态视图/设计期协议）。
- OpenJDK 8 `src/share/classes/java/beans/Beans.java`（isInstanceOf → Introspector.isSubclass 委托、异常 remap 注释）与 `test/jdk/java/beans` 回归思路（本测试未拷贝任何代码）。
- 本文所有行为断言均经 /tmp 探针在 jdk8u372-b07 实测（双侧 jdk-27.0.1.0.2+1 对照一致）。

## 四、测试用例设计 + 易错规范细节
测试文件 `BeansTest.java`（89 断言），方法分组：
- `testWhiteBoxStructure`：public 具体类、直继承 Object、无接口/无泛型/无字段、单一 public 无参构造器、**全部声明方法均为 static**；每个 `instantiate` 重载声明 IOException+ClassNotFoundException。
- `testInstanceOfMatrix`：精确类/超类/双接口/Number/数组精确类型/数组→Object（经超类链）；**协变陷阱**：`String[]`→`Object[]` 与 →`CharSequence[]` 均为 false，`String[]/int[]`→`Cloneable`、→`Serializable` 为 true；原始类型与 void 目标均 false；null targetType false 无异常；null bean → **NPE**（JDK-27 同）。
- `testGetInstanceOf`：各类 targetType（含不匹配、null、null bean）全部恒等返回原引用。
- `testDesignTimeToggle`：默认 false、true/false 往返、幂等、**finally 恢复 false**。
- `testGuiAvailableRelative`：记录初值 → setGuiAvailable(false) 两次 → **恢复初值**（headless 下绝不置 true）。
- `testInstantiatePositive`：嵌套类二进制名 `BeansTest$SimpleBean`（null loader 与显式 loader）、每次新实例、存在 `<Bean>BeanInfo` 类不干扰、`java.lang.String` 公有无参构造器。
- `testInstantiateNegative`（负例精确异常表，JDK-8 = JDK-27 实测一致）：
  | 触发 | 异常 |
  |---|---|
  | 不存在类名 / 空 / `..Bad` / 抽象类 / 私有构造器 / 接口名 / 数组类名 | `ClassNotFoundException`（后四者 cause 分别为 InstantiationException / IllegalAccessException / InstantiationException / InstantiationException） |
  | `instantiate(null, null)` | `NullPointerException`（未声明） |
- `testInstantiateWithBeanContext`：bean 被加入 BeanContext（contains/size）、二次 add、null context 合法。
- `testDesignModeStateIndependence`：**Beans 全局开关与 BeanContextSupport 自身 flag 相互独立**（实测两侧一致：new BeanContextSupport() 不从 Beans 取种子值）。
- `testVersionSpecificSignatures`（JDK-8）：`getDeclaredMethod("instantiate", CL, String, BeanContext, AppletInitializer)` 存在；instantiate 重载恰 3 个；`setDesignTime/setGuiAvailable` 的 `getExceptionTypes()` 含 `SecurityException`。
- 易错点：①isInstanceOf 不是 `Class.isInstance`（数组协变差异）；②instantiate 的失败重映射让 InstantiationException 等永远不直接抛出；③setDesignTime 是 JVM 级（ThreadGroup）状态，必须 finally 恢复，否则污染同 JVM 后续测试；④jtreg 每测试新 JVM，但同文件内多个 test 方法共享状态，顺序敏感断言（默认 false）须放在前。
