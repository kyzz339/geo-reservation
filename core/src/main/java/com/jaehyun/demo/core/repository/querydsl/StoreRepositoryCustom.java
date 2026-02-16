package com.jaehyun.demo.core.repository.querydsl;

import com.jaehyun.demo.core.entity.Store;
import org.locationtech.jts.geom.Point;

import java.util.List;

public interface StoreRepositoryCustom {

    List<Store> listStoreNearBy(Point location , Double radius);

}
