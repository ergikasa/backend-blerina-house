package com.blerinahouse.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreatePaymentIntentRequest(@NotNull Long reservationId) {}