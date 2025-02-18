// "Move 'SomeOptInAnnotation' opt-in requirement from getter to property" "true"
// RUNTIME_WITH_SCRIPT_RUNTIME

@RequiresOptIn
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_GETTER)
annotation class SomeOptInAnnotation

class Foo(val value: Int) {
    val bar: Boolean
        <caret>@SomeOptInAnnotation get() = value > 0
}
