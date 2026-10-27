
package com.example.demo.mapper;

import org.mapstruct.MappingTarget;
import java.util.List;

public interface BaseMapper<E, REQUEST, RESPONSE> {

    E toEntity(REQUEST request);

    RESPONSE toResponse(E entity);

    List<RESPONSE> toResponseList(List<E> entities);

    void updateEntity(
            REQUEST request,
            @MappingTarget E entity
    );
}
