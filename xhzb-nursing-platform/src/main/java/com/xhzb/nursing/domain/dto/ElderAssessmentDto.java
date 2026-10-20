package com.xhzb.nursing.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElderAssessmentDto {
    private BasicInfo basicInfo;
    private HealthAssessmentDto healthAssessmentDto;
    private DailyLivingActivities dailyLivingActivities;
    private MentakState mentalState;
    private PerceptionAndCommunication perceptionAndCommunication;
    private SocialParticiption socialParticipation;
}
