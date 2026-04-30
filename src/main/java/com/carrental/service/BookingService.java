package com.carrental.service;

import com.carrental.dto.BookingRequest;
import com.carrental.dto.BookingResponse;
import com.carrental.model.Booking;
import com.carrental.model.Car;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;
    private final CarService carService;

    public BookingResponse createBooking(String userId, BookingRequest request) {
        Car car = carService.getCarById(request.getCarId());
        
        if (!car.getAvailable()) {
            throw new RuntimeException("Car is not available for booking");
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Start date cannot be in the past");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        BigDecimal totalPrice = car.getPricePerDay().multiply(BigDecimal.valueOf(days));

        Booking booking = Booking.builder()
                .userId(userId)
                .carId(request.getCarId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays((int) days)
                .totalPrice(totalPrice)
                .status(Booking.BookingStatus.PENDING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        
        car.setAvailable(false);
        carRepository.save(car);

        return mapToResponse(savedBooking, car);
    }

    public List<BookingResponse> getUserBookings(String userId) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return bookings.stream()
                .map(booking -> {
                    Car car = carService.getCarById(booking.getCarId());
                    return mapToResponse(booking, car);
                })
                .collect(Collectors.toList());
    }

    public List<BookingResponse> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAllByOrderByCreatedAtDesc();
        return bookings.stream()
                .map(booking -> {
                    Car car = carService.getCarById(booking.getCarId());
                    return mapToResponse(booking, car);
                })
                .collect(Collectors.toList());
    }

    public void cancelBooking(String bookingId, String userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to cancel this booking");
        }

        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Only pending bookings can be cancelled");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        Car car = carService.getCarById(booking.getCarId());
        car.setAvailable(true);
        carRepository.save(car);
    }

    public BookingResponse updateBookingStatus(String bookingId, Booking.BookingStatus status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(status);
        Booking savedBooking = bookingRepository.save(booking);

        if (status == Booking.BookingStatus.CANCELLED || status == Booking.BookingStatus.COMPLETED) {
            Car car = carService.getCarById(booking.getCarId());
            car.setAvailable(true);
            carRepository.save(car);
        }

        Car car = carService.getCarById(booking.getCarId());
        return mapToResponse(savedBooking, car);
    }

    private BookingResponse mapToResponse(Booking booking, Car car) {
        BookingResponse.CarSummary carSummary = BookingResponse.CarSummary.builder()
                .id(car.getId())
                .brand(car.getBrand())
                .model(car.getModel())
                .imageUrl(car.getImageUrl())
                .build();

        return BookingResponse.fromBooking(booking, carSummary);
    }
}
