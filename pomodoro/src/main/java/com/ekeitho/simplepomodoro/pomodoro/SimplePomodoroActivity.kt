package com.ekeitho.simplepomodoro.pomodoro

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ekeitho.simplepomodoro.pomodoro.PomodoroState.WAITING_FOR_INPUT
import com.ekeitho.simplepomodoro.pomodoro.SimplePomodoroPresenter.Companion.TIME_PREFERENCES
import com.ekeitho.simplepomodoro.pomodoro.dagger.PomodoroModule
import kotlinx.android.synthetic.main.activity_main.floatingActionButton
import kotlinx.android.synthetic.main.activity_main.textView
import kotlinx.android.synthetic.main.activity_main.time_icon
import javax.inject.Inject

class SimplePomodoroActivity : AppCompatActivity(), PomodoroContract.PomodoroView {

  // awesome way to use extension property
  val Activity.app: PomodoroApplication
    get() = application as PomodoroApplication

  @Inject lateinit var presenter: PomodoroContract.PomodoroPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    app.component.plus(PomodoroModule(this))
        .inject(this)
    presenter.init()

    floatingActionButton.setOnClickListener {
      // when the button is clicked
      when (presenter.getCurrentState()) {
      // and the current state is updating time.... then user is intentionally pausing
        PomodoroState.UPDATING_TIME -> presenter.intentionalPause()
        PomodoroState.WAITING_FOR_INPUT -> presenter.startTimer()
        PomodoroState.PAUSED_INTENTIONALLY -> presenter.startTimer()
      }
    }

    val alertDialog = AlertDialog.Builder(this)
        .setTitle("Choose Time")
        .setSingleChoiceItems(TIME_PREFERENCES,
            TIME_PREFERENCES.indexOf(presenter.getUsersTimePreference()),
            { dialog, selectionIndex ->
              // if dialog is being interacted with, but isnt using pomodoro atm, then change view
              if (presenter.getCurrentState() == WAITING_FOR_INPUT) {
                updateTimerView(TIME_PREFERENCES[selectionIndex])
              }
              presenter.saveUsersTimePreference(TIME_PREFERENCES[selectionIndex])
            })
        .create()

    time_icon.setOnClickListener {
      alertDialog.show()
    }
  }

  override fun onStart() {
    super.onStart()
    presenter.checkAppResume()
  }

  override fun onPause() {
    super.onPause()
    presenter.pauseTimer()
  }

  override fun updateTimerView(time: String) {
    textView.text = time
  }

  override fun showPlayButton() {
    floatingActionButton.setImageDrawable(getDrawable(R.drawable.play))
  }

  override fun showPauseButton() {
    floatingActionButton.setImageDrawable(getDrawable(R.drawable.pause))
  }
}
