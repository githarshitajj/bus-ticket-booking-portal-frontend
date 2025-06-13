package com.team4.frontend.controller;

import com.team4.frontend.client.ApiClient;
import com.team4.frontend.dto.response.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class ViewController {

    private final ApiClient apiClient;

    public ViewController(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @GetMapping("/")
    public String showTiles() {
        return "index";
    }

    @GetMapping("/bookings")
    public String bookings(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model) {
        ApiResponse<PagedResponse<BookingResponseDto>> response = apiClient.getAllBookings(page, size);
        if (!"success".equalsIgnoreCase(response.getStatus()) || response.getData() == null) {
            model.addAttribute("error", "Failed to fetch bookings: " + response.getMessage());
            return "bookings";
        }
        addPaginationAttributes(model, response.getData());
        return "bookings";
    }

    @GetMapping("/trips")
    public String trips(
            @RequestParam(required = false) String fromCity,
            @RequestParam(required = false) String toCity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        ApiResponse<PagedResponse<TripResponseDto>> response = apiClient.getAllTrips(page, size, fromCity, toCity);
        if (!"success".equalsIgnoreCase(response.getStatus()) || response.getData() == null) {
            model.addAttribute("error", "Failed to fetch trips: " + response.getMessage());
            return "trips";
        }

        model.addAttribute("items", response.getData().getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", response.getData().getTotalPages());
        model.addAttribute("fromCity", fromCity);
        model.addAttribute("toCity", toCity);

        ApiResponse<PagedResponse<RouteResponseDto>> routesResponse = apiClient.getAllRoutes(0, 100);
        if (!"success".equalsIgnoreCase(routesResponse.getStatus()) || routesResponse.getData() == null) {
            model.addAttribute("error", "Failed to load route data for filters.");
            model.addAttribute("fromCities", Set.of());
            model.addAttribute("toCities", Set.of());
            return "trips";
        }

        var routes = routesResponse.getData().getContent();

        Set<String> fromCities = routes.stream()
                .map(RouteResponseDto::fromCity)
                .collect(Collectors.toSet());

        Set<String> toCities = routes.stream()
                .map(RouteResponseDto::toCity)
                .collect(Collectors.toSet());

        model.addAttribute("fromCities", fromCities);
        model.addAttribute("toCities", toCities);

        return "trips";
    }

    @GetMapping("/drivers")
    public String drivers(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          Model model) {
        ApiResponse<PagedResponse<DriverResponseDto>> response = apiClient.getAllDrivers(page, size);
        if (!"success".equalsIgnoreCase(response.getStatus()) || response.getData() == null) {
            model.addAttribute("error", "Failed to fetch drivers: " + response.getMessage());
            return "drivers";
        }
        addPaginationAttributes(model, response.getData());
        return "drivers";
    }

    @GetMapping("/buses")
    public String buses(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {
        ApiResponse<PagedResponse<BusResponseDto>> response = apiClient.getAllBuses(page, size);
        if (!"success".equalsIgnoreCase(response.getStatus()) || response.getData() == null) {
            model.addAttribute("error", "Failed to fetch buses: " + response.getMessage());
            return "buses";
        }
        addPaginationAttributes(model, response.getData());
        return "buses";
    }

    @GetMapping("/offices")
    public String offices(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          Model model) {
        ApiResponse<PagedResponse<AgencyOfficeResponseDto>> response = apiClient.getAllAgencyOffices(page, size);
        if (!"success".equalsIgnoreCase(response.getStatus()) || response.getData() == null) {
            model.addAttribute("error", "Failed to fetch offices: " + response.getMessage());
            return "offices";
        }
        addPaginationAttributes(model, response.getData());
        return "offices";
    }

    @GetMapping("/customers")
    public String customers(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {
        ApiResponse<PagedResponse<CustomerResponseDto>> response = apiClient.getAllCustomers(page, size);
        if (!"success".equalsIgnoreCase(response.getStatus()) || response.getData() == null) {
            model.addAttribute("error", "Failed to fetch customers: " + response.getMessage());
            return "customers";
        }
        addPaginationAttributes(model, response.getData());
        return "customers";
    }

    @GetMapping("/payments")
    public String payments(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String paymentStatus,
                           @RequestParam(required = false) String bookingStatus,
                           Model model) {
        ApiResponse<PagedResponse<PaymentResponseDto>> response = apiClient.getAllPayments(page, size, paymentStatus, bookingStatus);

        model.addAttribute("paymentStatus", paymentStatus);
        model.addAttribute("bookingStatus", bookingStatus);
        model.addAttribute("paymentStatuses", List.of("Pending", "Success", "Failed"));
        model.addAttribute("bookingStatuses", List.of("Booked", "Available"));

        if (!"success".equalsIgnoreCase(response.getStatus()) || response.getData() == null) {
            model.addAttribute("error", "Failed to fetch payments: " + response.getMessage());
            return "payments";
        }

        addPaginationAttributes(model, response.getData());
        return "payments";
    }

    private <T> void addPaginationAttributes(Model model, PagedResponse<T> response) {
        model.addAttribute("items", response.getContent());
        model.addAttribute("currentPage", response.getPage());
        model.addAttribute("pageSize", response.getSize());
        model.addAttribute("totalPages", response.getTotalPages());
        model.addAttribute("totalElements", response.getTotalElements());
    }
}
