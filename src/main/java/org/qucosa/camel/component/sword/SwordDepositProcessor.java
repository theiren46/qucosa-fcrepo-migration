package org.qucosa.camel.component.sword;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.spi.Registry;

public class SwordDepositProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        Registry reg = exchange.getContext().getRegistry();
        SwordConnection connection = (SwordConnection) reg.lookupByName(SwordConnection.DATA_SOURCE_NAME);
        if (connection == null) {
            throw new Exception("No instance of " + SwordConnection.DATA_SOURCE_NAME +
                    " found in context registry.");
        }

        Message msg = exchange.getIn();
        SwordDeposit deposit = (SwordDeposit) msg.getBody();

        connection.deposit(deposit);
    }

}
