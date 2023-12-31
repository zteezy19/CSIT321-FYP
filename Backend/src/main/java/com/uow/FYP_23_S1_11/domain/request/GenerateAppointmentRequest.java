package com.uow.FYP_23_S1_11.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GenerateAppointmentRequest {
    private Integer doctorId;
    private String generatedDate;
}
