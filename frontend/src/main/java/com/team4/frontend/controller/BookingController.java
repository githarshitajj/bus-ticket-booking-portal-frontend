package com.team4.frontend.controller;

import com.team4.frontend.client.ApiClient;
import com.team4.frontend.dto.request.BookingRequestDto;
import com.team4.frontend.dto.response.BookingResponseDto;
import com.team4.frontend.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {

    private final ApiClient apiClient;

    @GetMapping("/edit/{id}")
    public String showEditBookingForm(@PathVariable Integer id, Model model) {
        ApiResponse<BookingResponseDto> response = apiClient.getBookingById(id);

        if (!"success".equalsIgnoreCase(response.getStatus()) || response.getData() == null) {
            model.addAttribute("error", "Failed to load booking details: " + response.getMessage());
            return "redirect:/bookings";
        }

        BookingResponseDto booking = response.getData();
        BookingRequestDto form = new BookingRequestDto(
                booking.id(),
                booking.trip().id(),
                booking.seatNumber(),
                booking.status()
        );

        model.addAttribute("booking", form);
        model.addAttribute("mode", "edit");
        return "add-edit-booking";
    }

    @GetMapping("/add")
    public String showAddBookingForm(Model model) {
        model.addAttribute("booking", new BookingRequestDto(null, null, null, null));
        model.addAttribute("mode", "add");
        return "add-edit-booking";
    }

    @PostMapping("/update")
    public String updateBooking(@ModelAttribute BookingRequestDto bookingRequestDto,
                                @RequestParam String mode,
                                Model model) {
        try {
            ApiResponse<BookingResponseDto> response;

            if ("add".equals(mode)) {
                response = apiClient.createBooking(bookingRequestDto);
            } else {
                response = apiClient.updateBooking(bookingRequestDto.id(), bookingRequestDto);
            }

            if (!"success".equalsIgnoreCase(response.getStatus())) {
                model.addAttribute("booking", bookingRequestDto);
                model.addAttribute("mode", mode);
                model.addAttribute("error", "Failed to save booking: " + response.getMessage());
                return "add-edit-booking";
            }

        } catch (Exception ex) {
            log.error("Exception during booking save", ex);
            model.addAttribute("booking", bookingRequestDto);
            model.addAttribute("mode", mode);
            model.addAttribute("error", "Something went wrong: " + ex.getMessage());
            return "add-edit-booking";
        }

        return "redirect:/bookings";
    }
}
