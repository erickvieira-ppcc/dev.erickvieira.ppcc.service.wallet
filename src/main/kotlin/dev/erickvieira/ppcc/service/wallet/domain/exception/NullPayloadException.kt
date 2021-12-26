package dev.erickvieira.ppcc.service.wallet.domain.exception

import dev.erickvieira.ppcc.service.wallet.web.api.model.ApiErrorType

class NullPayloadException(payload: String? = null) : ConflictException(
    message = "The${payload?.let { it.ifBlank { " " } } ?: " $payload "}payload can't be null",
    type = ApiErrorType.nullPayload
)