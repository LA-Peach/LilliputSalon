package com.LilliputSalon.SalonApp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.LilliputSalon.SalonApp.domain.ServiceCategory;
import com.LilliputSalon.SalonApp.repository.ServiceCategoryRepository;
import com.LilliputSalon.SalonApp.repository.ServiceRepository;

//Cannot import the Service entity normally due to its name and conflicts

@Service
public class ServiceService {

    private final ServiceRepository serviceRepo;
    private final ServiceCategoryRepository categoryRepo;

    public ServiceService(ServiceRepository serviceRepo, ServiceCategoryRepository categoryRepo) {
        this.serviceRepo = serviceRepo;
        this.categoryRepo = categoryRepo;
    }

    public List<com.LilliputSalon.SalonApp.domain.Service> getAllAvailable() {
        return serviceRepo.findByIsAvailableTrue();
    }

    public Optional<com.LilliputSalon.SalonApp.domain.Service> getById(Long id) {
        return serviceRepo.findById(id);
    }

    public List<ServiceCategory> getAllCategoriesOrdered() {
        return categoryRepo.findAllByOrderByDisplayOrderAsc();
    }

    public Optional<ServiceCategory> getCategoryById(Long id) {
        return categoryRepo.findById(id);
    }

    public com.LilliputSalon.SalonApp.domain.Service save(
            com.LilliputSalon.SalonApp.domain.Service service
    ) {
        return serviceRepo.save(service);
    }

    public void archive(Long id) {
        serviceRepo.findById(id).ifPresent(s -> {
            s.setIsAvailable(false);
            serviceRepo.save(s);
        });
    }

    public boolean serviceNameExists(String name) {
        return serviceRepo.existsByNameIgnoreCase(name);
    }

    public void unarchive(Long id) {
        serviceRepo.findById(id).ifPresent(s -> {
            s.setIsAvailable(true);
            serviceRepo.save(s);
        });
    }



}
