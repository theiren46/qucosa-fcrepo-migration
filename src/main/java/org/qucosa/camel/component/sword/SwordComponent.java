package org.qucosa.camel.component.sword;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.impl.ProcessorEndpoint;

import java.util.Map;

public class SwordComponent extends DefaultComponent {
    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        if (remaining.startsWith("deposit")) {
            return new ProcessorEndpoint(uri, this, new SwordDepositProcessor());
        }
        throw new Exception("Unknown endpoint URI:" + remaining);
    }
}
