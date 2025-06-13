package com.team4.frontend.controller;

import com.team4.frontend.client.ApiClient;
import com.team4.frontend.dto.form.TripFormDto;
import com.team4.frontend.dto.request.TripRequestDto;
import com.team4.frontend.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Controller
@RequestMapping("/trips")
@RequiredArgsConstructor
public class TripController {

    private final ApiClient apiClient;

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("trip", new TripFormDto());
        model.addAttribute("mode", "add");

        boolean populated = populateDropdowns(model);
        if (!populated) {
            model.addAttribute("error", "Failed to load required dropdown data.");
        }

        return "add-edit-trip";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        ApiResponse<TripResponseDto> response = apiClient.getTripById(id);

        if (!"success".equalsIgnoreCase(response.getStatus()) || response.getData() == null) {
            model.addAttribute("error", "Failed to load trip: " + response.getMessage());
            return "redirect:/trips";
        }

        TripResponseDto trip = response.getData();

        TripFormDto formDto = new TripFormDto();
        formDto.setId(trip.id());
        formDto.setRouteId(trip.route().id());
        formDto.setBusId(trip.bus().id());
        formDto.setDriver1Id(trip.driver1Driver().id());
        formDto.setDriver2Id(trip.driver2Driver().id());
        formDto.setBoardingAddressId(trip.boardingAddressId());
        formDto.setDroppingAddressId(trip.droppingAddressId());
        formDto.setDepartureTime(LocalDateTime.ofInstant(trip.departureTime(), ZoneId.systemDefault()));
        formDto.setArrivalTime(LocalDateTime.ofInstant(trip.arrivalTime(), ZoneId.systemDefault()));
        formDto.setAvailableSeats(trip.availableSeats());
        formDto.setFare(trip.fare());
        formDto.setTripDate(LocalDate.ofInstant(trip.tripDate(), ZoneId.systemDefault()));

        model.addAttribute("trip", formDto);
        model.addAttribute("mode", "edit");

        boolean populated = populateDropdowns(model);
        if (!populated) {
            model.addAttribute("error", "Failed to load required dropdown data.");
        }

        return "add-edit-trip";
    }

    @PostMapping("/save")
    public String saveTrip(@ModelAttribute TripFormDto formDto, @RequestParam String mode, Model model) {
        TripRequestDto dto = new TripRequestDto(
                formDto.getId(),
                formDto.getRouteId(),
                formDto.getBusId(),
                formDto.getDriver1Id(),
                formDto.getDriver2Id(),
                formDto.getBoardingAddressId(),
                formDto.getDroppingAddressId(),
                formDto.getDepartureTime() != null ? formDto.getDepartureTime().atZone(ZoneId.systemDefault()).toInstant() : null,
                formDto.getArrivalTime() != null ? formDto.getArrivalTime().atZone(ZoneId.systemDefault()).toInstant() : null,
                formDto.getAvailableSeats(),
                formDto.getFare(),
                formDto.getTripDate() != null ? formDto.getTripDate().atStartOfDay(ZoneId.systemDefault()).toInstant() : null
        );

        log.debug(dto.toString());

        try {
            if ("add".equals(mode)) {
                ApiResponse<TripResponseDto> response = apiClient.createTrip(dto);
                if (!"success".equalsIgnoreCase(response.getStatus())) {
                    model.addAttribute("error", "Failed to add trip: " + response.getMessage());
                    return "add-edit-trip";
                }
            } else {
                ApiResponse<TripResponseDto> response = apiClient.updateTrip(dto.id(), dto);
                if (!"success".equalsIgnoreCase(response.getStatus())) {
                    model.addAttribute("error", "Failed to update trip: " + response.getMessage());
                    return "add-edit-trip";
                }
            }
        } catch (Exception e) {
            log.error("Error saving trip", e);
            model.addAttribute("error", "Something went wrong: " + e.getMessage());
            return "add-edit-trip";
        }

        return "redirect:/trips";
    }

    private boolean populateDropdowns(Model model) {
        try {
            var routeResp = apiClient.getAllRoutes(0, 100);
            var busResp = apiClient.getAllBuses(0, 100);
            var driverResp = apiClient.getAllDrivers(0, 100);

            boolean allOk = "success".equalsIgnoreCase(routeResp.getStatus()) &&
                    "success".equalsIgnoreCase(busResp.getStatus()) &&
                    "success".equalsIgnoreCase(driverResp.getStatus());

            if (!allOk || routeResp.getData() == null || busResp.getData() == null || driverResp.getData() == null) {
                return false;
            }

            model.addAttribute("routes", routeResp.getData().getContent());
            model.addAttribute("buses", busResp.getData().getContent());
            model.addAttribute("drivers", driverResp.getData().getContent());

            return true;

        } catch (Exception e) {
            log.error("Failed to populate dropdowns", e);
            return false;
        }
    }
}
