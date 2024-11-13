package newREALs.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProfileAttendanceListDTO {
    private Long userId;
    private List<Boolean> attendanceList;
}
