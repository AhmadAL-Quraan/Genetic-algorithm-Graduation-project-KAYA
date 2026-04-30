package com.kaya.model;

import com.kaya.model.enums.TeachingMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime startTime;
    private LocalTime endTime;

    // [تعديل جوهري]: استخدام الـ Enum الجاهز بتاع الجافا لأيام الأسبوع!
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> days;

    // [تعديل]: الوقت ده مخصص لطريقة تدريس معينة (مثلاً وقت للمدمج أو وقت للوجاهي)
    @Enumerated(EnumType.STRING)
    //public ArrayList<TeachingMethod> teachingMethod; //groups;
    private TeachingMethod teachingMethod;

    public TimeSlot(LocalTime startTime, LocalTime endTime, Set<DayOfWeek> days, TeachingMethod teachingMethod) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.days = days;
        this.teachingMethod = teachingMethod;
    }

    // ضفتلك الـ equals والـ hashCode عشان الخوارزمية تعرف تقارن الأوقات ببعضها بدقة
    // Calc hash for each object to choose the bucket, then uses equals() to compare objects inside that bucket.
    //true there is contradiction, false there is not
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return Objects.equals(startTime, timeSlot.startTime) &&
                Objects.equals(endTime, timeSlot.endTime) &&
                Objects.equals(days, timeSlot.days) &&
                teachingMethod == timeSlot.teachingMethod;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime, days, teachingMethod);
    }

    @Override
    public String toString() {
        return "Days: " + days + " Start: " + startTime + " End: " + endTime + " Method: " + teachingMethod;
    }
}