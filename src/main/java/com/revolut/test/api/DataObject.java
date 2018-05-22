package com.revolut.test.api;

public interface DataObject<TT> {

    TT getId();

    Long getVersion();

    void setVersion(Long version);

}
