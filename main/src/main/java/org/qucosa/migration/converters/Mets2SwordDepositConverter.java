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

package org.qucosa.migration.converters;

import gov.loc.mets.MetsDocument;
import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.qucosa.camel.component.sword.SwordDeposit;

@Converter
public class Mets2SwordDepositConverter {

    @Converter
    public SwordDeposit toSwordDeposit(MetsDocument metsDocument, Exchange exchange) {
        Message msg = exchange.getIn();
        return new SwordDeposit(
                msg.getHeader("Slug").toString(),
                metsDocument.xmlText(),
                msg.getHeader("Content-Type").toString(),
                msg.getHeader("Collection").toString());
    }

}
