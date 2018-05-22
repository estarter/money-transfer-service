package com.revolut.test.api;

import java.io.Serializable;

public interface DataObject<TT> extends Serializable {

    TT getId();

    Long getVersion();

    void setVersion(Long version);

}
