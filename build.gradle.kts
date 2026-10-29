// Дээд түвшний build файл: plugin-уудын хувилбарыг энд нэг удаа зарлана.
// (Lab6 inventory-app-тай ижил хувилбарууд — таны компьютер дээр аль хэдийн татагдсан.)
plugins {
    id("com.android.application") version "8.8.0" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
    // KSP — Room-ийн @Dao, @Database-аас код үүсгэдэг хэрэгсэл
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
}
