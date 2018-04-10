package com.ekeitho.simplepomodoro.pomodoro

import android.app.Application
import com.ekeitho.simplepomodoro.pomodoro.dagger.ApplicationComponent
import com.ekeitho.simplepomodoro.pomodoro.dagger.DaggerApplicationComponent

class PomodoroApplication : Application() {

    val component : ApplicationComponent by lazy {
        DaggerApplicationComponent.create()
    }

}