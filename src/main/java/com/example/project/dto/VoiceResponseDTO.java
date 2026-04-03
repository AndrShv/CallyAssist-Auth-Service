


package com.example.project.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceResponseDTO {
    private String response;
    private Integer voiceRequestsToday;
    private Integer voiceRequestsLimit;
    private Integer remainingToday;
    private Boolean isPaidUser;
}
