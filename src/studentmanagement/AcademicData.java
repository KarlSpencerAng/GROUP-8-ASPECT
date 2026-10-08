/**
 * AcademicData.java
 * Purpose: Shared academic data and in-memory state.
 * Organization note: Executable code and original formatting below are unchanged.
 */
package studentmanagement;

import java.sql.SQLException;
import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import java.util.Map;

public class AcademicData {

    private static final List<Student> students =

            new ArrayList<>();

    private static final List<Course> courses =

            new ArrayList<>();

    private static final List<Grade> grades =

            new ArrayList<>();

    private static final List<GradeConcern> concerns =

            new ArrayList<>();

    // OLA assessment storage

    private static final List<CourseAssessment> olaAssessments =

            new ArrayList<>();

    // Assessment ID -> Student Number -> Score

    private static final Map<Integer, Map<String, Double>>

            assessmentScores = new HashMap<>();

    private static int nextAssessmentId = 1;

    private AcademicData() {

    }

    // STUDENTS

    public static void addStudent(Student student) {

        students.add(student);

    }

    public static List<Student> getStudents() {

        return students;

    }

    // COURSES

    public static void addCourse(Course course) {

        courses.add(course);

    }

    public static List<Course> getCourses() {

        return courses;

    }

    // GRADES

    public static void addGrade(Grade grade) {

        grades.add(grade);

    }

    public static List<Grade> getGrades() {

        return grades;

    }

    public static List<Grade> getGradesByStudent(

            String studentNumber) {

        List<Grade> result = new ArrayList<>();

        for (Grade grade : grades) {

            if (grade.getStudent()

                    .getStudentNumber()

                    .equals(studentNumber)) {

                result.add(grade);

            }

        }

        return result;

    }

    // GRADE CONCERNS

    public static void addConcern(GradeConcern concern) {

        concerns.add(concern);

    }

    public static List<GradeConcern> getConcerns() {

        return concerns;

    }



private static void checkAssessmentModificationAllowed(

        String courseCode) {

    for (Grade grade : grades) {

        if (grade.getCourse().getCourseCode()

                .equals(courseCode)) {

            String status = grade.getStatus();

            if (status.equals("Submitted")

                    || status.equals("Revised")) {

                throw new IllegalStateException(

                        "This course contains submitted or "

                        + "revised grades. Assessment "

                        + "definitions cannot be modified."

                );

            }

        }

    }

}


/**
 * Imports existing MySQL OLA assessments
 * and their student scores.
 *
 * Intended for application initialization,
 * before any dashboard is opened.
 */
public static void loadDatabaseOLA(
        List<CourseAssessment> assessments,
        Map<Integer, Map<String, Double>> scores) {

    if (assessments == null || scores == null) {
        throw new IllegalArgumentException(
            "OLA data cannot be null."
        );
    }

    // Prepare replacement data first.
    List<CourseAssessment> importedAssessments =
            new ArrayList<>();

    Map<Integer, Map<String, Double>> importedScores =
            new HashMap<>();

    int highestId = 0;

    for (CourseAssessment record : assessments) {

        if (record == null) {
            throw new IllegalArgumentException(
                "Invalid OLA assessment."
            );
        }

        int id = record.getAssessmentId();

        if (importedScores.containsKey(id)) {
            throw new IllegalArgumentException(
                "Duplicate assessment ID: " + id
            );
        }

        boolean courseExists = false;

        for (Course course : courses) {
            if (course.getCourseCode().equals(
                    record.getCourseCode())) {

                courseExists = true;
                break;
            }
        }

        if (!courseExists) {
            throw new IllegalArgumentException(
                "Unknown course: "
                + record.getCourseCode()
            );
        }

        Map<String, Double> recordScores =
                scores.get(id);

        Map<String, Double> validatedScores =
                new HashMap<>();

        if (recordScores != null) {

            for (Map.Entry<String, Double> entry :
                    recordScores.entrySet()) {

                String studentNumber = entry.getKey();
                Double score = entry.getValue();

                boolean studentExists = false;

                for (Student student : students) {
                    if (student.getStudentNumber()
                            .equals(studentNumber)) {

                        studentExists = true;
                        break;
                    }
                }

                if (!studentExists || score == null) {
                    throw new IllegalArgumentException(
                        "Invalid student score for "
                        + "assessment " + id
                    );
                }

                // Reuse existing score validation.
                record.getAssessment()
                        .calculatePercentage(score);

                validatedScores.put(
                    studentNumber, score
                );
            }
        }

        importedAssessments.add(record);
        importedScores.put(id, validatedScores);

        highestId = Math.max(highestId, id);
    }

    // Replace memory only after all records
    // have passed validation.
    olaAssessments.clear();
    olaAssessments.addAll(importedAssessments);

    assessmentScores.clear();
    assessmentScores.putAll(importedScores);

    nextAssessmentId = highestId + 1;

    System.out.println(
        "Loaded " + olaAssessments.size()
        + " OLA assessments from MySQL."
    );
}

    // OLA ASSESSMENT MANAGEMENT

    public static CourseAssessment addOLAAssessment(

            String courseCode,

            String name,

            double maximumScore,

            double weight) {

        boolean courseExists = false;

        for (Course course : courses) {

            if (course.getCourseCode()

                    .equals(courseCode)) {

                courseExists = true;

                break;

            }

        }

        if (!courseExists) {

            throw new IllegalArgumentException(

                    "Course does not exist."

            );

        }

        checkAssessmentModificationAllowed(courseCode);

        Assessment assessment =

                new Assessment(

                        name,

                        maximumScore,

                        weight

                );

        CourseAssessment record =

                new CourseAssessment(

                        nextAssessmentId++,

                        courseCode,

                        assessment

                );

        olaAssessments.add(record);

        assessmentScores.put(

                record.getAssessmentId(),

                new HashMap<>()

        );

        return record;

    }

    public static List<CourseAssessment>

            getOLAAssessments(String courseCode) {

        List<CourseAssessment> result =

                new ArrayList<>();

        for (CourseAssessment record : olaAssessments) {

            if (record.getCourseCode()

                    .equals(courseCode)) {

                result.add(record);

            }

        }

        return result;

    }

    public static CourseAssessment findOLAAssessment(

            int assessmentId) {

        for (CourseAssessment record : olaAssessments) {

            if (record.getAssessmentId()

                    == assessmentId) {

                return record;

            }

        }

        return null;

    }

    public static void updateOLAAssessment(

            int assessmentId,

            String name,

            double maximumScore,

            double weight) {

        CourseAssessment record =

                findOLAAssessment(assessmentId);

        if (record == null) {

            throw new IllegalArgumentException(

                    "Assessment not found."

            );

        }

        checkAssessmentModificationAllowed(

                record.getCourseCode()

        );

        // Validate all changes before modifying

        // the existing assessment.

        Assessment validated =

                new Assessment(

                        name,

                        maximumScore,

                        weight

                );

        Map<String, Double> scores =

                assessmentScores.get(assessmentId);

        for (double score : scores.values()) {

            if (score > maximumScore) {

                throw new IllegalArgumentException(

                        "New maximum is below an "

                        + "existing student score."

                );

            }

        }

        Assessment assessment =

                record.getAssessment();

        assessment.setName(validated.getName());

        assessment.setMaximumScore(

                validated.getMaximumScore()

        );

        assessment.setWeight(

                validated.getWeight()

        );

    }



public static void deleteOLAAssessment(

        int assessmentId) {

    CourseAssessment record =

            findOLAAssessment(assessmentId);

    if (record == null) {

        throw new IllegalArgumentException(

                "Assessment not found."

        );

    }

    checkAssessmentModificationAllowed(

            record.getCourseCode()

    );

    olaAssessments.remove(record);

    assessmentScores.remove(assessmentId);

}

    // INDIVIDUAL STUDENT SCORES

    public static void setAssessmentScore(

            int assessmentId,

            String studentNumber,

            double score) {

        CourseAssessment record =

                findOLAAssessment(assessmentId);

        if (record == null) {

            throw new IllegalArgumentException(

                    "Assessment not found."

            );

        }

        if (studentNumber == null) {

            throw new IllegalArgumentException(

                    "Student number is required."

            );

        }

        Grade matchingGrade = null;

        for (Grade grade : grades) {

            if (grade.getStudent()

                    .getStudentNumber()

                    .equals(studentNumber)

                    && grade.getCourse()

                    .getCourseCode()

                    .equals(record.getCourseCode())) {

                matchingGrade = grade;

                break;

            }

        }

        if (matchingGrade == null) {

            throw new IllegalArgumentException(

                    "Student is not assigned to "

                    + "this course."

            );

        }

        if (matchingGrade.getStatus()

                .equals("Submitted")

                || matchingGrade.getStatus()

                .equals("Revised")) {

            throw new IllegalStateException(

                    "Submitted or revised grades "

                    + "cannot be edited here."

            );

        }

        // Reuse Assessment's score validation

        record.getAssessment()

                .calculatePercentage(score);

        assessmentScores.get(assessmentId)

                .put(studentNumber, score);

    }

    public static Double getAssessmentScore(

            int assessmentId,

            String studentNumber) {

        Map<String, Double> scores =

                assessmentScores.get(assessmentId);

        if (scores == null) {

            return null;

        }

        return scores.get(studentNumber);

    }

    // OLA CALCULATION

    public static double calculateStudentOLA(

            String studentNumber,

            String courseCode) {

        List<CourseAssessment> records =

                getOLAAssessments(courseCode);

        if (records.isEmpty()) {

            throw new IllegalStateException(

                    "No OLA assessments found."

            );

        }

        double totalWeight = 0;

        for (CourseAssessment record : records) {

            totalWeight += record.getAssessment()

                    .getWeight();

        }

        if (Math.abs(totalWeight - 100.0)

                > 0.000001) {

            throw new IllegalStateException(

                    "OLA weights must total 100%."

            );

        }

        double olaGrade = 0;

        for (CourseAssessment record : records) {

            Double score = getAssessmentScore(

                    record.getAssessmentId(),

                    studentNumber

            );

            if (score == null) {

                throw new IllegalStateException(

                        "Missing score for "

                        + record.getAssessment()

                                .getName()

                );

            }

            olaGrade += record.getAssessment()

                    .calculateWeightedScore(score);

        }

        return olaGrade;

    }


    // Authorized OLA correction: only a specific reviewed concern can authorize
    // changes to one student's scores. The normal score editor remains locked.
    public static double previewOLARevision(int concernId,
            Map<Integer, Double> proposedScores) {
        OLARevisionTarget target = validateOLARevision(concernId, proposedScores);
        double total = 0;
        for (CourseAssessment record : getOLAAssessments(target.courseCode)) {
            int id = record.getAssessmentId();
            Double proposed = proposedScores.get(id);
            Double score = proposed != null ? proposed
                    : getAssessmentScore(id, target.studentNumber);
            if (score == null) {
                throw new IllegalStateException("Missing score for "
                        + record.getAssessment().getName());
            }
            total += record.getAssessment().calculateWeightedScore(score);
        }
        return total;
    }

    public static void submitOLARevision(int concernId,
            Map<Integer, Double> proposedScores) {
        OLARevisionTarget target = validateOLARevision(concernId, proposedScores);
        double revisedOLA = previewOLARevision(concernId, proposedScores);
        // All validation happens before any stored score is changed.
        for (Map.Entry<Integer, Double> entry : proposedScores.entrySet()) {
            assessmentScores.get(entry.getKey())
                    .put(target.studentNumber, entry.getValue());
        }
        target.grade.setOLA(revisedOLA);
        target.grade.calculateFinalGrade();
        target.grade.setStatus("Revised");
        target.concern.setStatus("Revised");
    }

    private static final class OLARevisionTarget {
        final Grade grade;
        final GradeConcern concern;
        final String studentNumber;
        final String courseCode;
        OLARevisionTarget(Grade grade, GradeConcern concern) {
            this.grade = grade;
            this.concern = concern;
            this.studentNumber = concern.getStudent().getStudentNumber();
            this.courseCode = concern.getCourse().getCourseCode();
        }
    }

    private static OLARevisionTarget validateOLARevision(int concernId,
            Map<Integer, Double> proposedScores) {
        if (proposedScores == null || proposedScores.isEmpty()) {
            throw new IllegalArgumentException("Select at least one OLA score to revise.");
        }
        GradeConcern concern = null;
        for (GradeConcern candidate : concerns) {
            if (candidate.getConcernId() == concernId) {
                concern = candidate;
                break;
            }
        }
        if (concern == null || !"Under Review".equals(concern.getStatus())) {
            throw new IllegalStateException("An active, reviewed concern is required.");
        }
        Grade matchingGrade = null;
        for (Grade grade : grades) {
            if (grade.getStudent().getStudentNumber()
                    .equals(concern.getStudent().getStudentNumber())
                    && grade.getCourse().getCourseCode()
                    .equals(concern.getCourse().getCourseCode())) {
                matchingGrade = grade;
                break;
            }
        }
        if (matchingGrade == null || !matchingGrade.isCalculationComplete()
                || !("Submitted".equals(matchingGrade.getStatus())
                    || "Revised".equals(matchingGrade.getStatus()))) {
            throw new IllegalStateException("A submitted grade is required for revision.");
        }
        List<CourseAssessment> records = getOLAAssessments(
                concern.getCourse().getCourseCode());
        if (records.isEmpty()) {
            throw new IllegalStateException("No OLA assessments found.");
        }
        double totalWeight = 0;
        for (CourseAssessment record : records) {
            totalWeight += record.getAssessment().getWeight();
        }
        if (Math.abs(totalWeight - 100.0) > 0.000001) {
            throw new IllegalStateException("OLA weights must total 100%.");
        }
        for (Map.Entry<Integer, Double> entry : proposedScores.entrySet()) {
            CourseAssessment record = findOLAAssessment(entry.getKey());
            if (record == null || !record.getCourseCode()
                    .equals(concern.getCourse().getCourseCode())) {
                throw new IllegalArgumentException("Invalid assessment for this concern.");
            }
            if (entry.getValue() == null) {
                throw new IllegalArgumentException("A revised score cannot be blank.");
            }
            record.getAssessment().calculatePercentage(entry.getValue());
        }
        return new OLARevisionTarget(matchingGrade, concern);
    }

    // Historical records are entered by an authorized staff workflow, never by students.
    // In-memory until JDBC is integrated; no historical records are inferred from screenshots.
    public static final class HistoricalRecord {
        private final String studentNumber, courseCode, status, note;
        public HistoricalRecord(String studentNumber, String courseCode, String status, String note) {
            this.studentNumber=studentNumber;
            this.courseCode=courseCode;
            this.status=status;
            this.note=note;
        }
        public String getStudentNumber(){return studentNumber;}
        public String getCourseCode(){return courseCode;}
        public String getStatus(){return status;}
        public String getNote(){return note;}
    }
    private static final Map<String, Map<String, HistoricalRecord>> historicalRecords = new HashMap<>();

    /** Refresh the shared curriculum cache only after a successful database read. */
    public static void refreshHistoricalRecords() throws SQLException {
        List<HistoricalRecord> loaded = HistoricalRecordsRepository.loadAll();
        Map<String, Map<String, HistoricalRecord>> fresh = new HashMap<>();
        for (HistoricalRecord record : loaded) {
            fresh.computeIfAbsent(record.getStudentNumber(), k -> new HashMap<>())
                 .put(record.getCourseCode(), record);
        }
        historicalRecords.clear();
        historicalRecords.putAll(fresh);
    }

    public static HistoricalRecord getHistoricalRecord(String studentNumber, String courseCode) {
        Map<String, HistoricalRecord> byCourse=historicalRecords.get(studentNumber);
        return byCourse==null?null:byCourse.get(courseCode);
    }
    public static List<HistoricalRecord> getHistoricalRecords(String studentNumber) {
        Map<String, HistoricalRecord> byCourse=historicalRecords.get(studentNumber);
        return byCourse==null?new ArrayList<>():new ArrayList<>(byCourse.values());
    }
    public static void saveHistoricalRecord(String studentNumber, String courseCode, String status, String note) {
        boolean exists=false;
        for(Student s:students) if(s.getStudentNumber().equals(studentNumber)){exists=true;break;}
        if(!exists) throw new IllegalArgumentException("Unknown student.");
        CurriculumPanel.curriculumUnits(courseCode); // validates curriculum code
        if(!("Taken".equals(status)||"Incomplete".equals(status)||"Exempted/Credited".equals(status)))
            throw new IllegalArgumentException("Invalid historical status.");
        if(note==null||note.trim().isEmpty()) throw new IllegalArgumentException("Enter a record reference or reason.");
        for(Grade g:grades) if(g.getStudent().getStudentNumber().equals(studentNumber)
                && g.getCourse().getCourseCode().equals(courseCode))
            throw new IllegalStateException("A current grade exists for this course. Resolve it before entering a historical record.");
        historicalRecords.computeIfAbsent(studentNumber,k->new HashMap<>()).put(courseCode,
                new HistoricalRecord(studentNumber,courseCode,status,note.trim()));
    }
    public static void removeHistoricalRecord(String studentNumber,String courseCode) {
        Map<String, HistoricalRecord> records=historicalRecords.get(studentNumber);
        if(records==null||records.remove(courseCode)==null)
            throw new IllegalArgumentException("Historical record not found.");
    }

    // Demo records are explicitly labeled and can be removed without touching
    // staff-entered records, current grades, assessments, or grade concerns.
    private static final String DEMO_PREFIX = "[DEMO SAMPLE] ";

    public static int loadHistoricalDemo(String studentNumber) {
        // These are fictional examples, NOT the student's verified transcript.
        String[][] samples = {
            {"CSS121P", "Taken", "Sample previously completed course"},
            {"GED101", "Taken", "Sample previously completed course"},
            {"GED103", "Exempted/Credited", "Sample transfer credit"},
            {"FW01-2", "Incomplete", "Sample official incomplete record"}
        };
        int added = 0;
        for (String[] sample : samples) {
            if (getHistoricalRecord(studentNumber, sample[0]) != null) continue;
            try {
                saveHistoricalRecord(studentNumber, sample[0], sample[1],
                        DEMO_PREFIX + sample[2]);
                added++;
            } catch (IllegalStateException ex) {
                // An existing current course grade takes precedence. Never replace it.
            }
        }
        return added;
    }

    public static int resetHistoricalDemo(String studentNumber) {
        Map<String, HistoricalRecord> records = historicalRecords.get(studentNumber);
        if (records == null) return 0;
        int before = records.size();
        records.entrySet().removeIf(entry -> entry.getValue().getNote()
                .startsWith(DEMO_PREFIX));
        return before - records.size();
    }


/**
 * Synchronizes grades already stored in MySQL.
 *
 * Database records take precedence over temporary
 * Java demonstration grades.
 *
 * This method does not write to MySQL.
 */
public static void synchronizeDatabaseGrades()
        throws SQLException {

    // Retrieve records first. If the database is
    // unavailable, leave existing data unchanged.
    List<GradeRepository.GradeRecord> records =
        GradeRepository.loadAllGrades();

    // Prepare changes before applying them.
    Map<Grade, Grade> replacements = new HashMap<>();

    for (GradeRepository.GradeRecord record : records) {

        Student student = null;
        Course course = null;

        for (Student candidate : students) {
            if (candidate.getStudentNumber()
                    .equals(record.studentNumber)) {
                student = candidate;
                break;
            }
        }

        for (Course candidate : courses) {
            if (candidate.getCourseCode()
                    .equals(record.courseCode)) {
                course = candidate;
                break;
            }
        }

        if (student == null || course == null) {
            throw new SQLException(
                "Database grade references an unknown "
                + "student or course: "
                + record.studentNumber + " / "
                + record.courseCode
            );
        }

        Grade databaseGrade =
            new Grade(student, course);

        databaseGrade.setExam1(record.co1);
        databaseGrade.setExam2(record.co2);
        databaseGrade.setExam3(record.co3);
        databaseGrade.setFinalExam(record.finalExam);
        databaseGrade.setOLA(record.ola);
        databaseGrade.setCoursera(record.coursera);

        if (record.calculationComplete) {

            databaseGrade.calculateFinalGrade();

            if (Math.abs(
                    databaseGrade.getFinalGrade()
                    - record.finalPercentage
                ) > 0.011) {

                throw new SQLException(
                    "Grade calculation mismatch: "
                    + record.studentNumber + " / "
                    + record.courseCode
                );
            }

            if (Math.abs(
                    databaseGrade.getNumericalGrade()
                    - record.numericalGrade
                ) > 0.001) {

                throw new SQLException(
                    "Numerical grade mismatch: "
                    + record.studentNumber + " / "
                    + record.courseCode
                );
            }
        }

        databaseGrade.setStatus(
            record.submissionStatus
        );

        Grade existingGrade = null;

        for (Grade candidate : grades) {

            if (candidate.getStudent()
                    .getStudentNumber()
                    .equals(record.studentNumber)
                    && candidate.getCourse()
                    .getCourseCode()
                    .equals(record.courseCode)) {

                existingGrade = candidate;
                break;
            }
        }

        replacements.put(
            existingGrade, databaseGrade
        );
    }

    // All records have passed validation.
    // Apply the prepared changes.
    for (Map.Entry<Grade, Grade> entry :
            replacements.entrySet()) {

        Grade existing = entry.getKey();
        Grade replacement = entry.getValue();

        if (existing != null) {
            int index = grades.indexOf(existing);
            grades.set(index, replacement);
        } else {
            grades.add(replacement);
        }
    }

    System.out.println(
        "Synchronized " + records.size()
        + " grades from MySQL."
    );
}

}
