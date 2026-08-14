package com.abhinav.warehouse.service;

import com.abhinav.warehouse.dto.WarehouseRequest;
import com.abhinav.warehouse.entity.Warehouse;
import com.abhinav.warehouse.exception.NotFoundException;
import com.abhinav.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public List<Warehouse> findAll() {
        return warehouseRepository.findAll();
    }

    public Warehouse findById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("warehouse", id));
    }

    @Transactional
    public Warehouse create(WarehouseRequest req) {
        return warehouseRepository.save(Warehouse.builder()
                .name(req.name()).city(req.city())
                .latitude(req.latitude()).longitude(req.longitude())
                .capacity(req.capacity())
                .active(req.active() == null || req.active())
                .build());
    }

    @Transactional
    public Warehouse update(Long id, WarehouseRequest req) {
        Warehouse w = findById(id);
        w.setName(req.name());
        w.setCity(req.city());
        w.setLatitude(req.latitude());
        w.setLongitude(req.longitude());
        w.setCapacity(req.capacity());
        if (req.active() != null) w.setActive(req.active());
        return w;   // dirty checking flushes on commit
    }

    /**
     * Deactivate rather than delete. Fulfillment logs reference warehouses, and
     * an audit trail pointing at a deleted row is worthless. An inactive
     * warehouse is skipped by the selector but keeps its history.
     */
    @Transactional
    public void deactivate(Long id) {
        findById(id).setActive(false);
    }
}
