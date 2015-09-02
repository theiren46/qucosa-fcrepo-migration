/*
 * Copyright (C) 2015 Saxon State and University Library Dresden (SLUB)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.qucosa.camel.component.sword;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.spi.Registry;
import org.apache.http.HttpResponse;

import static org.qucosa.camel.component.sword.SwordComponent.DepositMode.DEPOSIT;

public class SwordDepositProcessor implements Processor {

    private final SwordComponent.DepositMode mode;

    public SwordDepositProcessor(SwordComponent.DepositMode depositMode) {
        this.mode = depositMode;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Registry reg = exchange.getContext().getRegistry();
        SwordConnection connection = (SwordConnection) reg.lookupByName(SwordConnection.DATA_SOURCE_NAME);
        if (connection == null) {
            exchange.getIn().setFault(true);
            throw new Exception("No instance of " + SwordConnection.DATA_SOURCE_NAME +
                    " found in context registry.");
        }

        Message msg = exchange.getIn();
        SwordDeposit swordDeposit = (SwordDeposit) msg.getBody();

        final Boolean noopHeader = (Boolean) msg.getHeader("X-No-Op", true);
        final String onBehalfOfHeader = msg.getHeader("X-On-Behalf-Of", String.class);

        HttpResponse httpResponse;
        if (mode == DEPOSIT) {
            final String slugHeader = swordDeposit.getSlug();
            httpResponse = connection.deposit(swordDeposit, noopHeader, slugHeader, onBehalfOfHeader);
        } else {
            final String pid = msg.getHeader("PID", String.class);
            httpResponse = connection.update(pid, swordDeposit, noopHeader, onBehalfOfHeader);
        }

        exchange.getIn().setBody(httpResponse);
    }

}
