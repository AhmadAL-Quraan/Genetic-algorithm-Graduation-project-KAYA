package model;

public class Course {

    private String department;
    private String courseNumber;
    private String section;
    private String courseName;
    private String teacherName;

    // جديد
    private int timeSlotId = -1;
    private int patternId = -1;
    private int teacherId = -1;


    private String roomCode;

    private int roomId = -1;
    private boolean isOnline = false;
    private boolean requiresLab = false;


    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public String getDepartment() {
        return department;
    }

    public String getCourseNumber() {
        return courseNumber;
    }

    public String getSection() {
        return section;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public int getTimeSlotId() {
        return timeSlotId;
    }

    public int getPatternId() {
        return patternId;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setTimeSlotId(int timeSlotId) {
        this.timeSlotId = timeSlotId;
    }

    public void setPatternId(int patternId) {
        this.patternId = patternId;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setCourseNumber(String courseNumber) {
        this.courseNumber = courseNumber;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public void setRequiresLab(boolean requiresLab) {
        this.requiresLab = requiresLab;
    }

    public boolean getIsOnline() {
        return isOnline();
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public boolean getIsRequiresLab() {
        return isRequiresLab();
    }

    public Course(
            String department,
            String courseNumber,
            String section,
            String courseName,
            String teacherName,
            String roomCode) {

        this.setDepartment(department);
        this.setCourseNumber(courseNumber);
        this.setSection(section);
        this.setCourseName(courseName);
        this.setTeacherName(teacherName);
        this.setRoomCode(roomCode);

        // كشف Online
        if (roomCode == null
                || roomCode.trim().isEmpty()
                || roomCode.toLowerCase()
                .contains("oline")) {

            setOnline(true);
        }

        if (courseNumber
                .toUpperCase()
                .contains("L")) {
            setRequiresLab(true);
        }
    }

    @Override
    public String toString() {

        return getDepartment() + " "
                + getCourseNumber()
                + " - Sec "
                + getSection()
                + " - T "
                + getTeacherId()
                + " - R "
                + getRoomId()
                + " - Pattern "
                + getPatternId()
                + " - Slot "
                + getTimeSlotId()
                + " - Online "
                + isOnline();
    }

    public boolean isOnline() {
        return isOnline;
    }

    public boolean isRequiresLab() {
        return requiresLab;
    }
}