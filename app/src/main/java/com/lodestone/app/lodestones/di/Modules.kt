package com.lodestone.app.lodestones.di

import android.content.Context
import com.lodestone.app.db.LocationQueries
import com.lodestone.app.db.LodestonesDatabase
import com.lodestone.app.lodestones.backend.LodestonesRepository
import com.lodestone.app.lodestones.backend.LodestonesRepositoryImpl
import com.lodestone.app.lodestones.backend.db.lodestonesDatabase
import com.lodestone.app.lodestones.backend.db.sqliteDriver
import com.lodestone.app.lodestones.backend.location.LocationManager
import com.lodestone.app.lodestones.backend.location.LocationManagerImpl
import com.lodestone.app.lodestones.backend.directions.DirectionsManager
import com.lodestone.app.lodestones.backend.directions.DirectionsManagerImpl
import com.lodestone.app.lodestones.backend.sensors.SensorsManager
import com.lodestone.app.lodestones.backend.sensors.SensorsManagerImpl
import com.patloew.colocation.CoLocation
import com.squareup.sqldelight.db.SqlDriver
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
interface LodestoneBindingsModule {

    @Binds
    fun bindLodestonesRepository(
        impl: LodestonesRepositoryImpl
    ): LodestonesRepository

    @Binds
    @InternalApi
    fun bindLocationManager(
        impl: LocationManagerImpl
    ): LocationManager

    @Binds
    @InternalApi
    fun bindDirectionsManager(
        impl: DirectionsManagerImpl
    ): DirectionsManager

    @Binds
    @InternalApi
    fun bindSensorsManager(
        impl: SensorsManagerImpl
    ): SensorsManager
}

@Module
@InstallIn(ViewModelComponent::class)
object LodestoneProvidedModules {

    @Provides
    @Reusable
    @InternalApi
    fun provideLocationProvider(@ApplicationContext context: Context): CoLocation {
        return CoLocation.from(context)
    }

    @Provides
    @InternalApi
    fun provideSqliteDriver(@ApplicationContext context: Context): SqlDriver {
        return sqliteDriver(context)
    }

    @Provides
    @InternalApi
    fun provideLodestonesDatabase(@InternalApi driver: SqlDriver): LodestonesDatabase {
        return lodestonesDatabase(driver)
    }

    @Provides
    @Reusable
    @InternalApi
    fun provideLocationQueries(@InternalApi db: LodestonesDatabase): LocationQueries {
        return db.locationQueries
    }
}
