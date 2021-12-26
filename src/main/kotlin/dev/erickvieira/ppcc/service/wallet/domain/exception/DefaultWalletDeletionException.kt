package dev.erickvieira.ppcc.service.wallet.domain.exception

import dev.erickvieira.ppcc.service.wallet.web.api.model.ApiErrorType
import java.util.*

class DefaultWalletDeletionException(id: UUID?, surname: String?) : BadRequestException(
    message = "It is impossible to delete the wallet (id: $id, surname: $surname) while it is the default one",
    type = ApiErrorType.forbiddenDefaultWalletDeletion
)