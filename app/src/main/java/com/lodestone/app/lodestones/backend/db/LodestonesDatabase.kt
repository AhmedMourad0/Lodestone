package com.lodestone.app.lodestones.backend.db

import android.content.Context
import com.lodestone.app.db.LodestonesDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver

@Volatile
private var DRIVER_INSTANCE: SqlDriver? = null

@Volatile
private var DATABASE_INSTANCE: LodestonesDatabase? = null

fun lodestonesDatabase(driver: SqlDriver): LodestonesDatabase {
    return DATABASE_INSTANCE ?: synchronized(LodestonesDatabase::class) {
        DATABASE_INSTANCE ?: buildDatabase(driver).also { DATABASE_INSTANCE = it }
    }
}

fun sqliteDriver(context: Context): SqlDriver {
    return DRIVER_INSTANCE ?: synchronized(SqlDriver::class) {
        DRIVER_INSTANCE ?: buildDriver(context).also { DRIVER_INSTANCE = it }
    }
}

private fun buildDriver(context: Context): SqlDriver {
    return AndroidSqliteDriver(LodestonesDatabase.Schema, context, "lodestone.db")
}

private fun buildDatabase(driver: SqlDriver): LodestonesDatabase {
    LodestonesDatabase.Schema.create(driver)
    return LodestonesDatabase(driver)
}
