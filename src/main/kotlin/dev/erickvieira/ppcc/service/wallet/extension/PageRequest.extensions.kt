package dev.erickvieira.ppcc.service.wallet.extension

import dev.erickvieira.ppcc.service.wallet.web.api.model.Direction
import dev.erickvieira.ppcc.service.wallet.web.api.model.WalletFields
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

fun PageRequest(
    pagination: Map<String, Any?>
): PageRequest = PageRequest.of(
    pagination["page"] as Int? ?: 0,
    pagination["size"] as Int? ?: 20,
    Sort
        .by((pagination["sort"] as WalletFields? ?: WalletFields.surname).value)
        .let { if (pagination["direction"] == Direction.desc) it.descending() else it.ascending() }
)