package com.team4.frontend.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team4.frontend.dto.request.*;
import com.team4.frontend.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiClient {

    private final WebClient webClient;

    private String buildUri(String path, int page, int size) {
        return UriComponentsBuilder.fromPath(path)
                .queryParam("page", page)
                .queryParam("size", size)
                .toUriString();
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    private <T> ApiResponse<T> handleRequest(Supplier<Mono<ApiResponse<T>>> requestSupplier) {
        try {
            return requestSupplier.get().block();
        } catch (WebClientResponseException ex) {
            log.error("WebClient error: status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());

            String message = "Request failed";
            try {
                ApiResponse<?> errorResponse = objectMapper.readValue(ex.getResponseBodyAsString(), ApiResponse.class);
                message = errorResponse.getMessage();
            } catch (Exception parseEx) {
                log.warn("Failed to parse error body as ApiResponse: {}", parseEx.getMessage());
                message = ex.getResponseBodyAsString();
            }

            return new ApiResponse<>("fail", ex.getRawStatusCode(), message, null);
        } catch (Exception ex) {
            log.error("Unexpected error calling backend", ex);
            return new ApiResponse<>("error", 500, "Internal error", null);
        }
    }


    public ApiResponse<PagedResponse<BookingResponseDto>> getAllBookings(int page, int size) {
        return handleRequest(() -> webClient.get()
                .uri(buildUri("/bookings", page, size))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<PagedResponse<BookingResponseDto>>>() {})
                );
    }

    public ApiResponse<BookingResponseDto> getBookingById(Integer id) {
        return handleRequest(() -> webClient.get()
                .uri("/bookings/" + id)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<BookingResponseDto>>() {})
                );
    }

    public ApiResponse<BookingResponseDto> createBooking(BookingRequestDto bookingRequestDto) {
        return handleRequest(() -> webClient.post()
                .uri("/bookings")
                .bodyValue(bookingRequestDto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<BookingResponseDto>>() {})
                );
    }


    public ApiResponse<BookingResponseDto> updateBooking(Integer id, BookingRequestDto bookingRequestDto) {
        return handleRequest(() -> webClient.put()
                .uri("/bookings/" + id)
                .bodyValue(bookingRequestDto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<BookingResponseDto>>() {}));
    }



    public ApiResponse<PagedResponse<TripResponseDto>> getAllTrips(int page, int size, String fromCity, String toCity) {
        String baseUrl = "/trips";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("page", page)
                .queryParam("size", size);

        if (fromCity != null && !fromCity.isBlank()) {
            builder.queryParam("fromCity", fromCity);
        }

        if (toCity != null && !toCity.isBlank()) {
            builder.queryParam("toCity", toCity);
        }

        return handleRequest(() -> webClient.get()
                .uri(builder.toUriString())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<PagedResponse<TripResponseDto>>>() {})
                );
    }


    public ApiResponse<TripResponseDto> getTripById(Integer id) {
        return handleRequest(() -> webClient.get()
                .uri("/trips/" + id)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<TripResponseDto>>() {})
                );
    }

    public ApiResponse<TripResponseDto> createTrip(TripRequestDto tripRequestDto) {
        return handleRequest(() -> webClient.post()
                .uri("/trips")
                .bodyValue(tripRequestDto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<TripResponseDto>>() {}));
    }

    public ApiResponse<TripResponseDto> updateTrip(Integer id, TripRequestDto tripRequestDto) {
        return handleRequest(() -> webClient.put()
                .uri("/trips/" + id)
                .bodyValue(tripRequestDto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<TripResponseDto>>() {})
                );
    }

    public ApiResponse<PagedResponse<DriverResponseDto>> getAllDrivers(int page, int size) {
        return handleRequest(() -> webClient.get()
                .uri(buildUri("/drivers", page, size))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<PagedResponse<DriverResponseDto>>>() {})
                );
    }

    public ApiResponse<DriverResponseDto> getDriverById(Integer id) {
        return handleRequest(() -> webClient.get()
                .uri("/drivers/id/" + id)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<DriverResponseDto>>() {})
                );
    }

    public ApiResponse<DriverResponseDto> createDriver(DriverRequestDto dto) {
        return handleRequest(() -> webClient.post().uri("/drivers")
                .bodyValue(dto)
                .retrieve().bodyToMono(new ParameterizedTypeReference<ApiResponse<DriverResponseDto>>() {})
                );
    }

    public ApiResponse<DriverResponseDto> updateDriver(Integer id, DriverRequestDto dto) {
        return handleRequest(() -> webClient.put().uri("/drivers/" + id)
                .bodyValue(dto)
                .retrieve().bodyToMono(new ParameterizedTypeReference<ApiResponse<DriverResponseDto>>() {})
                );
    }

    public ApiResponse<PagedResponse<BusResponseDto>> getAllBuses(int page, int size) {
        return handleRequest(() -> webClient.get()
                .uri(buildUri("/buses", page, size))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<PagedResponse<BusResponseDto>>>() {})
                );
    }

    public ApiResponse<BusResponseDto> getBusById(Integer id) {
        return handleRequest(() -> webClient.get()
                .uri("/buses/id/" + id)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<BusResponseDto>>() {})
        );
    }

    public ApiResponse<BusResponseDto> createBus(BusRequestDto busRequestDto) {
        return handleRequest(() -> webClient.post()
                .uri("/buses")
                .bodyValue(busRequestDto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<BusResponseDto>>() {})
                );
    }

    public ApiResponse<BusResponseDto> updateBus(Integer id, BusRequestDto busRequestDto) {
        return handleRequest(() -> webClient.put()
                .uri("/buses/" + id)
                .bodyValue(busRequestDto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<BusResponseDto>>() {})
                );
    }


    public ApiResponse<PagedResponse<AgencyOfficeResponseDto>> getAllAgencyOffices(int page, int size) {
        return handleRequest(() -> webClient.get()
                .uri(buildUri("/agencies/offices", page, size))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<PagedResponse<AgencyOfficeResponseDto>>>() {})
                );
    }

    public ApiResponse<AgencyOfficeResponseDto> getAgencyOfficeById(Integer id) {
        return handleRequest(() -> webClient.get()
                .uri("/agencies/offices/" + id)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<AgencyOfficeResponseDto>>() {})
                );
    }

    public ApiResponse<AgencyOfficeResponseDto> createOffice(AgencyOfficeRequestDto officeDto) {
        return handleRequest(() -> webClient.post()
                .uri("/agencies/offices")
                .bodyValue(officeDto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<AgencyOfficeResponseDto>>() {})
                );
    }

    // Update office
    public ApiResponse<AgencyOfficeResponseDto> updateOffice(Integer id, AgencyOfficeRequestDto officeDto) {
        return handleRequest(() -> webClient.put()
                .uri("/agencies/offices/" + id)
                .bodyValue(officeDto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<AgencyOfficeResponseDto>>() {})
                );
    }

    public ApiResponse<PagedResponse<CustomerResponseDto>> getAllCustomers(int page, int size) {
        return handleRequest(() -> webClient.get()
                .uri(buildUri("/customers", page, size))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<PagedResponse<CustomerResponseDto>>>() {})
                );
    }

    public ApiResponse<CustomerResponseDto> getCustomerById(Integer id) {
        return handleRequest(() -> webClient.get()
                .uri("/customers/" + id)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<CustomerResponseDto>>() {})
                );
    }

    public ApiResponse<CustomerResponseDto> createCustomer(CustomerRequestDto dto) {
        return handleRequest(() -> webClient.post()
                .uri("/customers")
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<CustomerResponseDto>>() {})
                );
    }

    public ApiResponse<CustomerResponseDto> updateCustomer(Integer id, CustomerRequestDto dto) {
        return handleRequest(() -> webClient.put()
                .uri("/customers/" + id)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<CustomerResponseDto>>() {})
                );
    }


    public ApiResponse<PagedResponse<PaymentResponseDto>> getAllPayments(int page, int size, String paymentStatus, String bookingStatus) {
        return handleRequest(() -> webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/payments")
                            .queryParam("page", page)
                            .queryParam("size", size);
                    if (paymentStatus != null && !paymentStatus.isEmpty())
                        uriBuilder.queryParam("paymentStatus", paymentStatus);
                    if (bookingStatus != null && !bookingStatus.isEmpty())
                        uriBuilder.queryParam("bookingStatus", bookingStatus);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<PagedResponse<PaymentResponseDto>>>() {})
                );
    }


    public ApiResponse<PaymentResponseDto> getPaymentById(Integer id) {
        return handleRequest(() -> webClient.get()
                .uri("/payments/" + id)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<PaymentResponseDto>>() {})
                );
    }

    public ApiResponse<PaymentResponseDto> createPayment(PaymentRequestDto dto) {
        return handleRequest(() -> webClient.post()
                .uri("/payments")
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<PaymentResponseDto>>() {})
                );
    }

    public ApiResponse<PaymentResponseDto> updatePayment(Integer id, PaymentRequestDto dto) {
        return handleRequest(() -> webClient.put()
                .uri("/payments/" + id)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<PaymentResponseDto>>() {})
                );
    }

    public ApiResponse<PagedResponse<RouteResponseDto>> getAllRoutes(int page, int size) {
        return handleRequest(() -> webClient.get()
                .uri(buildUri("/routes", page, size))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<PagedResponse<RouteResponseDto>>>() {})
                );
    }

    public ApiResponse<PagedResponse<AddressResponseDto>> getAllAddresses(int page, int size) {
        return handleRequest(() -> webClient.get()
                .uri(buildUri("/routes", page, size))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<PagedResponse<AddressResponseDto>>>() {})
                );
    }
}
