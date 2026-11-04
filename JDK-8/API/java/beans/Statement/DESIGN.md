# java.beans.Statement — 设计案（JDK-8）

## 一、API 功能
Statement 表示"对 target 调用 methodName(arguments)"的原语语句（Expression 的基类）。
- 构造器 `(Object target, String methodName, Object[] arguments)`：null arguments 归一为空数组；参数数组防御性克隆。
- `getTarget()/getMethodName()/getArguments()`（每次克隆）；`execute()`：按名查找方法并调用。
- 派发特例：Class 作 target（静态方法 + Class.class 实例方法 + "new"→构造器 + 数组类 "new"→按实参个数建数组）；数组 target 的 "get"/"set" 映射 `java.lang.reflect.Array.get/set`；`Class.class,"forName"` 特例。

## 二、使用方式
`new Statement(list, "add", new Object[]{"x"}).execute()` 反射式调用；XMLEncoder 持久化格式的底层执行原语。

## 三、参考来源
- Oracle Java SE 8 javadoc：java.beans.Statement（静态方法/保留名 "new"/数组 get·set 特例、@throws 契约）。
- JavaBeans 规范 1.01-A（方法/属性命名约定与持久化）。
- OpenJDK 回归思路：`test/jdk/java/beans/Statement/Test4530962.java`（方法查找）、`Test6707226.java`、`Test6788531.java`（null 处理）、`ClassForName/`（ClassFinder 特例）。代码全部自写。

## 四、测试用例设计 + 易错规范细节
测试方法与覆盖（61 断言）：
1. **testWhiteBox**：类（非接口）、public、公开方法集合 {execute,getTarget,getMethodName,getArguments}、不覆盖 equals（恒等语义）。
2. **testAccessorsAndCtor**：三个 getter 恒等返回；构造器克隆 args；getArguments 每次新克隆且改动无效；null args → 空数组；构造器不做校验（null target/methodName 延迟到 execute 抛）；参数元素按引用保留。
3. **testExecuteInstanceSideEffects**：List.add/clear（原有检查保留）；StringBuilder append(String)+append(int) 链式副作用；setText；`execute() 两次则方法调用两次`（计数器）；纯调用可重复执行；List.remove(int)（Integer 实参→int 形参，接口方法派发）。
4. **testExecuteStaticAndClassTargets**：Class target 调静态 setter（自有静态字段，测后恢复）；Math.max 执行无异常；String.class getSuperclass（Class.class 实例方法）；"new" 构造形式经 execute()。
5. **testArrayOperations**（易错）：数组 "set" 写 String[]/int[]（Integer 拆箱）/boolean[]；"get" 经 execute；越界 set/get → ArrayIndexOutOfBoundsException；索引非 Integer → ClassCastException；元素类型不符 → IllegalArgumentException("array element type mismatch")；"length" → NoSuchMethodException。
6. **testExceptionMatrix**：null target / null methodName → NPE（execute 时）；未知方法 → NoSuchMethodException；**属性名风格 "text" 不翻译成 setText → NoSuchMethodException**；**public 字段名 "pub" 无字段回退 → NoSuchMethodException**；被调方法异常同实例透传（ISE token）；null→primitive 形参 → IllegalArgumentException；包私有构造器 "new" → IllegalAccessException。
7. **testToString**：String target/实参加引号；实参按非限定类名渲染（Integer）；endsWith ");"；null target 与 null 实参渲染为 "null.m(null);"；嵌套类 target 渲染为 `StatementTest$Bean` 形式。

### 易错规范细节（实证记录）
- 异常类型（双侧一致）：NoSuchMethodException（查无方法）、NPE（null target/methodName，execute 时）、AIOOBE（数组索引）、CCE（索引类型）、IAE（元素类型不符、null→primitive）。
- Statement **不支持**属性名缩写与公共字段访问（与 Spring BeanWrapper 等不同），javadoc 中 `a.foo` 记法仅为文档缩写——已作负例固定。
- 被调方法抛出的异常经 `InvocationTargetException` 解包后**原样抛出**（同实例），仅 IllegalAccessException 在"反射无法直接调用"这一支被包装成普通 Exception（本套触发的是构造器支路，实测原样 IAE 透传）。
- execute() 语义上是"每次都执行"；幂等性由被调方法决定，测试用计数器区分。
