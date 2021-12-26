package dev.erickvieira.ppcc.service.wallet.domain.exception

import dev.erickvieira.ppcc.service.wallet.web.api.model.ApiErrorType

class DuplicatedWalletSurnameException(surname: String?) : ConflictException(
    message = "The given wallet surname already had been taken: $surname",
    type = ApiErrorType.duplicatedWalletSurname
)