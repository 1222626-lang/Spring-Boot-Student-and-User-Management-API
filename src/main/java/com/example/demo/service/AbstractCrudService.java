
package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.BaseMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractCrudService<
        E, ID, REQUEST, RESPONSE>
        implements CrudService<ID, REQUEST, RESPONSE> {

    protected final JpaRepository<E, ID> repository;

    protected final BaseMapper<E, REQUEST, RESPONSE> mapper;

    protected AbstractCrudService(
            JpaRepository<E, ID> repository,
            BaseMapper<E, REQUEST, RESPONSE> mapper) {

        this.repository = repository;
        this.mapper = mapper;
    }

    // CREATE
    @Override
    @Transactional
    public RESPONSE create(REQUEST request) {

        E entity = mapper.toEntity(request);

        E saved = repository.save(entity);

        return mapper.toResponse(saved);
    }

    // GET BY ID
    @Override
    @Transactional(readOnly = true)
    public RESPONSE getById(ID id) {

        E entity = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Record", id));

        return mapper.toResponse(entity);
    }

    // GET ALL WITH PAGINATION
    @Override
    @Transactional(readOnly = true)
    public Page<RESPONSE> getAll(Pageable pageable) {

        return repository.findAll(pageable)
                .map(mapper::toResponse);
    }

    // UPDATE
    @Override
    @Transactional
    public RESPONSE update(ID id, REQUEST request) {

        E existing = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Record", id));

        mapper.updateEntity(request, existing);

        E saved = repository.save(existing);

        return mapper.toResponse(saved);
    }

    // DELETE
    @Override
    @Transactional
    public void delete(ID id) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Record", id);
        }

        repository.deleteById(id);
    }
}
