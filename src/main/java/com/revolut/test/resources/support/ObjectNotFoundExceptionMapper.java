package com.revolut.test.resources.support;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.revolut.test.db.support.ObjectNotFoundException;

import io.dropwizard.jersey.errors.ErrorMessage;

public class ObjectNotFoundExceptionMapper implements ExceptionMapper<ObjectNotFoundException> {

    @Override
    public Response toResponse(ObjectNotFoundException e) {
        Response.Status status = Response.Status.NOT_FOUND;
        return Response.status(status)
                       .type(MediaType.APPLICATION_JSON_TYPE)
                       .entity(new ErrorMessage(status.getStatusCode(), e.getMessage()))
                       .build();
    }

}
