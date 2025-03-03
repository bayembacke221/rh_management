package sn.bmbacke.rh.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import sn.bmbacke.rh.exception.ResourceNotFoundException;
import sn.bmbacke.rh.repository.GenericRepository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GenericService<T, ID extends Serializable> {

    protected final GenericRepository<T, ID> repository;

    public GenericService(GenericRepository<T, ID> repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> findAll(boolean pageable, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        List<T> items = new ArrayList<T>();
        if (!pageable){
            items= repository.findAll();
        }else {
            Pageable paging = PageRequest.of(page-1, size);
            Page<T> pageTuts=repository.findAll(paging);
            items = new ArrayList<>(pageTuts.getContent());
            result.put("totalItems", pageTuts.getTotalElements());
            result.put("totalPages", pageTuts.getTotalPages());
        }
        result.put("data", items);
        return result;
    }

    @Transactional(readOnly = true)
    public T findById(ID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La ressource avec l'id " + id + " n'existe pas"));
    }

    @Transactional
    public T save(T entity) {
        return repository.save(entity);
    }

    @Transactional
    public void deleteById(ID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("La ressource avec l'id " + id + " n'existe pas");
        }
        repository.deleteById(id);
    }

    @Transactional
    public T update(ID id,T entity) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("La ressource avec l'id " + id + " n'existe pas");
        }
        return repository.save(entity);
    }

}
