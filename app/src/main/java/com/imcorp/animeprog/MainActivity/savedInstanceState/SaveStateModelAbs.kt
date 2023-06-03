package com.imcorp.animeprog.MainActivity.savedInstanceState

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class SaveStateModelAbs: ViewModel() {
    open val data = MutableLiveData<Bundle>()
}