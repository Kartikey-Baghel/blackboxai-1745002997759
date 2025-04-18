package com.example.collegefacultymanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class AboutFragment : Fragment() {

    private lateinit var totalCoursesTextView: TextView
    private lateinit var totalStudentsTextView: TextView
    private lateinit var exportAllButton: Button

    private val courses = arrayOf("BCA", "BBA", "B.COM", "MCA", "MBA", "BA", "B.Ed", "MA")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)

        totalCoursesTextView = view.findViewById(R.id.totalCoursesTextView)
        totalStudentsTextView = view.findViewById(R.id.totalStudentsTextView)
        exportAllButton = view.findViewById(R.id.exportAllButton)

        updateStats()

        exportAllButton.setOnClickListener {
            exportAllData()
        }

        return view
    }

    private fun updateStats() {
        val studentsFile = File(requireContext().filesDir, "students.json")
        var totalStudents = 0
        if (studentsFile.exists()) {
            val content = studentsFile.readText()
            val studentsArray = JSONArray(content)
            totalStudents = studentsArray.length()
        }

        totalCoursesTextView.text = "Total Courses: ${courses.size}"
        totalStudentsTextView.text = "Total Students: $totalStudents"
    }

    private fun exportAllData() {
        try {
            val studentsFile = File(requireContext().filesDir, "students.json")
            val attendanceFiles = requireContext().filesDir.listFiles { file ->
                file.name.startsWith("attendance_") && file.name.endsWith(".json")
            } ?: arrayOf()

            val exportObject = JSONObject()

            if (studentsFile.exists()) {
                val studentsContent = studentsFile.readText()
                exportObject.put("students", JSONArray(studentsContent))
            } else {
                exportObject.put("students", JSONArray())
            }

            val attendanceArray = JSONArray()
            for (file in attendanceFiles) {
                val content = file.readText()
                attendanceArray.put(JSONObject(content))
            }
            exportObject.put("attendance", attendanceArray)

            // Export to JSON file
            val fileName = "college_faculty_manager_export.json"
            val file = File(requireContext().filesDir, fileName)
            FileOutputStream(file).use {
                it.write(exportObject.toString(4).toByteArray())
            }

            Toast.makeText(requireContext(), "All data exported to $fileName", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to export data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
