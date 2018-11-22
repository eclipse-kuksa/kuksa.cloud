/*******************************************************************************
 * Copyright (C) 2018 Netas Telekomunikasyon A.S.
 *  
 *  This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *  
 * SPDX-License-Identifier: EPL-2.0
 *  
 * Contributors:
 * Adem Kose, Fatih Ayvaz and Ilker Kuzu (Netas Telekomunikasyon A.S.) - Initial functionality
 ******************************************************************************/
package org.eclipse.kuksa.appstore.controller;


import org.eclipse.kuksa.appstore.exception.AlreadyExistException;
import org.eclipse.kuksa.appstore.exception.BadRequestException;
import org.eclipse.kuksa.appstore.exception.ForbiddenException;
import org.eclipse.kuksa.appstore.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@ControllerAdvice(basePackages = "com.netas.appstore.controller")
public class ExceptionController {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @ExceptionHandler(AlreadyExistException.class)
    @ResponseStatus(code = HttpStatus.CONFLICT)
    @ResponseBody
    public ResponseEntity<?> handleAlreadyExistException(AlreadyExistException ex) {
        LOGGER.error("[AlreadyExistException] Exception: {}", ex.getMessage());
        return new ResponseEntity(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    @ResponseBody
    public ResponseEntity<?> handleNotFoundException(NotFoundException ex) {
        LOGGER.error("[NotFoundException] Exception: {}", ex.getMessage());
        return new ResponseEntity(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(code = HttpStatus.FORBIDDEN)
    @ResponseBody
    public ResponseEntity<?> handleForbiddenException(ForbiddenException ex) {
        LOGGER.error("[ForbiddenException] Exception: {}", ex.getMessage());
        return new ResponseEntity(ex.getMessage(), HttpStatus.FORBIDDEN);
    }
    

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        LOGGER.error("[handleMethodArgumentNotValidException]: Method Argument Not Valid Exception  handler. Exception Message -> {}", ex.getBindingResult().getFieldError().getDefaultMessage());
        return new ResponseEntity(ex.getBindingResult().getFieldError().getDefaultMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidFormatException.class)
    @ResponseBody
    public ResponseEntity<?> handleInvalidFormatException(InvalidFormatException ex) {
        LOGGER.error("[handleInvalidFormatException] Exception: {}", ex.getMessage()); // TODO value causes to exception should be taken from exception message
        String errorResponseText = null;
        if (ex.getPath() != null && !ex.getPath().isEmpty()) {
            if (ex.getTargetType().getEnumConstants() != null && ex.getTargetType().getEnumConstants().length > 0) {
                //errorResponseText = Utils.getNotInListError(String.valueOf(ex.getValue()), Arrays.toString(ex.getTargetType().getEnumConstants()));
                errorResponseText = "ADEM";
            } else {
                errorResponseText = ex.getOriginalMessage();
            }

        } else {
            errorResponseText = "Invalid JSON format!";
        }
        return new ResponseEntity<>(errorResponseText, HttpStatus.BAD_REQUEST);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<?> handleBadRequestException(BadRequestException ex) {
        LOGGER.error("[BadRequestException] Exception: {}", ex.getMessage());
        return new ResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
