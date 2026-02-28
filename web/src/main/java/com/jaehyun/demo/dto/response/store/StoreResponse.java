package com.jaehyun.demo.dto.response.store;

import com.jaehyun.demo.core.entity.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

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
    private LocalTime openTime;
    private LocalTime closeTime;

    public static StoreResponse from(Store store){
        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .description(store.getDescription())
                .latitude(store.getLocation().getY())
                .longitude(store.getLocation().getX())
                .address(store.getAddress())
                .openTime(store.getOpenTime())
                .closeTime(store.getCloseTime())
                .build();
    }

}
