package newREALs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProfileAttendanceListDto {
    private Long user_id;
    private List<Boolean> attendanceList;
}
