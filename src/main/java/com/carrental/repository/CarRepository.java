package com.carrental.repository;

import com.carrental.model.Car;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CarRepository extends MongoRepository<Car, String> {

    List<Car> findByAvailableTrue();

    List<Car> findByCategoryAndAvailableTrue(Car.Category category);

    List<Car> findByFuelTypeAndAvailableTrue(String fuelType);

    @Query("{ 'available': true, 'pricePerDay': { $gte: ?0, $lte: ?1 } }")
    List<Car> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    @Query("{ 'available': true, 'category': { $in: ?0 }, 'fuelType': { $in: ?1 }, 'pricePerDay': { $gte: ?2, $lte: ?3 } }")
    List<Car> findByFilters(List<Car.Category> categories, List<String> fuelTypes, BigDecimal minPrice, BigDecimal maxPrice);
}
