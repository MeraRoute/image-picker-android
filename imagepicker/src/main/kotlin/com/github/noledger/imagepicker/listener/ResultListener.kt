package com.github.noledger.imagepicker.listener
internal interface ResultListener<T> {

    fun onResult(t: T?)
}
