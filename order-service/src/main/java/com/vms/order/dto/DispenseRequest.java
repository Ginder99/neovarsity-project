package com.vms.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DispenseRequest(@JsonProperty("qr_payload") String qrPayload) {}
