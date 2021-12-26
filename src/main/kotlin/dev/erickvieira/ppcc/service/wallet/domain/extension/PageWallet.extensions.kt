package dev.erickvieira.ppcc.service.wallet.domain.extension

import dev.erickvieira.ppcc.service.wallet.domain.entity.Wallet
import dev.erickvieira.ppcc.service.wallet.web.api.model.PageWalletDTO
import org.springframework.data.domain.Page

fun PageWalletDTO?.fromPage(page: Page<Wallet>) = this ?: PageWalletDTO(
    pageable = page.pageable,
    content = page.content.map { it.toWalletDTO() },
    total = page.totalElements,
    pageCount = page.totalPages,
    sortedBy = page.pageable.sort.toString()
)