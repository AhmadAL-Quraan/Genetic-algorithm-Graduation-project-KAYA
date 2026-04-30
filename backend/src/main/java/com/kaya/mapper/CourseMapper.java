package com.kaya.mapper;

import com.kaya.dto.response.CourseResponse;
import com.kaya.dto.response.DepartmentResponse;
import com.kaya.dto.response.TeacherResponse;
import com.kaya.model.Course;
import com.kaya.model.Lecture;
import com.kaya.service.DepartmentService;
import com.kaya.service.TeacherService;

public class CourseMapper {

    public static CourseResponse mapToResponse(Course course, Lecture lecture) {
        CourseResponse r = new CourseResponse();
        r.setId(course.getId());
        r.setCourseSymbol(course.getCourseSymbol());
        r.setCourseNumber(course.getCourseNumber());
        r.setMajors(course.getMajors());
        r.setRoomGroups(course.getRequiredRoomType());
        r.setTimeGroups(course.getTeachingMethod());
        if (course.getDepartment() != null) {
            var d = course.getDepartment();
            r.setDepartment(new DepartmentResponse(d.getId(), d.getName(), d.getCode()));
        }
        if (lecture != null) {
            r.setLectureId(lecture.getId());
            r.setInstructor(lecture.getInstructor());
            r.setSectionNumber(lecture.getSectionNumber());
            if (lecture.getTeacher() != null) {
                var t = lecture.getTeacher();
                DepartmentResponse dr = t.getDepartment() != null
                        ? new DepartmentResponse(t.getDepartment().getId(), t.getDepartment().getName(), t.getDepartment().getCode())
                        : null;
                r.setTeacher(new TeacherResponse(t.getId(), t.getName(), t.getEmail(), dr));
            }
            if (lecture.getRoom() != null) r.setRoom(RoomMapper.mapToResponse(lecture.getRoom()));
            if (lecture.getTimeSlot() != null) r.setTimeSlot(TimeSlotMapper.mapToResponse(lecture.getTimeSlot()));
        }
        return r;
    }

    public static CourseResponse mapToResponse(Course course) {
        return mapToResponse(course, null);
    }
}
