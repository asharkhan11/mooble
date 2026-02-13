package in.ashar.mooble.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionRequest {

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @NotNull
    private Integer tuitionClassId;

    private Integer subjectId;
    private Integer courseId;

    @NotNull
    private Integer teacherId;

    @Builder.Default
    private RecurrenceType recurrenceType = RecurrenceType.NONE;

    private RecurrencePattern recurrencePattern;
    private Set<DayOfWeek> recurrenceDays;

    private LocalDate recurrenceEndDate;
    /* ---------- Validations ---------- */

    @AssertTrue(message = "Either subjectId or courseId must be provided")
    public boolean hasOneType() {
        return (subjectId != null) ^ (courseId != null);
    }

    @AssertTrue(message = "startTime must be before endTime")
    public boolean isValidTimeRange() {
        return startTime == null || endTime == null || startTime.isBefore(endTime);
    }

    @AssertTrue(message = "Custom recurrence requires at least one day")
    public boolean customDaysValid() {
        if (recurrenceType == RecurrenceType.REPEATED &&
                recurrencePattern == RecurrencePattern.CUSTOM) {
            return recurrenceDays != null && !recurrenceDays.isEmpty();
        }
        return true;
    }

    @AssertTrue(message = "Recurrence end date required when repeated")
    public boolean recurrenceEndRequired() {
        if (recurrenceType == RecurrenceType.REPEATED) {
            return recurrenceEndDate != null;
        }
        return true;
    }

    /* ---------- Enums ---------- */

    public enum RecurrenceType {
        NONE, REPEATED
    }

    public enum RecurrencePattern {
        ALL, MON_SAT, CUSTOM
    }

}
