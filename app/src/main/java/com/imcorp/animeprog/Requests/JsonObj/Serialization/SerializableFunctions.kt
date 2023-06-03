package com.imcorp.animeprog.Requests.JsonObj.Serialization

import android.util.Log
import com.imcorp.animeprog.Config
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.NullPointerException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible


val variablesMap: HashMap<KClass<*>, HashMap<String, IProperty<Any>>> = HashMap()
fun <T : Any> getJsonVariables(cls: KClass<out T>): Map<String, IProperty<T>>{

    return (if(cls in variablesMap) variablesMap[cls] else buildVariables(cls)) as Map<String, IProperty<T>>;
}
fun <T : Any> buildVariables(cls: KClass<out T>): HashMap<String, IProperty<T>>{
    val map = HashMap<String, IProperty<T>>()
    for (property in cls.memberProperties) { //cls.members
        if (property !is KMutableProperty1<*, *>) continue

        property.findAnnotation<SerializableField>()?.let {
            val name = it.jsonName
            val prop = property as KMutableProperty1<T, Any?>
            val cls = property.returnType;
//            val cls = (prop.javaField!!.type as Class<Any>).kotlin;
            map[name] = IProperty.buildIProp(prop, cls)
//            Log.i("JSON_CODER","Property with name=${it.jsonName} found")
        }
    }
//    Log.i("JSON_PROP_FOUND", "$cls - ${map.size} ($map)")
    variablesMap[cls] = map as HashMap<String, IProperty<Any>>;
    return map

}

class IProperty<T: Any>(private val property: KMutableProperty1<T, Any?>?, val instanceClass: KClass<Any>,
                        val typeArguments: Array<IProperty<Any>>){
    fun setValue(obj: T, value: Any?) = this.property?.let{prop->
        try{
            prop.isAccessible = true
            if(this.instanceClass.java.isPrimitive) {
                prop.set(obj, when(this.instanceClass){
                    Int::class -> (value as Number).toInt()
                    Byte::class -> (value as Number).toByte()
                    Long::class -> (value as Number).toLong()
                    else->value
                })
            }
            else
                prop.set(obj, value)
        }catch (e: NullPointerException){
            Log.e("JsonError", "property ${this.instanceClass.simpleName}.${this.property!!.name} couldn't be null", e)
            throw JSONException("Invalid structure")
        }
//        this.property?.setValue(obj, property, !)
    }
    fun getValue(obj: T): Any? = this.property?.let{prop->
        prop.isAccessible = true
        return prop.get(obj)//(obj, property)
    }
    public companion object {
        fun <F : Any> buildIProp(prop: KMutableProperty1<F, Any?>?, cls: KType): IProperty<F> {
            return IProperty<F>(prop, cls.classifier as KClass<Any>, cls.arguments.map { c ->
                buildIProp<Any>(null, c.type!!)
            }.toTypedArray())

        }
    }
}
fun <T : Any> JSONObject.convertTo(to: KClass<T>): T{
    return this.convertToAny(to as KClass<Any>) as T
}
fun JSONObject.convertToAny(to: KClass<Any>): Any{
//    Log.i("JsonConvertTo", "$to $this")
//    val constructor = to.constructors.first{ it.parameters.isEmpty() }.also{it.isAccessible=true}
//    val instance = constructor.call()
    val instance = to.java.newInstance()
    val jsonProperties = getJsonVariables(to)
    for(key in this.keys()){
        jsonProperties[key]?.let{ prop->
            val value = generateValueFromJson(this[key], prop)
            prop.setValue(instance, value)
        }?:Log.e("JSON_NOT_FOUND", "Not found key '$key' in object $to - found $jsonProperties\n HOW TO FIX: add EMPTY constructor to the class AND check that this variable is used somewhere")
    }
    return instance

}
fun generateValueFromJson(value: Any, cls: IProperty<Any>): Any?{
    if(JSONObject.NULL == value) return null
    if(value is JSONObject){
        if(cls.instanceClass.isSubclassOf(HashMap::class)){
            val instance: HashMap<Any, Any?> = (cls.instanceClass.constructors.firstOrNull {
                it.parameters.size == 1 && it.parameters[0].type.isSubtypeOf(Int::class.createType())
            }?.also{it.isAccessible=true}?.call(value.length()) ?: cls.instanceClass.constructors.first{ it.parameters.isEmpty() }.also{it.isAccessible=true}.call()) as HashMap<Any,Any?>
            val keyArg = cls.typeArguments[0]
            val valueArg = cls.typeArguments[1]
            val isKeyString = keyArg.instanceClass == String::class
            for(i in value.keys()){
                val res = generateValueFromJson(value[i], valueArg)
                //95%
                if(isKeyString)
                    instance[i] = res
                else instance[formatNumber(i, keyArg)] = res
            }
            return instance
        }
        return value.convertToAny(cls.instanceClass)
    }
    else if(value is JSONArray){
        val arg = cls.typeArguments[0] //cls.java.componentType as Class<Any>

        val ans: Array<Any?> = java.lang.reflect.Array.newInstance(arg.instanceClass.java, value.length()) as Array<Any?>;//Array(value.length())
        for(i in 0 until value.length()) {
            ans[i] = (generateValueFromJson(value[i], arg))
        }
        return when(cls.instanceClass.java){
            ArrayList::class.java -> ArrayList(ans.toList())
            List::class.java -> ans.toList()
            LinkedList::class.java -> LinkedList<Any?>(ans.toList())
            else -> ans
        }
    }
    else if((value is Int || value is Long) && cls.instanceClass.isSubclassOf(Enum::class)){
        val enumClz = (cls.instanceClass as KClass<Enum<*>>).java.enumConstants as Array<Enum<*>>
        return enumClz.firstOrNull { it.ordinal == value } ?: {
            Log.e("EnumNotFound","Not found enum value=$value of class ${cls.instanceClass}")
            enumClz[0]
        }
    }
//    if((value is Int || value is Long) && cls.instanceClass !== ){
//
//    }
    return value
}
fun formatNumber(i: String, prop: IProperty<Any>): Any{
   return when(prop.instanceClass){
        Int::class -> Integer.parseInt(i)
        Float::class -> java.lang.Float.parseFloat(i)
        Double::class -> java.lang.Double.parseDouble(i)
        Long::class -> java.lang.Long.parseLong(i)
        Short::class -> java.lang.Short.parseShort(i)
        Byte::class -> java.lang.Byte.parseByte(i)
        else->throw IllegalArgumentException("Argument type $i of class $prop is not yet accessible")
    }
}
fun Any.toJSONObject(): Any = when(this) {
    is Int, is Long, is Double, is Float, is String, is Byte -> this
    is Enum<*> -> this.ordinal
    is JSONObject -> this
    is Map<*,*> -> JSONObject().apply {
        forEach { item ->
            this.put(item.key.toString(), item.value)
        }
    }
    is Array<*> -> JSONArray(this.map{ it?.toJSONObject()})
    is Iterable<*> -> JSONArray(this.map{ it?.toJSONObject()})
    else -> JSONObject().also{
        val variables = getJsonVariables(this::class)
        for (i in variables)
            it.put(i.key, i.value.getValue(this)?.toJSONObject())
    }
}
