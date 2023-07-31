package com.dicoding.habitapp.ui.countdown

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.dicoding.habitapp.R
import com.dicoding.habitapp.data.Habit
import com.dicoding.habitapp.notification.NotificationWorker
import com.dicoding.habitapp.utils.HABIT
import com.dicoding.habitapp.utils.HABIT_ID
import com.dicoding.habitapp.utils.HABIT_TITLE
import java.util.concurrent.TimeUnit

class CountDownActivity : AppCompatActivity() {

    private var startCount = false
    private lateinit var workManager: WorkManager
    private lateinit var oneTimeWorkRequest: OneTimeWorkRequest


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_count_down)
        supportActionBar?.title = "Count Down"


        workManager = WorkManager.getInstance(this)
        val habit = intent.getParcelableExtra<Habit>(HABIT) as Habit

        findViewById<TextView>(R.id.tv_count_down_title).text = habit.title
        val countDown = findViewById<TextView>(R.id.tv_count_down)

        val viewModel = ViewModelProvider(this).get(CountDownViewModel::class.java)

        //TODO 10 : Set initial time and observe current time. Update button state when countdown is finished
        viewModel.setInitialTime(habit.minutesFocus)
        viewModel.currentTimeString.observe(this, {
            countDown.text = it
        })

        //TODO 13 : Start and cancel One Time Request WorkManager to notify when time is up.
//        viewModel.eventCountDownFinish.observe(this, {
//            updateButtonState(!it)
//            if (it && startCount){
//                startOneTimeTask(habit)
//            }
//        })

        viewModel.eventCountDownFinish.observeForever{isCompleted->
            updateButtonState(!isCompleted)
            if(isCompleted){
                val data = Data.Builder()
                    .putInt(HABIT_ID, habit.id)
                    .putString(HABIT_TITLE, habit.title)
                    .build()

                oneTimeWorkRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                    .setInputData(data)
                    .build()

                workManager.enqueue(oneTimeWorkRequest)
                workManager.getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
                    .observe(this@CountDownActivity, { workInfo ->
                        val status = workInfo.state.name
                        if (workInfo.state == WorkInfo.State.ENQUEUED) {
                            Log.d(TAG, "Notification has been enqueued. Status : $status")
                        } else if (workInfo.state == WorkInfo.State.CANCELLED) {
                            Log.d(TAG, "Notification has been cancelled")
                        }
                    })
            }
            else{
                workManager.cancelAllWorkByTag(getString(R.string.notify_channel_name))
            }
        }

        findViewById<Button>(R.id.btn_start).setOnClickListener {
            viewModel.startTimer()
            startCount = true
        }

        findViewById<Button>(R.id.btn_stop).setOnClickListener {
            viewModel.resetTimer()
            startCount = false
            cancelOneTimeTask()
        }
    }

    private fun startOneTimeTask(habit: Habit){
//        val data = Data.Builder()
//            .putInt(HABIT_ID, habit.id)
//            .putString(HABIT_TITLE, habit.title)
//            .build()
//
//
//        oneTimeWorkRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
//            .setInputData(data)
//            .build()
//
//        workManager.enqueue(oneTimeWorkRequest)
//        workManager.getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
//            .observe(this@CountDownActivity, { workInfo ->
//                val status = workInfo.state.name
//                if (workInfo.state == WorkInfo.State.ENQUEUED) {
//                    Log.d(TAG, "Notification has been enqueued. Status : $status")
//                } else if (workInfo.state == WorkInfo.State.CANCELLED) {
//                    Log.d(TAG, "Notification has been cancelled")
//                }
//            })
    }

    private fun cancelOneTimeTask(){
        Log.d(TAG, "Cancel Work Manager")
        workManager.cancelWorkById(oneTimeWorkRequest.id)
    }

    private fun updateButtonState(isRunning: Boolean) {
        findViewById<Button>(R.id.btn_start).isEnabled = !isRunning
        findViewById<Button>(R.id.btn_stop).isEnabled = isRunning
    }

    companion object{
        private const val TAG = "CountDownActivity"
    }
}