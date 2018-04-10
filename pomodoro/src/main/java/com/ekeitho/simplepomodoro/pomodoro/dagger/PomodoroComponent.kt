package com.ekeitho.simplepomodoro.pomodoro.dagger

import com.ekeitho.simplepomodoro.pomodoro.SimplePomodoroActivity
import dagger.Subcomponent

@Subcomponent(modules = arrayOf(PomodoroModule::class))
interface PomodoroComponent {
    fun inject(activity: SimplePomodoroActivity)
}