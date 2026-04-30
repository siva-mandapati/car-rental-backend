package com.carrental.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cars")
public class Car {

    @Id
    private String id;
    
    private String brand;
    private String model;
    private Integer year;
    private BigDecimal pricePerDay;
    private Integer seats;
    private String fuelType;
    private String transmission;
    private Category category;
    private String imageUrl;
    private Boolean available;
    private String location;

    public enum Category {
        SUV, SEDAN, HATCHBACK, LUXURY
    }
}
