package com.taipower.intranet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class APMCUST {
    String custNo;
    String custName;
    String custCity;
    String custTown;
    String custRoad;
    String custLine;
    String custPtcod;
    String useTp;
    String contTp;
    String discTp;
    String speCustTp;
    String mailCity;
    String mailTown;
    String mailRoad;
    String mailLine;
    String cycleDay;
    String bussId;
}
