package com.imcorp.animeprog.Requests.JsonObj.Serialization

import androidx.annotation.Keep
import java.lang.annotation.RetentionPolicy

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.PROPERTY_GETTER,
        AnnotationTarget.FUNCTION)
@Keep
public annotation class SerializableField(
        val jsonName: String,
) {

}