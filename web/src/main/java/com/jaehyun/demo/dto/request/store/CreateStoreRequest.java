package com.jaehyun.demo.dto.request.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStoreRequest {

    private String name;
    private String description;
    private Double latitude;
    private Double longitude;
    private String address;
    private Boolean active;

}
