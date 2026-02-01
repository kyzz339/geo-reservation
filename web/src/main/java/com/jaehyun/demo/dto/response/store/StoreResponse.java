package com.jaehyun.demo.dto.response.store;

import com.jaehyun.demo.core.entity.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreResponse {

    private Long id;
    private String name;
    private String description;
    private Double latitude;
    private Double longitude;
    private String address;

    public static StoreResponse from(Store store){
        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .description(store.getDescription())
                .latitude(store.getLocation().getY())
                .longitude(store.getLocation().getX())
                .address(store.getAddress())
                .build();
    }

}
