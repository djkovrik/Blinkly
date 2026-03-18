package com.sedsoftware.blinkly.domain.model

sealed class ComponentOutput {

    sealed class Common : ComponentOutput() {
        data class ErrorCaught(val throwable: Throwable) : Common()
    }
}
