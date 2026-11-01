# Kancentration ProGuard / R8 Rules

# Preserve all serialized data models and localized Kanji structures
-keepclassmembers class com.example.kancentration.data.model.** {
    <fields>;
    <init>(...);
}

-keep class com.example.kancentration.data.model.** {
    *;
}

# Preserve JSON parsing data classes & elements
-keepclassmembers class com.example.kancentration.data.repository.SimpleJsonParser$** {
    <fields>;
    <init>(...);
}

-keep class com.example.kancentration.data.repository.SimpleJsonParser$** {
    *;
}

-keep class com.example.kancentration.data.repository.KanjiQuadParser {
    *;
}

-keep class com.example.kancentration.data.repository.AssetAndFileKanjiRepository {
    *;
}

# Preserve ViewModels and Factory reflection
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

-keep class com.example.kancentration.ui.viewmodel.** {
    *;
}

# Jetpack Compose Rules
-keepclassmembers class androidx.compose.ui.platform.AndroidComposeView {
    *;
}
