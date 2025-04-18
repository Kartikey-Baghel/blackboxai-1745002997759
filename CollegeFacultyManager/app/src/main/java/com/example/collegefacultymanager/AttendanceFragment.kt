package com.example.collegefacultymanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class AttendanceFragment : Fragment() {

    private val courses = arrayOf("BCA", "BBA", "B.COM", "MCA", "MBA", "BA", "B.Ed", "MA")
    private val semesters = arrayOf("1", "2", "3", "4", "5", "6")

    private lateinit var courseSpinner: Spinner
    private lateinit var semesterSpinner: Spinner
    private lateinit var rollNumberContainer: LinearLayout
    private lateinit var exportButton: Button

    private val rollNumberCount = 5
    private val attendanceInputs = mutableListOf<CheckBox>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_attendance, container, false)

        courseSpinner = view.findViewById(R.id.courseSpinner)
        semesterSpinner = view.findViewById(R.id.semesterSpinner)
        rollNumberContainer = view.findViewById(R.id.rollNumberContainer)
        exportButton = view.findViewById(R.id.exportButton)

        courseSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, courses)
        semesterSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, semesters)

        setupRollNumberInputs()

        exportButton.setOnClickListener {
            exportAttendanceData()
        }

        return view
    }

    private fun setupRollNumberInputs() {
        rollNumberContainer.removeAllViews()
        attendanceInputs.clear()

        for (i in 1..rollNumberCount) {
            val row = LinearLayout(requireContext())
            row.orientation = LinearLayout.HORIZONTAL
            row.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            row.setPadding(0, 8, 0, 8)

            val rollNumberText = TextView(requireContext())
            rollNumberText.text = "Roll No $i"
            rollNumberText.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            val presentCheckBox = CheckBox(requireContext())
            presentCheckBox.text = "Present"
            presentCheckBox.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            val absentCheckBox = CheckBox(requireContext())
            absentCheckBox.text = "Absent"
            absentCheckBox.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            // Ensure only one checkbox can be selected at a time
            presentCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    absentCheckBox.isChecked = false
                }
            }
            absentCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    presentCheckBox.isChecked = false
                }
            }

            row.addView(rollNumberText)
            row.addView(presentCheckBox)
            row.addView(absentCheckBox)

            rollNumberContainer.addView(row)
            attendanceInputs.add(presentCheckBox)
            attendanceInputs.add(absentCheckBox)
        }
    }

    private fun exportAttendanceData() {
        val course = courseSpinner.selectedItem.toString()
        val semester = semesterSpinner.selectedItem.toString()

        val attendanceArray = JSONArray()

        for (i in 0 until rollNumberCount) {
            val present = attendanceInputs[i * 2].isChecked
            val absent = attendanceInputs[i * 2 + 1].isChecked

            val attendanceObject = JSONObject()
            attendanceObject.put("rollNumber", i + 1)
            attendanceObject.put("status", when {
                present -> "Present"
                absent -> "Absent"
                else -> "Not Marked"
            })

            attendanceArray.put(attendanceObject)
        }

        val exportObject = JSONObject()
        exportObject.put("course", course)
        exportObject.put("semester", semester)
        exportObject.put("attendance", attendanceArray)

        // Export to JSON file
        val fileName = "attendance_${course}_sem${semester}.json"
        val file = File(requireContext().filesDir, fileName)
        FileOutputStream(file).use {
            it.write(exportObject.toString(4).toByteArray())
        }

        Toast.makeText(requireContext(), "Attendance exported to $fileName", Toast.LENGTH_LONG).show()
    }
}
