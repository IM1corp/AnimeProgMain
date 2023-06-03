package com.imcorp.animeprog.Default

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.fragment.app.Fragment
import com.imcorp.animeprog.MainActivity.MainActivity
import com.imcorp.animeprog.MainActivity.fragments.Adapters.Separator
import java.util.*
import kotlin.collections.ArrayList

public fun<T> any(array: List<T>, contains: Contains<T>): T? = array.firstOrNull(contains::booleanF)
public fun<T> where(array: List<T>,contains: Contains<T>) =(array.filter(contains::booleanF) as (ArrayList<T>))

public fun fillStringArray(array:Array<String>,fillValue:String,from_index:Int=0,to_index:Int=-1){
    array.fill(fillValue,from_index,(if(to_index!=-1) to_index else array.size))
}
public fun<FROM,TO> selectByFunc(array: List<FROM>, func: GetProp<FROM,TO>) = ArrayList<TO>(array.size).apply{
    for(item in array){
        add(func.getProp(item));
    }
}
public fun<T> sortBy(array: ArrayList<T>,func:Sort<T>):ArrayList<T> {
    array.sortWith(func::getIndex)
    return array
}
public fun<T> sortBy(array: LinkedList<T>,func:Sort<T>):LinkedList<T> {
    array.sortWith(func::getIndex)
    return array
}
public fun stringJoiner(array:List<CharSequence>, separator: String):String{
    return array.joinToString(separator)
}
public fun<T> indexOfSet(set:Set<T>,el:T): Int =set.indexOf(el)
public interface Contains<T> { public fun booleanF(objects: T): Boolean }
public interface GetProp<FROM, TO> {public fun getProp(element: FROM): TO; }
public interface Sort<FROM> {public fun getIndex(element1: FROM,element2: FROM): Int; }
fun Context.getColorAttr(@AttrRes attribute:Int) = (TypedValue()).run {
        this@getColorAttr.theme.resolveAttribute(attribute, this, true)
        this.data
    }