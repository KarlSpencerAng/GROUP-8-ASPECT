package studentmanagement;

import java.sql.SQLException;
import java.util.List;

/** Loads live application state from MySQL. No demo students, subjects, or grades are created here. */
public final class DataInitializer {
    private static boolean initialized = false;
    private DataInitializer() {}

    public static synchronized void initialize() {
        if (initialized) return;
        try {
            List<Student> students = AcademicRepository.loadStudents();
            List<Course> courses = AcademicRepository.loadCurrentCourses();
            for (Student student : students) AcademicData.addStudent(student);
            for (Course course : courses) AcademicData.addCourse(course);
            AcademicData.synchronizeDatabaseGrades();
            OLARepository.OLAData olaData = OLARepository.loadAllOLA();
            AcademicData.loadDatabaseOLA(olaData.assessments, olaData.scores);
            AcademicData.refreshHistoricalRecords();
            initialized = true;
            System.out.println("Academic data loaded from MySQL successfully.");
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load academic data from MySQL.", exception);
        }
    }
}
