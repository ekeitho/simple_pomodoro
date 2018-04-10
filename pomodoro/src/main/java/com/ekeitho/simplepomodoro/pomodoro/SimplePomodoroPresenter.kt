package com.ekeitho.simplepomodoro.pomodoro

import android.content.SharedPreferences
import com.ekeitho.simplepomodoro.pomodoro.PomodoroState.PAUSED_INTENTIONALLY
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SimplePomodoroPresenter
@Inject
constructor(
  val pomodoroView: PomodoroContract.PomodoroView,
  val sharedPreferences: SharedPreferences
) : PomodoroContract.PomodoroPresenter {

  companion object {
    val TIME_PREFERENCES = arrayOf("15:00", "25:00", "35:00")
  }

  // used in app resume to see if there was a timer going on, when app was paused
  // if so then save the pausedTimeStamp
  private val pauseTimestampKey = "PAUSED_TIME_KEY"
  // when there is a pauseTimestamp, then there will be a key for time left
  private val timeLeftKey = "TIME_LEFT_KEY"

  // need a state key, because i cant just depend on the state after user has quit the app
  // even if i used the paused time, i wouldn't know if that was intentional puased or not
  private val pauseIntentKey = "PAUSED_INTENTIONALLY_KEY"

  // gets what the users time preferences for pomodoro will be
  // more of a setting property
  private val timePrefKey = "TIME_PREFERENCE_KEY"

  private var timeLeft = 0L
  private var timerSubscription = Disposables.disposed()

  // based on the states, update the UI in certain ways, however initialize as W_F_I
  private val state = BehaviorSubject.createDefault(PomodoroState.WAITING_FOR_INPUT)

  override fun init() {
    state.subscribe {
      when (it) {
        PomodoroState.WAITING_FOR_INPUT -> {
          pomodoroView.showPlayButton()
          pomodoroView.updateTimerView(
              getFormatedTimeLeft(getUsersTimePreferenceInMillis())
          )
        }
        PomodoroState.PAUSED_INTENTIONALLY -> pomodoroView.showPlayButton()
        PomodoroState.UPDATING_TIME -> pomodoroView.showPauseButton()
      }
    }
  }

  // gets called from when client clicks on the button for the first time
  override fun startTimer() {
    // either a ffresh timer start
    if (sharedPreferences[timeLeftKey, 0L] == 0L) {
      timeLeft = getUsersTimePreferenceInMillis()
    }
    // or user is come back from a pause
    else {
      // fetch time left when user paused
      timeLeft = sharedPreferences[timeLeftKey, 0L]
      // reset time left if user decides to pause again
      sharedPreferences[timeLeftKey] = 0L
    }
    updateTime()
  }

  // gets called after startTimer & after if time didn't expire at checkAppResume
  override fun updateTime() {
    state.onNext(PomodoroState.UPDATING_TIME)
    timerSubscription = Observable
        .interval(1, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .takeUntil {
          if (timeLeft <= 0L) {
            finishTimer()
          }
          timeLeft <= 0L || state.value != PomodoroState.UPDATING_TIME
        }
        .forEach {
          timeLeft -= TimeUnit.SECONDS.toMillis(1)
          pomodoroView.updateTimerView(getFormatedTimeLeft(timeLeft))
        }
  }

  // updates internal cache since we have finished a task
  override fun finishTimer() {
    state.onNext(PomodoroState.WAITING_FOR_INPUT)
    sharedPreferences[timeLeftKey] = 0L
    sharedPreferences[pauseTimestampKey] = 0L
    timeLeft = 0L
  }

  override fun checkAppResume() {
    // if state is not paused, then lets check some things
    if (!sharedPreferences[pauseIntentKey, false]) {
      val pausedTimestamp = sharedPreferences[pauseTimestampKey, 0L]
      if (pausedTimestamp != 0L) {
        timeLeft = getTimeLeft(
            sharedPreferences[timeLeftKey, 0L], pausedTimestamp, System.currentTimeMillis()
        )
        if (timeLeft > 0L) {
          pomodoroView.updateTimerView(getFormatedTimeLeft(timeLeft))
          updateTime()
        } else {
          finishTimer()
        }
      }
    } else {
      // update the puased time
      pomodoroView.updateTimerView(
          getFormatedTimeLeft(sharedPreferences[timeLeftKey, getUsersTimePreferenceInMillis()])
      )
    }
  }

  // actually puts pomodoro in paused state
  override fun intentionalPause() {
    state.onNext(PomodoroState.PAUSED_INTENTIONALLY)
    pauseTimer()
  }

  // this type of pause can occur when user hits home and comes back
  // and does not mean user clicked paused
  override fun pauseTimer() {
    sharedPreferences[timeLeftKey] = timeLeft
    sharedPreferences[pauseTimestampKey] = System.currentTimeMillis()
    sharedPreferences[pauseIntentKey] = state.value == PAUSED_INTENTIONALLY
    timerSubscription.dispose()
    pomodoroView.showPlayButton()
  }

  override fun getUsersTimePreference(): String {
    return sharedPreferences[timePrefKey, TIME_PREFERENCES[0]]
  }

  // time will be 45:00 format
  // if ever extend to hours i can just add TimeUnit.Hours....
  private fun getUsersTimePreferenceInMillis(): Long {
    val split = getUsersTimePreference().split(":")
    return TimeUnit.MINUTES.toMillis(split[0].toLong()) +
        TimeUnit.SECONDS.toMillis(
            split[1].toLong()
        )
  }

  override fun saveUsersTimePreference(time: String) {
    sharedPreferences[timePrefKey] = time
  }

  //never called when app is paused
  //timePaused 3:30
  //timeLeftWhenPaused = 30 seconds
  //timeCamBack = 4:30
  //timePaused + timeLeftWhenPaused - timeCameBack
  private fun getTimeLeft(
    timeLeftWhenPaused: Long,
    timePaused: Long,
    currentTime: Long
  ): Long {
    return timePaused + timeLeftWhenPaused - currentTime
  }

  override fun getCurrentState(): PomodoroState {
    return state.value
  }

  private fun getFormatedTimeLeft(timeLeft: Long): String {
    return SimpleDateFormat("mm:ss", Locale.US).format(Date(timeLeft))
  }
}