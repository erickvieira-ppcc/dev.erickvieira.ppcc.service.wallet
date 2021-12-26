package dev.erickvieira.ppcc.service.wallet.domain.exception

import dev.erickvieira.ppcc.service.wallet.web.api.model.ApiErrorType

class WalletNotFoundException(vararg search: Pair<String, Any?>) : NotFoundException(
    message = "No wallets found using the search terms provided",
    type = ApiErrorType.walletNotFound,
    search = search
)
