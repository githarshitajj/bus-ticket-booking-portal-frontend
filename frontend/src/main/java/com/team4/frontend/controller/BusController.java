package com.team4.frontend.controller;

import com.team4.frontend.client.ApiClient;
import com.team4.frontend.dto.request.BusRequestDto;
import com.team4.frontend.dto.response.BusResponseDto;
import com.team4.frontend.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/buses")
@RequiredArgsConstructor
public class BusController {

    private final ApiClient apiClient;

    @GetMapping("/add")
    public String showAddBusForm(Model model) {
        model.addAttribute("bus", new BusRequestDto(null, "", 0, "", null));
        model.addAttribute("mode", "add");
        return "add-edit-bus";
    }

    @GetMapping("/edit/{id}")
    public String showEditBusForm(@PathVariable Integer id, Model model) {
        ApiResponse<BusResponseDto> response = apiClient.getBusById(id);

        if (!"success".equalsIgnoreCase(response.getStatus()) || response.getData() == null) {
            model.addAttribute("error", "Failed to load bus: " + response.getMessage());
            return "redirect:/buses";
        }

        BusResponseDto bus = response.getData();
        BusRequestDto dto = new BusRequestDto(
                bus.id(),
                bus.registrationNumber(),
                bus.capacity(),
                bus.type(),
                bus.office() != null ? bus.office().id() : null
        );

        model.addAttribute("bus", dto);
        model.addAttribute("mode", "edit");
        return "add-edit-bus";
    }

    @PostMapping("/update")
    public String updateBus(@ModelAttribute BusRequestDto busDto,
                            @RequestParam String mode,
                            Model model) {
        try {
            ApiResponse<BusResponseDto> response;

            if ("add".equals(mode)) {
                response = apiClient.createBus(busDto);
            } else {
                response = apiClient.updateBus(busDto.id(), busDto);
            }

            if (!"success".equalsIgnoreCase(response.getStatus())) {
                model.addAttribute("bus", busDto);
                model.addAttribute("mode", mode);
                model.addAttribute("error", "Failed to save bus: " + response.getMessage());
                return "add-edit-bus";
            }

        } catch (Exception e) {
            log.error("Error saving bus", e);
            model.addAttribute("bus", busDto);
            model.addAttribute("mode", mode);
            model.addAttribute("error", "Something went wrong: " + e.getMessage());
            return "add-edit-bus";
        }

        return "redirect:/buses";
    }
}
