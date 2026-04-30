package com.carrental.service;

import com.carrental.dto.CarRequest;
import com.carrental.model.Car;
import com.carrental.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;

    public List<Car> getAllAvailableCars() {
        return carRepository.findByAvailableTrue();
    }

    public Car getCarById(String id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Car not found with id: " + id));
    }

    public List<Car> filterCars(String category, String fuel, BigDecimal minPrice, BigDecimal maxPrice) {
        List<Car.Category> categories = (category != null && !category.isEmpty())
                ? Collections.singletonList(Car.Category.valueOf(category))
                : List.of(Car.Category.values());
        
        List<String> fuelTypes = (fuel != null && !fuel.isEmpty())
                ? Collections.singletonList(fuel)
                : List.of("Petrol", "Diesel", "Electric", "Hybrid");

        BigDecimal min = minPrice != null ? minPrice : BigDecimal.ZERO;
        BigDecimal max = maxPrice != null ? maxPrice : new BigDecimal("999999");

        return carRepository.findByFilters(categories, fuelTypes, min, max);
    }

    public Car createCar(CarRequest request) {
        Car car = Car.builder()
                .brand(request.getBrand())
                .model(request.getModel())
                .year(request.getYear())
                .pricePerDay(request.getPricePerDay())
                .seats(request.getSeats())
                .fuelType(request.getFuelType())
                .transmission(request.getTransmission())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .available(request.getAvailable())
                .location(request.getLocation())
                .build();

        return carRepository.save(car);
    }

    public Car updateCar(String id, CarRequest request) {
        Car existingCar = getCarById(id);
        
        existingCar.setBrand(request.getBrand());
        existingCar.setModel(request.getModel());
        existingCar.setYear(request.getYear());
        existingCar.setPricePerDay(request.getPricePerDay());
        existingCar.setSeats(request.getSeats());
        existingCar.setFuelType(request.getFuelType());
        existingCar.setTransmission(request.getTransmission());
        existingCar.setCategory(request.getCategory());
        existingCar.setImageUrl(request.getImageUrl());
        existingCar.setLocation(request.getLocation());
        
        return carRepository.save(existingCar);
    }

    public void deleteCar(String id) {
        Car car = getCarById(id);
        carRepository.delete(car);
    }

    public void updateCarAvailability(String id, boolean available) {
        Car car = getCarById(id);
        car.setAvailable(available);
        carRepository.save(car);
    }
}
