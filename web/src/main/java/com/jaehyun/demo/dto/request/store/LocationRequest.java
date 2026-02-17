package com.jaehyun.demo.dto.request.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationRequest {

    private Double longitude;
    private Double latitude;
    private Double radius;

}
