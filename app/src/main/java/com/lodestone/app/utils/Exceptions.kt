package com.lodestone.app.utils

sealed interface LocalReadWriteException

data class UnknownException(val origin: Throwable) : LocalReadWriteException
