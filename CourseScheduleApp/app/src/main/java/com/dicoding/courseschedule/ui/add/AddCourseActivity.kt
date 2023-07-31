package com.dicoding.courseschedule.ui.add

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.dicoding.courseschedule.R
import com.dicoding.courseschedule.util.TimePickerFragment
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class AddCourseActivity : AppCompatActivity(), TimePickerFragment.DialogTimeListener {

    private lateinit var addCourseViewModel: AddCourseViewModel
    private lateinit var inputCourseName: TextInputEditText
    private lateinit var inputLecturerName: TextInputEditText
    private lateinit var inputNote: TextInputEditText
    private lateinit var inputDay: Spinner
    private var timeStart: String = ""
    private var timeEnd: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_course)

        supportActionBar?.title = "Add Course"
        addCourseViewModel = ViewModelProvider(
            this,
            AddViewModelFactory.createFactory(this)
        )[AddCourseViewModel::class.java]

        addCourseViewModel.saved.observe(
            this,{
                when(it.getContentIfNotHandled()){
                    true -> {
                        onBackPressed()
                    }
                    false -> {
                        Toast.makeText(applicationContext, "all off contect must be filled", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.action_insert -> {
                //do insert function
                inputCourseName = findViewById(R.id.add_course_name)
                inputLecturerName = findViewById(R.id.add_lecturer_name)
                inputNote = findViewById(R.id.add_note)
                inputDay = findViewById<Spinner>(R.id.add_day)
                val courseName = inputCourseName.text.toString().trim()
                val lecturerName = inputLecturerName.text.toString().trim()
                val note = inputNote.text.toString().trim()
                val day = inputDay.selectedItemPosition

                addCourseViewModel.insertCourse(courseName, day, timeStart, timeEnd, lecturerName, note)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDialogTimeSet(tag: String?, hour: Int, minute: Int) {
        val calender = Calendar.getInstance()
        calender.set(Calendar.HOUR_OF_DAY, hour)
        calender.set(Calendar.MINUTE, minute)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        if (tag == "startPicker" ){
            findViewById<TextView>(R.id.add_start_time_tv).text = timeFormat.format(calender.time)
            timeStart = timeFormat.format(calender.time)
        }else if (tag == "endPicker"){
            findViewById<TextView>(R.id.add_end_time_tv).text = timeFormat.format(calender.time)
            timeEnd = timeFormat.format(calender.time)
        }else{
            Log.d("Add Activity", "DialogTimeError")
        }
    }


    fun showTimeStartPicker(view: View) {
        val timePickerFragment = TimePickerFragment()
        timePickerFragment.show(supportFragmentManager, "startPicker")
    }
    fun showTimeEndPicker(view: View) {
        val timePickerFragment = TimePickerFragment()
        timePickerFragment.show(supportFragmentManager, "endPicker")
    }


}