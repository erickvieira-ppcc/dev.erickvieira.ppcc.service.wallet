package dev.erickvieira.ppcc.service.wallet.error

import dev.erickvieira.ppcc.service.wallet.domain.exception.BadRequestException
import dev.erickvieira.ppcc.service.wallet.domain.exception.UnexpectedException
import dev.erickvieira.ppcc.service.wallet.domain.exception.ConflictException
import dev.erickvieira.ppcc.service.wallet.domain.exception.NotFoundException
import dev.erickvieira.ppcc.service.wallet.web.api.model.ApiError
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class ApplicationExceptionHandler {

    @ExceptionHandler(BadRequestException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun exception(ex: BadRequestException) = ApiError(type = ex.type, message = ex.message)

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    fun exception(ex: NotFoundException) = ApiError(type = ex.type, message = ex.message)

    @ExceptionHandler(ConflictException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    fun exception(ex: ConflictException) = ApiError(type = ex.type, message = ex.message)

    @ExceptionHandler(UnexpectedException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun exception(ex: UnexpectedException) = ApiError(type = ex.type, message = ex.message)

}