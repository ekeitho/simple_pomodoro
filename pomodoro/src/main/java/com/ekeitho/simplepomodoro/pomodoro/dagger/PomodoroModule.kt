package com.ekeitho.simplepomodoro.pomodoro.dagger

import android.preference.PreferenceManager
import com.ekeitho.simplepomodoro.pomodoro.PomodoroContract
import com.ekeitho.simplepomodoro.pomodoro.SimplePomodoroActivity
import com.ekeitho.simplepomodoro.pomodoro.SimplePomodoroPresenter
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
@Singleton
class PomodoroModule(val activity: SimplePomodoroActivity) {

    @Provides
    fun getSimple() : PomodoroContract.PomodoroPresenter {
        return SimplePomodoroPresenter(activity, PreferenceManager.getDefaultSharedPreferences(activity))
    }

    // idea ?
    // easily request different presenters by naming convention, using the same interface
    //public fun getComplex()
}