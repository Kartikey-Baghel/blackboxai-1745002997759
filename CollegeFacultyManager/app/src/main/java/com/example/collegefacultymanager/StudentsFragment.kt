package com.example.collegefacultymanager

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

class StudentsFragment : Fragment() {

    private val courses = arrayOf("BCA", "BBA", "B.COM", "MCA", "MBA", "BA", "B.Ed", "MA")
    private val semesters = arrayOf("1", "2", "3", "4", "5", "6")
    private val universities = arrayOf("DBRAU", "AKTU")

    private lateinit var nameEditText: EditText
    private lateinit var rollNumberEditText: EditText
    private lateinit var courseSpinner: Spinner
    private lateinit var semesterSpinner: Spinner
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var universitySpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var importButton: Button

    private val studentsArray = JSONArray()

    private val importJsonLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                importStudentData(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_students, container, false)

        nameEditText = view.findViewById(R.id.nameEditText)
        rollNumberEditText = view.findViewById(R.id.rollNumberEditText)
        courseSpinner = view.findViewById(R.id.courseSpinner)
        semesterSpinner = view.findViewById(R.id.semesterSpinner)
        genderRadioGroup = view.findViewById(R.id.genderRadioGroup)
        universitySpinner = view.findViewById(R.id.universitySpinner)
        saveButton = view.findViewById(R.id.saveButton)
        importButton = view.findViewById(R.id.importButton)

        courseSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, courses)
        semesterSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, semesters)
        universitySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, universities)

        saveButton.setOnClickListener {
            saveStudentData()
        }

        importButton.setOnClickListener {
            openFilePicker()
        }

        return view
    }

    private fun saveStudentData() {
        val name = nameEditText.text.toString().trim()
        val rollNumber = rollNumberEditText.text.toString().trim()
        val course = courseSpinner.selectedItem.toString()
        val semester = semesterSpinner.selectedItem.toString()
        val university = universitySpinner.selectedItem.toString()

        val selectedGenderId = genderRadioGroup.checkedRadioButtonId
        if (selectedGenderId == -1) {
            Toast.makeText(requireContext(), "Please select gender", Toast.LENGTH_SHORT).show()
            return
        }
        val genderRadioButton = genderRadioGroup.findViewById<RadioButton>(selectedGenderId)
        val gender = genderRadioButton.text.toString()

        if (name.isEmpty() || rollNumber.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter name and roll number", Toast.LENGTH_SHORT).show()
            return
        }

        val studentObject = JSONObject()
        studentObject.put("name", name)
        studentObject.put("rollNumber", rollNumber)
        studentObject.put("course", course)
        studentObject.put("semester", semester)
        studentObject.put("gender", gender)
        studentObject.put("university", university)

        studentsArray.put(studentObject)

        // Save to JSON file
        val fileName = "students.json"
        val file = File(requireContext().filesDir, fileName)
        FileOutputStream(file).use {
            it.write(studentsArray.toString(4).toByteArray())
        }

        Toast.makeText(requireContext(), "Student saved", Toast.LENGTH_SHORT).show()

        // Clear inputs
        nameEditText.text.clear()
        rollNumberEditText.text.clear()
        genderRadioGroup.clearCheck()
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/json"
        importJsonLauncher.launch(intent)
    }

    private fun importStudentData(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val content = reader.readText()
            reader.close()

            val importedArray = JSONArray(content)
            for (i in 0 until importedArray.length()) {
                studentsArray.put(importedArray.getJSONObject(i))
            }

            Toast.makeText(requireContext(), "Student data imported", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to import data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
