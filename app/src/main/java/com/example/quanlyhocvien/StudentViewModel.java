package com.example.quanlyhocvien;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quanlyhocvien.object.Student;

import java.util.ArrayList;
import android.util.Log;

public class StudentViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<Student>> studentsLiveData = new MutableLiveData<>(new ArrayList<>());

    // Phương thức để lấy dữ liệu
    public MutableLiveData<ArrayList<Student>> getStudentsLiveData() {
        return studentsLiveData;
    }


    // Phương thức để cập nhật dữ liệu
    public void setStudentsLiveData(ArrayList<Student> students) {
        studentsLiveData.setValue(students);
    }

    // Phương thức để thêm một sinh viên mới
    public void addStudent(Student student) {
        ArrayList<Student> students = studentsLiveData.getValue();
        assert students != null;
        students.add(student);
        studentsLiveData.setValue(students);
    }

    // Phương thức để xóa một sinh viên
    public void removeStudent(Student student) {
        ArrayList<Student> students = studentsLiveData.getValue();
        if (students != null) {
            boolean removed = false;
            for (int i = 0; i < students.size(); i++) {
                if (students.get(i).getId().equals(student.getId())) {
                    students.remove(i);
                    removed = true;
                    break;
                }
            }
            if (removed) {
                studentsLiveData.setValue(students);
                Log.d("StudentViewModel", "removeStudent: " + student.getId());
            } else {
                Log.e("StudentViewModel", "removeStudent: Student not found");
            }
        }
    }

    // Phương thức để cập nhật thông tin của một sinh viên
    public void updateStudent(Student student) {
        ArrayList<Student> students = studentsLiveData.getValue();
        assert students != null;
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getId().equals(student.getId())) {
                students.set(i, student);
                break;
            }
        }
        studentsLiveData.setValue(students);

        // test
        Log.d("StudentViewModel", "updateStudent: " + student.getId());
    }
}
