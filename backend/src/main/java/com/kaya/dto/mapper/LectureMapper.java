package com.kaya.dto.mapper;

import com.kaya.dto.response.LectureResponse;
import com.kaya.model.Lecture;

public class LectureMapper {

    public static Lecture mapToEntity(LectureResponse response) {
        Lecture l = new Lecture();
        l.setId(response.getId());
        if (response.getCourse() != null) {
            l.setCourse(CourseMapper.mapToEntity(response.getCourse()));
        }
        if (response.getTimeSlot() != null) {
            com.kaya.model.TimeSlot ts = new com.kaya.model.TimeSlot();
            ts.setId(response.getTimeSlot().getId());
            l.setTimeSlot(ts);
        }
        if (response.getRoom() != null) {
            com.kaya.model.Room r = new com.kaya.model.Room();
            r.setId(response.getRoom().getId());
            l.setRoom(r);
        }
        l.setInstructor(response.getInstructor());
        return l;
    }

    public static LectureResponse mapToResponse(Lecture lecture) {
        LectureResponse.TeacherInfo teacherInfo = null;
        if (lecture.getTeacher() != null) {
            teacherInfo = new LectureResponse.TeacherInfo(
                    lecture.getTeacher().getId(),
                    lecture.getTeacher().getName()
            );
        }
        return new LectureResponse(
                lecture.getId(),
                lecture.getCourse() != null ? CourseMapper.mapToResponse(lecture.getCourse()) : null,
                teacherInfo,
                lecture.getInstructor(),
                lecture.getTimeSlot() != null ? TimeSlotMapper.mapToResponse(lecture.getTimeSlot()) : null,
                lecture.getRoom() != null ? RoomMapper.mapToResponse(lecture.getRoom()) : null
        );
    }
}
