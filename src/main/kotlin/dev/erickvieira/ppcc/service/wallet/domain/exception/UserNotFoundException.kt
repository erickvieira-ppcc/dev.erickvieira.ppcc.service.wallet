package dev.erickvieira.ppcc.service.wallet.domain.exception

import dev.erickvieira.ppcc.service.wallet.web.api.model.ApiErrorType

class UserNotFoundException(vararg search: Pair<String, Any?>) : NotFoundException(
    message = "No users found using the search terms provided",
    type = ApiErrorType.userNotFound,
    search = search
)
