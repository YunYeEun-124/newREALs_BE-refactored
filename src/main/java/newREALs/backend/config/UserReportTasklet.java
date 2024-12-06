package newREALs.backend.config;

import lombok.RequiredArgsConstructor;
import newREALs.backend.service.SubInterestService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserReportTasklet implements Tasklet {
    private final SubInterestService subInterestService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        // 모든 유저에 대한 보고서 생성
        subInterestService.generateReportsForAllUsers();
        return RepeatStatus.FINISHED;
    }
}
