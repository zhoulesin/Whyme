---
name: "kotlin-code-style"
description: "提供符合安卓规范的Kotlin代码风格指南。当用户需要检查或应用Kotlin代码规范时调用。"
---

# Kotlin 代码规范（安卓版）

## 1. 命名规范

### 1.1 包名
- 使用小写字母，避免下划线
- 采用反向域名命名方式：`com.example.project`

### 1.2 类名
- 使用大驼峰命名法（PascalCase）
- 示例：`MainActivity`, `UserViewModel`

### 1.3 函数名
- 使用小驼峰命名法（camelCase）
- 示例：`getUserInfo()`, `calculateTotal()`

### 1.4 变量名
- 使用小驼峰命名法（camelCase）
- 常量使用全大写，单词间用下划线分隔：`MAX_COUNT`
- 私有变量建议使用下划线前缀：`_privateVariable`

### 1.5 枚举类
- 枚举值使用全大写，单词间用下划线分隔
- 示例：`enum class Status { ACTIVE, INACTIVE, PENDING }`

## 2. 代码风格

### 2.1 缩进
- 使用4个空格进行缩进
- 避免使用制表符

### 2.2 空行
- 函数之间使用一个空行分隔
- 逻辑块之间使用空行分隔
- 文件末尾保留一个空行

### 2.3 括号
- 左括号与前一行代码在同一行
- 右括号单独占一行
- 示例：
  ```kotlin
  fun calculate(a: Int, b: Int): Int {
      return a + b
  }
  ```

### 2.4 分号
- Kotlin 不需要分号，除非在同一行有多个语句

### 2.5 字符串
- 优先使用字符串模板：`"Hello, $name"`
- 多行字符串使用三重引号：`"""多行
  字符串"""`

## 3. 编码实践

### 3.1 空安全
- 优先使用可空类型和安全调用操作符：`obj?.method()`
- 使用 Elvis 操作符处理空值：`val result = nullableValue ?: defaultValue`
- 避免使用 `!!` 操作符

### 3.2 扩展函数
- 合理使用扩展函数提高代码可读性
- 示例：`fun String.toSnakeCase(): String { ... }`

### 3.3 数据类
- 对于仅存储数据的类，使用 `data class`
- 示例：`data class User(val id: Int, val name: String)`

### 3.4 密封类
- 对于有限数量的状态，使用 `sealed class`
- 示例：
  ```kotlin
  sealed class Result {
      data class Success(val data: Data) : Result()
      data class Error(val message: String) : Result()
  }
  ```

## 4. Android 特定规范

### 4.1 Activity 和 Fragment
- Activity 类名以 `Activity` 结尾：`MainActivity`
- Fragment 类名以 `Fragment` 结尾：`HomeFragment`
- 使用 `viewBinding` 替代 `findViewById`

### 4.2 布局文件
- 布局文件使用小写字母和下划线：`activity_main.xml`
- ID 使用小写字母和下划线：`@+id/text_view_title`

### 4.3 资源文件
- 字符串资源放在 `strings.xml`
- 颜色资源放在 `colors.xml`
- 尺寸资源放在 `dimens.xml`

### 4.4 协程
- 在 Android 中使用 `lifecycleScope` 和 `viewModelScope`
- 避免在主线程执行耗时操作

## 5. 代码审查要点

- 检查命名是否符合规范
- 确认空安全处理是否合理
- 验证资源使用是否正确
- 确保代码可读性和可维护性
- 检查是否遵循 Android 最佳实践

## 6. 工具和插件

- 使用 Android Studio 的 Kotlin 插件
- 启用 lint 检查
- 考虑使用 ktlint 进行代码风格检查

## 示例代码

```kotlin
// 正确的命名和风格
class UserProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileBinding
    private val viewModel: UserViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        binding.btnSave.setOnClickListener {
            saveUserProfile()
        }
    }
    
    private fun observeViewModel() {
        viewModel.user.observe(this) { user ->
            binding.tvUserName.text = user.name
            binding.tvUserEmail.text = user.email
        }
    }
    
    private fun saveUserProfile() {
        val name = binding.etName.text.toString()
        val email = binding.etEmail.text.toString()
        
        if (name.isNotEmpty() && email.isNotEmpty()) {
            viewModel.updateUser(name, email)
        } else {
            Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show()
        }
    }
}

// 数据类示例
data class User(
    val id: Int,
    val name: String,
    val email: String
)

// ViewModel 示例
class UserViewModel : ViewModel() {
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user
    
    fun updateUser(name: String, email: String) {
        // 更新用户信息的逻辑
    }
}
```