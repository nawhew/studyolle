package studyolle.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import studyolle.event.domain.Event;
import studyolle.event.domain.EventType;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder @NoArgsConstructor @AllArgsConstructor
public class EventForm {

    @NotBlank
    @Length(max = 50)
    private String title;

    private EventType eventType;

    private Integer limitOfEnrollments;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endEnrollmentDateTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDateTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDateTime;

    private String description;

    public Event toEntity() {
        return Event.builder()
                .title(this.title)
                .eventType(this.eventType)
                .limitOfEnrollments(this.limitOfEnrollments)
                .endEnrollmentDateTime(this.endEnrollmentDateTime)
                .startDateTime(this.startDateTime)
                .endDateTime(this.endDateTime)
                .description(this.description)
                .build();
    }

    public static EventForm of(Event event) {
        return EventForm.builder()
                .title(event.getTitle())
                .eventType(event.getEventType())
                .limitOfEnrollments(event.getLimitOfEnrollments())
                .endEnrollmentDateTime(event.getEndEnrollmentDateTime())
                .startDateTime(event.getStartDateTime())
                .endDateTime(event.getEndDateTime())
                .description(event.getDescription())
                .build();
    }

}
