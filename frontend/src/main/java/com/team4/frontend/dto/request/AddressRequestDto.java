package com.team4.frontend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record AddressRequestDto(Integer id, @NotNull @Size(max = 255) String address, @NotNull @Size(max = 255) String city,
                                @NotNull @Size(max = 255) String state,
                                @NotNull @Size(max = 10) String zipCode) implements Serializable {
}