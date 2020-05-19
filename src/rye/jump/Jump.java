// This is a subset copied and renamed from https://github.com/fmnoise/flow/blob/master/src/fmnoise/flow/Fail.java
package rye.jump;

import clojure.lang.IExceptionInfo;
import clojure.lang.IPersistentMap;

public class Jump extends RuntimeException implements IExceptionInfo {
    public final IPersistentMap data;

    public Jump(IPersistentMap data, Throwable cause) {
        super(null, cause, false, false);
        this.data = data;
    }

    public IPersistentMap getData() {
        return data;
    }
}
