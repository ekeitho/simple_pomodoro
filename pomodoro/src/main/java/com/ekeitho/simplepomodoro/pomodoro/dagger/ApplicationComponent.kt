package com.ekeitho.simplepomodoro.pomodoro.dagger

import dagger.Component

@Component
interface ApplicationComponent {
    fun plus(pomodoroModule: PomodoroModule) : PomodoroComponent
}