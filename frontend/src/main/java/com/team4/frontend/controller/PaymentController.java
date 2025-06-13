package com.team4.frontend.controller;

import com.team4.frontend.client.ApiClient;
import com.team4.frontend.dto.request.BookingRequestDto;
import com.team4.frontend.dto.request.PaymentRequestDto;
import com.team4.frontend.dto.response.ApiResponse;
import com.team4.frontend.dto.response.PaymentResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Controller
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final ApiClient apiClient;

    @GetMapping("/add")
    public String showAddForm(Model model) {
        BookingRequestDto bookingDto = new BookingRequestDto(null, null, 0, "Booked");

        PaymentRequestDto dto = new PaymentRequestDto(
                null, bookingDto, null, null, null, ""
        );

        model.addAttribute("payment", dto);
        model.addAttribute("mode", "add");
        return "add-edit-payment";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        ApiResponse<PaymentResponseDto> response = apiClient.getPaymentById(id);

        if (!"success".equalsIgnoreCase(response.getStatus()) || response.getData() == null) {
            model.addAttribute("error", "Failed to fetch payment data: " + response.getMessage());
            return "redirect:/payments";
        }

        PaymentResponseDto p = response.getData();
        PaymentRequestDto form = new PaymentRequestDto(
                p.id(),
                new BookingRequestDto(p.booking().id(), p.booking().trip().id(), p.booking().seatNumber(), p.booking().status()),
                p.customer() != null ? p.customer().id() : null,
                p.amount(),
                p.paymentDate(),
                p.paymentStatus()
        );

        model.addAttribute("payment", form);
        model.addAttribute("mode", "edit");
        return "add-edit-payment";
    }

    @PostMapping("/save")
    public String savePayment(
            @RequestParam(required = false) Integer id,
            @RequestParam Integer customerId,
            @RequestParam BigDecimal amount,
            @RequestParam String paymentDate,
            @RequestParam String paymentStatus,
            @RequestParam String mode,
            @RequestParam("booking.id") Integer bookingId,
            @RequestParam("booking.tripId") Integer tripId,
            @RequestParam("booking.seatNumber") Integer seatNumber,
            @RequestParam("booking.status") String bookingStatus,
            Model model
    ) {
        try {
            Instant parsedDate = LocalDateTime
                    .parse(paymentDate)
                    .atZone(ZoneId.systemDefault())
                    .toInstant();

            BookingRequestDto bookingDto = new BookingRequestDto(
                    bookingId, tripId, seatNumber, bookingStatus
            );

            PaymentRequestDto newDto = new PaymentRequestDto(
                    id, bookingDto, customerId, amount, parsedDate, paymentStatus
            );

            ApiResponse<PaymentResponseDto> response;
            if ("add".equals(mode)) {
                response = apiClient.createPayment(newDto);
            } else {
                response = apiClient.updatePayment(newDto.id(), newDto);
            }

            if (!"success".equalsIgnoreCase(response.getStatus())) {
                model.addAttribute("error", "Failed to save payment: " + response.getMessage());
                model.addAttribute("payment", newDto);
                model.addAttribute("mode", mode);
                return "add-edit-payment";
            }

        } catch (Exception e) {
            log.error("Error while saving payment", e);
            model.addAttribute("error", "Something went wrong: " + e.getMessage());
            model.addAttribute("mode", mode);
            return "add-edit-payment";
        }

        return "redirect:/payments";
    }
}
