package com.team4.frontend.controller;

import com.team4.frontend.client.ApiClient;
import com.team4.frontend.dto.request.AgencyOfficeRequestDto;
import com.team4.frontend.dto.response.AgencyOfficeResponseDto;
import com.team4.frontend.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/offices")
@RequiredArgsConstructor
public class OfficeController {

    private final ApiClient apiClient;

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("office", new AgencyOfficeRequestDto(null, "", "", "", null, null));
        model.addAttribute("mode", "add");
        return "add-edit-office";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        ApiResponse<AgencyOfficeResponseDto> response = apiClient.getAgencyOfficeById(id);

        if (!"success".equalsIgnoreCase(response.getStatus()) || response.getData() == null) {
            model.addAttribute("error", "Failed to load office data: " + response.getMessage());
            return "redirect:/offices";
        }

        AgencyOfficeResponseDto office = response.getData();
        AgencyOfficeRequestDto dto = new AgencyOfficeRequestDto(
                office.id(),
                office.officeMail(),
                office.officeContactPersonName(),
                office.officeContactNumber(),
                office.agency() != null ? office.agency().id() : null,
                office.officeAddress() != null ? office.officeAddress().id() : null
        );

        model.addAttribute("office", dto);
        model.addAttribute("mode", "edit");
        return "add-edit-office";
    }

    @PostMapping("/save")
    public String saveOffice(@ModelAttribute("office") AgencyOfficeRequestDto officeDto,
                             @RequestParam String mode,
                             Model model) {
        try {
            ApiResponse<AgencyOfficeResponseDto> response;
            if ("add".equals(mode)) {
                response = apiClient.createOffice(officeDto);
            } else {
                response = apiClient.updateOffice(officeDto.id(), officeDto);
            }

            if (!"success".equalsIgnoreCase(response.getStatus())) {
                model.addAttribute("error", "Failed to save office: " + response.getMessage());
                model.addAttribute("office", officeDto);
                model.addAttribute("mode", mode);
                return "add-edit-office";
            }

        } catch (Exception e) {
            log.error("Error while saving office", e);
            model.addAttribute("error", "Something went wrong: " + e.getMessage());
            model.addAttribute("office", officeDto);
            model.addAttribute("mode", mode);
            return "add-edit-office";
        }

        return "redirect:/offices";
    }
}
