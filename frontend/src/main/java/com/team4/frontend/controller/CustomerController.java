package com.team4.frontend.controller;

import com.team4.frontend.client.ApiClient;
import com.team4.frontend.dto.request.CustomerRequestDto;
import com.team4.frontend.dto.response.CustomerResponseDto;
import com.team4.frontend.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final ApiClient apiClient;

    @GetMapping("/add")
    public String showAddCustomerForm(Model model) {
        model.addAttribute("customer", new CustomerRequestDto(null, "", "", "", null));
        model.addAttribute("mode", "add");
        return "add-edit-customer";
    }

    @GetMapping("/edit/{id}")
    public String showEditCustomerForm(@PathVariable Integer id, Model model) {
        ApiResponse<CustomerResponseDto> response = apiClient.getCustomerById(id);

        if (!"success".equalsIgnoreCase(response.getStatus()) || response.getData() == null) {
            model.addAttribute("error", "Failed to load customer: " + response.getMessage());
            return "redirect:/customers";
        }

        CustomerResponseDto customer = response.getData();
        CustomerRequestDto dto = new CustomerRequestDto(
                customer.id(),
                customer.name(),
                customer.email(),
                customer.phone(),
                customer.address() != null ? customer.address().id() : null
        );
        model.addAttribute("customer", dto);
        model.addAttribute("mode", "edit");
        return "add-edit-customer";
    }

    @PostMapping("/update")
    public String updateCustomer(@ModelAttribute CustomerRequestDto customer,
                                 @RequestParam String mode,
                                 Model model) {
        try {
            ApiResponse<CustomerResponseDto> response;

            if ("add".equals(mode)) {
                response = apiClient.createCustomer(customer);
            } else {
                response = apiClient.updateCustomer(customer.id(), customer);
            }

            if (!"success".equalsIgnoreCase(response.getStatus())) {
                model.addAttribute("customer", customer);
                model.addAttribute("mode", mode);
                model.addAttribute("error", "Failed to save customer: " + response.getMessage());
                return "add-edit-customer";
            }

        } catch (Exception e) {
            log.error("Error saving customer", e);
            model.addAttribute("customer", customer);
            model.addAttribute("mode", mode);
            model.addAttribute("error", "Something went wrong: " + e.getMessage());
            return "add-edit-customer";
        }

        return "redirect:/customers";
    }
}
