package com.romanurdu.pdfreader

import android.app.Application
import com.romanurdu.pdfreader.dictionary.DictionaryEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RomanUrduReaderApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Pre-warm local dictionary in background for instant lookups
        CoroutineScope(Dispatchers.IO).launch {
            DictionaryEngine(this@RomanUrduReaderApp).initialize()
        }
    }
}
