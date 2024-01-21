[![](https://jitpack.io/v/0xera/parcelable.svg)](https://jitpack.io/#0xera/parcelable)


Stream (Best performance):
```kotlin
val largeData: Parcelable = //...
// or
val largeData: Serializable = //...
 
// send    
Intent().apply {
    setComponent(APP_PACKAGE_NAME, ACTIVITY_PACKAGE_NAME)
    putExtra(LARGE_DATA_STREAM, largeData.parcelableInputStream())
}

// receive
intent.getParcelableExtra<ParcelableSlice<Parcelable>>(LARGE_DATA_STREAM)?.join()
```

Slice (Worst performance):
```kotlin
val largeData: Parcelable = //...
// or
val largeData: Serializable = //...
 
// send    
Intent().apply {
    setComponent(APP_PACKAGE_NAME, ACTIVITY_PACKAGE_NAME)
    putExtra(LARGE_DATA_SLICE, largeData.parcelableSlice())
}

// receive
intent.getParcelableExtra<ParcelableInputStream<Parcelable>>(LARGE_DATA_SLICE)?.read()
```

Pipe (Be careful about process lifecycle):
```kotlin
val largeData: Parcelable = //...
// or
val largeData: Serializable = //...

val (reader, writer) = ParcelableStreamPipe<Parcelable>()

// send
Thread {
  writer.write(largeData)
}.start()
    
Intent().apply {
    setComponent(APP_PACKAGE_NAME, ACTIVITY_PACKAGE_NAME)
    putExtra(LARGE_DATA_PIPE, reader)
}

// receive
Thread {
  intent.getParcelableExtra<ParcelableInputStream<Parcelable>>(LARGE_DATA_PIPE)?.read()
}.start()
```


Add dependencies:
```kotlin
dependencyResolutionManagement {
    repositories {
        maven("https://jitpack.io")
    }
}

dependencies {
    implementation("com.github.0xera.parcelable:stream:0.1")
    implementation("com.github.0xera.parcelable:slice:0.1")
}
```