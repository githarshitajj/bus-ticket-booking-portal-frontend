package com.team4.frontend.controller;

import com.team4.frontend.client.ApiClient;
import com.team4.frontend.dto.request.DriverRequestDto;
import com.team4.frontend.dto.response.DriverResponseDto;
import com.team4.frontend.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final ApiClient apiClient;

    // Show Add Form
    @GetMapping("/add")
    public String showAddDriverForm(Model model) {
        model.addAttribute("driver", new DriverRequestDto(null, "", "", "", null, null));
        model.addAttribute("mode", "add");
        return "add-edit-driver";
    }

    // Show Edit Form
    @GetMapping("/edit/{id}")
    public String showEditDriverForm(@PathVariable Integer id, Model model) {
        ApiResponse<DriverResponseDto> response = apiClient.getDriverById(id);

        if (!"success".equalsIgnoreCase(response.getStatus()) || response.getData() == null) {
            model.addAttribute("error", "Failed to load driver: " + response.getMessage());
            return "redirect:/drivers";
        }

        DriverResponseDto d = response.getData();
        model.addAttribute("driver", new DriverRequestDto(
                d.id(), d.licenseNumber(), d.name(), d.phone(),
                d.office() != null ? d.office().id() : null,
                d.address() != null ? d.address().id() : null
        ));
        model.addAttribute("mode", "edit");
        return "add-edit-driver";
    }

    // Handle Add or Update
    @PostMapping("/save")
    public String saveDriver(@ModelAttribute DriverRequestDto driver,
                             @RequestParam String mode,
                             Model model) {
        try {
            ApiResponse<DriverResponseDto> response;

            if ("add".equals(mode)) {
                response = apiClient.createDriver(driver);
            } else {
                response = apiClient.updateDriver(driver.id(), driver);
            }

            if (!"success".equalsIgnoreCase(response.getStatus())) {
                model.addAttribute("driver", driver);
                model.addAttribute("mode", mode);
                model.addAttribute("error", "Failed to save driver: " + response.getMessage());
                return "add-edit-driver";
            }

        } catch (Exception e) {
            log.error("Error saving driver", e);
            model.addAttribute("driver", driver);
            model.addAttribute("mode", mode);
            model.addAttribute("error", "Something went wrong: " + e.getMessage());
            return "add-edit-driver";
        }

        return "redirect:/drivers";
    }
}
