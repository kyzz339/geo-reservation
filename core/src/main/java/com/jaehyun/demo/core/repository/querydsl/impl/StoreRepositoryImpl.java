package com.jaehyun.demo.core.repository.querydsl.impl;

import com.jaehyun.demo.core.entity.QStore;
import com.jaehyun.demo.core.entity.Store;
import com.jaehyun.demo.core.repository.querydsl.StoreRepositoryCustom;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.util.List;

import static org.hibernate.spatial.SpatialFunction.distance;

@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private final QStore store = QStore.store;

    @Override
    public List<Store> listStoreNearBy(Point location, Double radius) {
        return queryFactory
                .select(store)
                .from(store)
                .where(
                        Expressions.booleanTemplate(
                                "ST_DistanceSphere({0}, {1}) <= {2}",
                                QStore.store.location,
                                location,
                                distance
                ),
                        store.deleted.isFalse()
                )
                .fetch();
    }
}
