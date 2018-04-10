package com.ekeitho.simplepomodoro.pomodoro

interface PomodoroContract {
    interface PomodoroPresenter {
        fun init()

        fun startTimer()
        fun pauseTimer()
        fun finishTimer()
        fun updateTime()

        fun intentionalPause()
        fun checkAppResume()

        fun getCurrentState() : PomodoroState

        fun getUsersTimePreference() : String
        fun saveUsersTimePreference(time : String)
    }

    interface PomodoroView {
        fun updateTimerView(time: String)
        fun showPlayButton()
        fun showPauseButton()
    }
}