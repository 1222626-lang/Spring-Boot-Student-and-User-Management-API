
package com.example.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CrudService<ID, REQUEST, RESPONSE> {

    RESPONSE create(REQUEST request);

    RESPONSE getById(ID id);

    Page<RESPONSE> getAll(Pageable pageable);

    RESPONSE update(ID id, REQUEST request);

    void delete(ID id);
}
